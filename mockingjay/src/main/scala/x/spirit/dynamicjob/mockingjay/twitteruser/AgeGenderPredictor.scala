package x.spirit.dynamicjob.mockingjay.twitteruser

import java.time.Year

import org.apache.hadoop.hbase.client.Scan
import org.apache.hadoop.hbase.filter.PrefixFilter
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.{SparkConf, SparkContext}
import x.spirit.dynamicjob.mockingjay.hbase.{HBaseConfig, _}

/**
  * Created by zhangwei on 11/4/16.
  *
  */
object AgeGenderPredictor extends App {


  override def main(args: Array[String]) {
    val sparkConf = new SparkConf().setAppName(this.getClass.getSimpleName)
    val sc = new SparkContext(sparkConf)

    implicit val config = HBaseConfig(
      hbaseXmlConfigFile = "hbase-site.xml"
    )
    config.get.set("hbase.rpc.timeout", "18000000")

    val sqlContext = new SQLContext(sc)

    val ageCSVFile = "hdfs://geotwitter.ttu.edu:54310/user/hadoopuser/geotwitterCSV/1912_2000.txt"

    val ageCSVSchema = StructType(Array(
      StructField("year", LongType, true),
      StructField("firstname", StringType, true),
      StructField("gender", StringType, true),
      StructField("occurance", LongType, true)
    ))

    val ageCSVDF = sqlContext.read.format("com.databricks.spark.csv").option("header", "true").schema(ageCSVSchema).load(ageCSVFile)

    val nameMax = ageCSVDF.groupBy("firstname").agg(max("occurance"), sum("occurance"))
      .map({ row =>
        row.getAs[String]("firstname") -> (
          row.getAs[Long]("max(occurance)"),
          row.getAs[Long]("sum(occurance)")
          )
      }).collectAsMap
    val nameYearGenderMap = nameMax.map({ case (k, v) =>
      val yearAndGenderRow = ageCSVDF.where("firstname='%s'".format(k)).where("occurance=%d".format(v._1))
        .select("year", "gender").first
      val year = yearAndGenderRow.getAs[Long]("year")
      val gender = yearAndGenderRow.getAs[String]("gender")
      val pct_probability = 100.0d * (v._1.toDouble) / v._2.toDouble

      k.toUpperCase -> (year, gender, pct_probability)
    })

    val genderCSVFile = "hdfs://geotwitter.ttu.edu:54310/user/hadoopuser/geotwitterCSV/firstname_list.csv"
    val genderCSVSchema = StructType(Array(
      StructField("firstname", StringType, true),
      StructField("male_times", LongType, true),
      StructField("female_times", LongType, true),
      StructField("occurrences", LongType, true)
    ))

    val genderCSVDF = sqlContext.read.format("com.databricks.spark.csv").option("header", "true").schema(genderCSVSchema).load(genderCSVFile)

    val nameGenderMap = genderCSVDF.map({ row =>
      val name = row.getAs[String]("firstname");
      val total = row.getAs[Long]("occurrences").toDouble;
      val male_pct = 100.0d * row.getAs[Long]("male_times").toDouble / total;
      val female_pct = 100.0d * row.getAs[Long]("female_times").toDouble / total;
      name.toUpperCase -> (male_pct, female_pct)
    }).collectAsMap()

    var startRowPrefix = 10

    while (startRowPrefix <= 99) {
      println("Start row prefix = %d".format(startRowPrefix))
      val scan = new Scan()
      scan.setCaching(100)
      scan.setCacheBlocks(true)
      scan.setAttribute(Scan.HINT_LOOKAHEAD, Bytes.toBytes(2))
      scan.setFilter(new PrefixFilter(Bytes.toBytes(startRowPrefix.toString)))
      val scanRst = sc.hbase[String]("machineLearn2012", Set("username"), scan)
      scanRst.map({ case (k, v) =>
        val uid = k;
        val firstName = v("username").getOrElse("firstName", "");
        // decide age
        val nameYearGender = nameYearGenderMap.getOrElse(firstName, (1912L, "F", 0.0d));
//        val age = Year.now().getValue.toLong - nameYearGender._1
        val year = nameYearGender._1
        val yearProb = nameYearGender._3;
        // decide gender
        var gender = "X"
        val genderYearProb = yearProb;
        gender = nameYearGender._2

        var tempGender = "X"
        var tempGenderProb = 0.0d
        val nameGenderTuple = nameGenderMap.getOrElse(firstName, (0.0d, 0.0d))
        if (nameGenderTuple._1 > nameGenderTuple._2) {
          tempGender = "M"
          tempGenderProb = nameGenderTuple._1
        } else if (nameGenderTuple._1 < nameGenderTuple._2) {
          tempGender = "F"
          tempGenderProb = nameGenderTuple._2
        } else {
          tempGender = "X"
          tempGenderProb = 0.0d
        }

        if (!gender.equals(tempGender)) {
          if (genderYearProb < tempGenderProb) {
            gender = tempGender;
          }
        }

        val record = Map(
          "Age" -> Map("year" -> Bytes.toBytes(year), "prob" -> Bytes.toBytes(yearProb)),
          "Gender" -> Map(
            "gender" -> Bytes.toBytes(gender),
            "year_prob" -> Bytes.toBytes(genderYearProb),
            "male_prob" -> Bytes.toBytes(nameGenderTuple._1),
            "female_prob" -> Bytes.toBytes(nameGenderTuple._2)
          )
        )
        uid -> record
      }).toHBase("machineLearn2012")
      startRowPrefix += 1;
    }
  }
}
