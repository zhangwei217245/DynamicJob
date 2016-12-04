package x.spirit.dynamicjob.mockingjay.twitteruser

import org.apache.hadoop.hbase.client.Scan
import org.apache.hadoop.hbase.filter.{PageFilter, PrefixFilter}
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
    var startRowPrefix = 1;
    while (startRowPrefix <= 9) {
      System.out.println("Start row prefix = %d".format(startRowPrefix))
      val scanRst = sc.hbase[String]("sent_blue_red_2012", Set("tsent"),
        new PrefixFilter(Bytes.toBytes(startRowPrefix.toString)))
        scanRst.map({ case (k, v) =>
          val uid = k;
          val tsent = v("tsent")
          val blue_red = tsent.map({ case (tid, jsonBytes) =>
            val jsonArr = new JSONArray(Bytes.toString(jsonBytes))
            val overallSenti = jsonArr.getInt(0)
            val blueScore = jsonArr.getInt(1)
            val redScore = jsonArr.getInt(2)
            val numSentence = jsonArr.getInt(3)
            (overallSenti, blueScore, redScore, numSentence)
          }).filter(_._4 > 0)

          var decision = None

          var avgBlue = 0.0d
          var avgRed = 0.0d

          if (blue_red.nonEmpty){
            val blueScores = blue_red.map({case (o, b,r, n)=> (b,n)}).filter(_._1 >= -10)
            val redScores = blue_red.map({case (o, b, r, n) => (r,n)}).filter(_._1 >= -10)

            avgBlue = blueScores.map(_._1).sum.toDouble/blueScores.map(_._2).sum.toDouble
            avgRed = redScores.map(_._1).sum.toDouble/redScores.map(_._2).sum.toDouble

            if (avgBlue.equals(avgRed)) {
              decision = Neutral;
            } else if (avgBlue > avgRed) {
              decision = Blue;
            } else {
              decision = Red;
            }
          }

          uid -> Map({
            "political" -> Map(
              "type" -> Bytes.toBytes(decision.id),
              "sumblue" -> Bytes.toBytes(avgBlue),
              "sumred" -> Bytes.toBytes(avgRed)
            )
          })
        }).toHBase("machineLearn2012")
        startRowPrefix += 1;
    }
  }
}
