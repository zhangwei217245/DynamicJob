package x.spirit.dynamicjob.mockingjay.exporter.csv

import org.apache.hadoop.hbase.client.Scan
import org.apache.hadoop.hbase.filter.PrefixFilter
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import x.spirit.dynamicjob.mockingjay.hbase.{HBaseConfig, _}
import x.spirit.dynamicjob.mockingjay.twitteruser.PoliticalPreference

import scala.collection.mutable.ArrayBuffer

/**
  * Created by zhangwei on 11/24/16.
  */
object TweetsPoliticalPreferenceExporter extends App{

  override def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName(this.getClass.getSimpleName)
      .set("spark.kryoserializer.buffer.max","2000")
    val sc = new SparkContext(sparkConf)

    implicit val config = HBaseConfig(
      hbaseXmlConfigFile = "hbase-site.xml"
    )
    config.get.set("hbase.rpc.timeout", "18000000")

    val sqlContext = new SQLContext(sc)


    val headers = Array(
      "uid",
      "tweet",
      "political:type"
    )

    var startRowPrefix = 10

    var NoneResult: ArrayBuffer[(String, Map[String, Map[String, Array[Byte]]])]  =
      ArrayBuffer()
    var BlueResult: ArrayBuffer[(String, Map[String, Map[String, Array[Byte]]])]  =
      ArrayBuffer()
    var RedResult: ArrayBuffer[(String, Map[String, Map[String, Array[Byte]]])]  =
      ArrayBuffer()
    var NeutralResult: ArrayBuffer[(String, Map[String, Map[String, Array[Byte]]])]  =
      ArrayBuffer()

    val outputMap = Map(
      "None" -> NoneResult,
      "Blue" -> BlueResult,
      "Red" -> RedResult,
      "Neutral" -> NeutralResult
    )

    while (startRowPrefix <= 99) {
      println("Start row prefix = %d".format(startRowPrefix))

      val scan = new Scan()
      scan.setCaching(100)
      scan.setCacheBlocks(true)
      scan.setAttribute(Scan.HINT_LOOKAHEAD, Bytes.toBytes(2))
      scan.setFilter(new PrefixFilter(Bytes.toBytes(startRowPrefix.toString)))
      val twitterUserCF = Set("tweet")
      val machineLearnCF = Set("political")

      val tweetScanRst = sc.hbase[Array[Byte]]("twitterUser", twitterUserCF , scan)
      val politicalScanRst = sc.hbase[Array[Byte]]("machineLearn2012", machineLearnCF, scan)


      val temprst = tweetScanRst.join(politicalScanRst).map({ row =>
        row._1 -> (row._2._1++row._2._2)
      })

      NoneResult ++= temprst.filter({row=>
        val politicalType = Bytes.toInt(row._2("political").apply("type"))
        politicalType == 0
      }).take(10)
      BlueResult ++= temprst.filter({row=>
        val politicalType = Bytes.toInt(row._2("political").apply("type"))
        politicalType == 1
      }).take(10)
      RedResult ++= temprst.filter({row=>
        val politicalType = Bytes.toInt(row._2("political").apply("type"))
        politicalType == 2
      }).take(10)
      NeutralResult ++= temprst.filter({row=>
        val politicalType = Bytes.toInt(row._2("political").apply("type"))
        politicalType == 3
      }).take(10)
      startRowPrefix=startRowPrefix+1;
    }

    for ((k,v) <- outputMap){
      val rstRDD = sc.makeRDD(v).map(row =>{
        val uid = row._1
        val politicalPreferenceString = PoliticalPreference(Bytes.toInt(row._2("political").apply("type")))
        val tweets = row._2("tweet").values.map({twtRow =>
          "\"%s\",\"%s\",\"%s\"".format(uid,politicalPreferenceString, Bytes.toString(twtRow))
        })
        tweets
      }).flatMap(_.seq)
      rstRDD.saveAsTextFile("hdfs://geotwitter.ttu.edu:54310/user/hadoopuser/geotwitterOutput/sentiment/%s.csv".format(k))
    }
  }

}
