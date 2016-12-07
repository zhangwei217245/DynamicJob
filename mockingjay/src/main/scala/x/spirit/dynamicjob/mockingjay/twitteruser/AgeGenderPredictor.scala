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

    /**
      * Possible Map Sample :
      *
      * Jane,(1921,F,49%)
      * Jane,(2000,M,12%)
      * Wei,(1912,M,99%)
      */
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

    /**
      * Map format:
      *
      * name, male_prob, female_prob
      */
    val nameGenderMap = genderCSVDF.map({ row =>
      val name = row.getAs[String]("firstname");
      val total = (row.getAs[Long]("male_times")+row.getAs[Long]("female_times")).toDouble;
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
        val uid = k
        val firstName = v("username").getOrElse("firstName", "").toUpperCase;

        var genderSource = "FB"

        // decide age
        val nameYearGender = nameYearGenderMap.getOrElse(firstName, (100L, "X", -0.001d));
        val year = nameYearGender._1
        val socialSecurityGender = nameYearGender._2
        val yearProb = nameYearGender._3;


        var gender = "X"
        var male_prob = 0.5d
        var female_prob = 0.5d

        // decide gender first according to facebook data.
        val nameGenderTuple = nameGenderMap.getOrElse(firstName, (0.5d, 0.5d))
        if (nameGenderTuple._1 > nameGenderTuple._2) {
          gender = "M"
        } else if (nameGenderTuple._1 < nameGenderTuple._2) {
          gender = "F"
        }

        male_prob = nameGenderTuple._1
        female_prob = nameGenderTuple._2

        if (gender.equalsIgnoreCase("X") && (!socialSecurityGender.equalsIgnoreCase("X"))) {
          gender = socialSecurityGender
          genderSource = "SSO"
          if (gender.equalsIgnoreCase("F")){
            female_prob = yearProb
            male_prob = 1.0d - yearProb
          } else if (gender.equalsIgnoreCase("M")){
            male_prob = yearProb
            female_prob = 1.0d - yearProb
          }
        }

        val record = Map(
          "Age" -> Map("year" -> Bytes.toBytes(year), "prob" -> Bytes.toBytes(yearProb)),
          "Gender" -> Map(
            "gender" -> Bytes.toBytes(gender),
            "year_prob" -> Bytes.toBytes(yearProb),
            "male_prob" -> Bytes.toBytes(male_prob),
            "female_prob" -> Bytes.toBytes(female_prob),
            "source" -> Bytes.toBytes(genderSource)
          )
        )
        uid -> record
      }).toHBase("machineLearn2012")
      startRowPrefix += 1;
    }
  }
}
