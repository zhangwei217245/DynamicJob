package x.spirit.dynamicjob.mockingjay.scanner

import org.apache.hadoop.hbase.filter.PrefixFilter
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.{SparkConf, SparkContext}
import org.json.JSONArray
import x.spirit.dynamicjob.mockingjay.hbase.{HBaseConfig, _}
import x.spirit.dynamicjob.mockingjay.twitteruser.PoliticalPreference._

/**
  * Created by zhangwei on 8/1/16.
  */
object TweetsScanner extends App {

  override def main(args: Array[String]) {
    val sparkConf = new SparkConf().setAppName(this.getClass.getName)
    val sc = new SparkContext(sparkConf)

    implicit val config = HBaseConfig(
      hbaseXmlConfigFile = "hbase-site.xml"
    )

    config.get.set("hbase.rpc.timeout", "1800000");

    /**
      * For all hbase filters, refer to :
      * https://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/package-summary.html
      * Here, it's better to use PageFilter and
      */
    var startRowPrefix = 100;
    var overallTweet = 0.0D;
    var overallBlueTweet = 0.0D;
    var overallRedTweet = 0.0D;
    var overallUser = 0L;
    var blueUser = 0L;
    var redUser = 0L;
    var neutralUser = 0L;
    while (startRowPrefix <= 999) {
      System.out.println("Start row prefix = %d".format(startRowPrefix))
      val scanRst = sc.hbase[String]("sent_blue_red_2012", Set("tsent"),
        new PrefixFilter(Bytes.toBytes(startRowPrefix.toString)));

      val intermediateResult = scanRst.map({ case (k, v) =>
        val uid = k;
        val tsent = v("tsent")
        val blue_red = tsent.map({ case (tid, jsonBytes) =>
          val jsonArr = new JSONArray(Bytes.toString(jsonBytes));
          val blueScore = jsonArr.getInt(1)
          val redScore = jsonArr.getInt(2)
          (blueScore, redScore, 1L)
        })

        val tweetCount = blue_red.map(_._3).sum
        val blueTweetCount = blue_red.filter(r => {r._1 > r._2}).map(_._3).sum
        val redTweetCount = blue_red.filter(r => {r._2 > r._1}).map(_._3).sum

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

        (tweetCount, blueTweetCount, redTweetCount, decision.id)

      })
      overallTweet+=intermediateResult.map(_._1).sum()
      overallBlueTweet+=intermediateResult.map(_._2).sum()
      overallRedTweet+=intermediateResult.map(_._3).sum()

      overallUser+=intermediateResult.count;
      blueUser+=intermediateResult.filter(_._4 == Blue.id).count;
      redUser+=intermediateResult.filter(_._4 == Red.id).count;
      neutralUser+=intermediateResult.filter(_._4 == Neutral.id).count;

      startRowPrefix += 1;
    }
    System.out.println("[FINAL TWEET RST] Overall %d, Blue %d, Red %d"
      .format(overallTweet,overallBlueTweet,overallRedTweet));
    System.out.println("[FINAL USER RST] Overall %d, Blue %d, Red %d, Neutral %d"
      .format(overallUser, blueUser, redUser, neutralUser))
  }
}
