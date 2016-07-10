package x.spirit.dynamicjob.mockingjay

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import x.spirit.dynamicjob.mockingjay.hbase.HBaseConfig

/**
  * Created by zhangwei on 7/7/16.
  */
object UserSentiment extends App {


  override def main(args: Array[String]) {
    if (args.length < 1) {
      System.err.println("Usage: FeedImporter <file>")
      System.exit(1);
    }
    val sparkConf = new SparkConf().setAppName("FileImporter")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)

    implicit val config = HBaseConfig(
      hbaseXmlConfigFile = "hbase-site.xml"
    )


  }
}
