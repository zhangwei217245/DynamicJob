package x.spirit.dynamicjob.mockingjay.twitteruser

import java.time.{Duration, Instant, ZoneId, ZonedDateTime}

import breeze.linalg.DenseMatrix
import org.apache.hadoop.hbase.client.Scan
import org.apache.hadoop.hbase.filter.PrefixFilter
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.{SparkConf, SparkContext}
import org.json.JSONArray
import x.spirit.dynamicjob.mockingjay.hbase.{HBaseConfig, _}
import x.spirit.dynamicjob.mockingjay.utils.TimeZoneMapping

/**
  * Created by zhangwei on 9/25/16.
  */
object NameExtractor extends App{

  override def main(args: Array[String]) {
    val sparkConf = new SparkConf().setAppName(this.getClass.getSimpleName)
    val sc = new SparkContext(sparkConf)

    implicit val config = HBaseConfig(
      hbaseXmlConfigFile = "hbase-site.xml"
    )
    config.get.set("hbase.rpc.timeout", "18000000")


    /**
      * For all hbase filters, refer to :
      * https://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/package-summary.html
      * Here, it's better to use PageFilter and
      */
    var startRowPrefix = 10
    var allRst: Array[(String, Int)] = Array()
    while (startRowPrefix <= 99) {
      println("Start row prefix = %d".format(startRowPrefix))
      val scan = new Scan()
      scan.setCaching(100)
      scan.setCacheBlocks(true)
      scan.setAttribute(Scan.HINT_LOOKAHEAD, Bytes.toBytes(2))
      scan.setFilter(new PrefixFilter(Bytes.toBytes(startRowPrefix.toString)))
      val scanRst = sc.hbase[String]("twitterUser", Set("user"), scan)
      scanRst.map({ case (k, v) =>
        val uid = k
        val userData = v("user")
        val name = userData.getOrElse("name", "");
        val nameArray = name.split("\\W+");
        var firstName = "";
        var lastName = "";
        if (nameArray.length == 1) {
            firstName = nameArray(0);
        } else if (nameArray.length == 2){
            firstName = nameArray(0)
            lastName = nameArray(1)
        } else if (nameArray.length > 2) {
            firstName = nameArray(0)
            lastName = nameArray(nameArray.length-1);
        }

        uid -> Map("username" -> Map(
          "firstName" -> Bytes.toBytes(firstName.toLowerCase),
          "lastName" -> Bytes.toBytes(lastName.toLowerCase)
        ))
      }).toHBase("machineLearn2012")
      startRowPrefix += 1;
    }
  }
}
