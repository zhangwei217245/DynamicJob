package x.spirit.dynamicjob.mockingjay.scanner

import org.apache.hadoop.hbase.filter.PrefixFilter
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.{SparkConf, SparkContext}
import org.json.JSONArray
import x.spirit.dynamicjob.mockingjay.hbase.{HBaseConfig, _}
import x.spirit.dynamicjob.mockingjay.twitteruser.PoliticalPreference._

import scala.collection.mutable

/**
  * Created by zhangwei on 8/1/16.
  */
object LocationScanner extends App{

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
    var startRowPrefix = 1;
    var allRst:Array[(String, Int)] = Array();
    while (startRowPrefix <= 9) {
      System.out.println("Start row prefix = %d".format(startRowPrefix))
      val scanRst = sc.hbase[String]("twitterUser", Set("tweet"),
        new PrefixFilter(Bytes.toBytes(startRowPrefix.toString)))
      val typeMap = scanRst.map({ case (k, v) =>
        val uid = k;
        val tweet = v("tweet")
        val types = tweet.map({ case (tid, jsonBytes) =>
          val jsonArr = new JSONArray(Bytes.toString(jsonBytes));
          val content = jsonArr.getJSONArray(1);
          val placeType = content.getString(2);
          (placeType, 1)
        }).groupBy(_._1).map({case(k,lst) =>
          (k, lst.size)
        })
        types
      }).flatMap({map=>map}).groupBy(_._1).map({case(k,v)=>(k,v.map(_._2).sum)}).collect
      allRst = Array.concat(allRst, typeMap);
      allRst = allRst.groupBy(_._1).map({case(k,v)=>(k,v.map(_._2).sum)}).toArray
      startRowPrefix += 1;
    }
    allRst.foreach({case(k,v)=>
      println("%s -> %s ".format(k, v))
    })
  }
}
