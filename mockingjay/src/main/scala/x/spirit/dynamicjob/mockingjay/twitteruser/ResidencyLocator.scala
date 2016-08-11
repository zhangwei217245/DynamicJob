package x.spirit.dynamicjob.mockingjay.twitteruser

import org.apache.hadoop.hbase.filter.PrefixFilter
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.{SparkConf, SparkContext}
import org.json.JSONArray
import x.spirit.dynamicjob.mockingjay.hbase.{HBaseConfig, _}

/**
  * Created by zhangwei on 8/10/16.
  */
object ResidencyLocator extends App {

  override def main(args: Array[String]) {
    val sparkConf = new SparkConf().setAppName("ResidencyLocator")
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
    var allRst: Array[(String, Int)] = Array();
    while (startRowPrefix <= 9) {
      System.out.println("Start row prefix = %d".format(startRowPrefix))
      val scanRst = sc.hbase[String]("twitterUser", Set("tweet"),
        new PrefixFilter(Bytes.toBytes(startRowPrefix.toString)))
      val typeMap = scanRst.map({ case (k, v) =>
        val uid = k;
        val tweet = v("tweet")
        val types = tweet.map({ case (tid, jsonBytes) =>
          val jsonArr = new JSONArray(Bytes.toString(jsonBytes));

          val time = jsonArr.getJSONArray(0);
          val content = jsonArr.getJSONArray(1);
          val coord = jsonArr.getJSONArray(2);


          val placeType = content.getString(2);
          (placeType, 1)
        }).groupBy(_._1).map({ case (k, lst) =>
          (k, lst.size)
        })
        types
      }).flatMap({ map => map }).groupBy(_._1).map({ case (k, v) => (k, v.map(_._2).sum) }).collect
      allRst = Array.concat(allRst, typeMap);
      startRowPrefix += 1;
    }
    allRst.groupBy(_._1).map({ case (k, v) => (k, v.map(_._2).sum) }).foreach({ case (k, v) =>
      System.out.println("%s -> %s ".format(k, v))
    })
  }
}
