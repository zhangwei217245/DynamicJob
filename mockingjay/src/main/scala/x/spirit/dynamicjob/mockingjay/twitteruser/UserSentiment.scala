package x.spirit.dynamicjob.mockingjay.twitteruser

import org.apache.hadoop.hbase.client.Scan
import org.apache.hadoop.hbase.filter.PageFilter
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.{SparkConf, SparkContext}
import org.json.JSONArray
import x.spirit.dynamicjob.mockingjay.hbase.{HBaseConfig, _}
import x.spirit.dynamicjob.mockingjay.twitteruser.PoliticalPreference._

/**
  * Created by zhangwei on 7/7/16.
  */
object UserSentiment extends App {

  override def main(args: Array[String]) {
    val sparkConf = new SparkConf().setAppName("UserSentimentCalculator")
    val sc = new SparkContext(sparkConf)

    implicit val config = HBaseConfig(
      hbaseXmlConfigFile = "hbase-site.xml"
    )

    /**
      * For all hbase filters, refer to :
      * https://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/package-summary.html
      * Here, it's better to use PageFilter and
      */
    var notfinished = true;
    var startRow = 0L;
    while (notfinished) {
      System.out.println("Start row = %d".format(startRow))
      val scan = new Scan(Bytes.toBytes(startRow.toString), new PageFilter(100l))
      val scanRst = sc.hbase[String]("sent_blue_red_2012", Set("tsent"), scan)
      val rstCount = scanRst.count();
      if (rstCount > 0) {
        scanRst.map({ case (k, v) =>
          val uid = k;
          val tsent = v("tsent")
          val blue_red = tsent.map({ case (tid, jsonBytes) =>
            val jsonArr = new JSONArray(Bytes.toString(jsonBytes));
            val blueScore = jsonArr.getInt(1)
            val redScore = jsonArr.getInt(2)
            (blueScore, redScore, 1)
          })
          val sumBlue = blue_red.map(_._1).sum
          val sumRed = blue_red.map(_._2).sum
          var decision = None;
          if (sumBlue.equals(sumRed)) {
            decision = Neutral;
          } else if (sumBlue > sumRed) {
            decision = Blue;
          } else {
            decision = Red;
          }
          uid -> Map({
            "political" -> Map(
              "type" -> Bytes.toBytes(decision.id),
              "sumblue" -> Bytes.toBytes(sumBlue),
              "sumred" -> Bytes.toBytes(sumRed)
            )
          })
        }).toHBase("machineLearn2012")
        startRow = scanRst.collect().last._1.toLong + 1L
      } else {
        notfinished = false;
      }
    }
  }
}
