package x.spirit.dynamicjob.mockingjay.twitteruser

import org.apache.hadoop.hbase.client.Scan
import org.apache.hadoop.hbase.filter.{PageFilter, PrefixFilter}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import x.spirit.dynamicjob.mockingjay.corenlp.functions._
import x.spirit.dynamicjob.mockingjay.hbase._
import x.spirit.dynamicjob.mockingjay.hbase.HBaseConfig

/**
  * Created by zhangwei on 7/7/16.
  */
object UserSentiment extends App{

  lazy val sparkConf = new SparkConf().setAppName("UserSentimentCalculator")
  lazy val sc = new SparkContext(sparkConf)
  lazy val sqlContext = new SQLContext(sc)

  implicit val config = HBaseConfig(
    hbaseXmlConfigFile = "hbase-site.xml"
  )

  override def main(args: Array[String]) {
    /**
      * For all hbase filters, refer to :
      * https://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/package-summary.html
      * Here, it's better to use PageFilter and
      */
    val scan = new Scan(Bytes.toBytes("0"), new PageFilter(100l))

    sc.hbase[String]("sent_blue_red_2012", Set("tsent"), scan)
      .map({ case (k, v) =>
        val tsent = v("tsent")
        tsent.map({case(tid, jsonArr)=>
          System.out.println(jsonArr);
        })
      })

  }
}
