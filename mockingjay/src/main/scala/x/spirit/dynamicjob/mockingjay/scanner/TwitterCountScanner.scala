package x.spirit.dynamicjob.mockingjay.scanner

import org.apache.hadoop.hbase.filter.PrefixFilter
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.{SparkConf, SparkContext}
import x.spirit.dynamicjob.mockingjay.hbase.{HBaseConfig, _}

/**
  * Created by zhangwei on 8/29/16.
  */
object TwitterCountScanner extends App{

  override def main(args: Array[String]) {
    val sparkConf = new SparkConf().setAppName(this.getClass.getSimpleName)
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
    var startRowPrefix = 10;
    var allRst:Array[(String, Int)] = Array();
    while (startRowPrefix <= 99) {
      System.out.println("Start row prefix = %d".format(startRowPrefix))
      val scanRst = sc.hbase[String]("twitterUser", Set("tweet"),
        new PrefixFilter(Bytes.toBytes(startRowPrefix.toString)))
      val sizeMap = scanRst.map({ case (k, v) =>
        val uid = k;
        val tweet = v("tweet")
        val tweetSize = tweet.size
        var sizeType = "0 - 1000"
        if (tweetSize >= 1000 && tweetSize < 2000) {
          sizeType = "1000 - 2000"
        } else if (tweetSize >= 2000 && tweetSize < 3000) {
          sizeType = "2000 - 3000"
        } else if (tweetSize >= 3000 && tweetSize < 4000) {
          sizeType = "3000 - 4000"
        } else if (tweetSize >= 4000 && tweetSize < 5000) {
          sizeType = "4000 - 5000"
        } else if (tweetSize >= 5000 && tweetSize < 10000) {
          sizeType = "5000 - 10000"
        } else if (tweetSize >= 10000 && tweetSize < 15000){
          sizeType = "10000 - 15000"
        } else if (tweetSize >= 15000 && tweetSize < 20000){
          sizeType = "15000 - 20000"
        } else if (tweetSize >= 20000) {
          sizeType = "20000 - ~"
        }
        (sizeType, 1)
      }).groupBy(_._1).map({case(k,v)=>(k,v.map(_._2).sum)}).collect
      println("sizeMap = " + sizeMap)
      allRst = Array.concat(allRst, sizeMap);
      allRst = allRst.groupBy(_._1).map({case(k,v)=>(k,v.map(_._2).sum)}).toArray
      startRowPrefix += 1;
    }
    allRst.foreach({case(k,v)=>
      println("%s -> %s ".format(k, v))
    })
  }
}
