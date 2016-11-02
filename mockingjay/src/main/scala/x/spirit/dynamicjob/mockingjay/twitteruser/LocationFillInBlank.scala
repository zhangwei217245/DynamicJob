package x.spirit.dynamicjob.mockingjay.twitteruser

import org.apache.hadoop.hbase.client.Scan
import org.apache.hadoop.hbase.filter.PrefixFilter
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.{SparkConf, SparkContext}
import org.json.JSONArray
import x.spirit.dynamicjob.mockingjay.hbase.{HBaseConfig, _}

/**
  * Created by zhangwei on 9/25/16.
  */
object LocationFillInBlank extends App {

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
    var allRst: Array[(String, Int)] = Array();
    while (startRowPrefix <= 9) {
      println("Start row prefix = %d".format(startRowPrefix))
      val scan = new Scan()
      scan.setCaching(100)
      scan.setCacheBlocks(true)
      scan.setAttribute(Scan.HINT_LOOKAHEAD, Bytes.toBytes(2))
      scan.setFilter(new PrefixFilter(Bytes.toBytes(startRowPrefix.toString)))
      val scanRst = sc.hbase[String]("machineLearn2012", Set("location"), scan)
      scanRst.map({ case (k, v) =>
        val uid = k;
        val locationsAtDifferentLevel = v("location")
        var precise: Array[Double] = null;
        var city: Array[Double] = null;
        var admin: Array[Double] = null;

        locationsAtDifferentLevel.foreach({ case (precision, jsonBytes) =>
          val jsonArr = new JSONArray(Bytes.toString(jsonBytes))
          if (jsonArr != null && jsonArr.length() >= 2) {
            val x = jsonArr.getDouble(0)
            val y = jsonArr.getDouble(1)
            if (precision.equals("precise")) {
              precise = Array[Double](x, y);
              city = Array[Double](x, y);
              admin = Array[Double](x, y);
            } else if (precision.equals("city")) {
              city = Array[Double](x, y);
              admin = Array[Double](x, y);
            } else if (precision.equals("admin")) {
              admin = Array[Double](x, y);
            }
          }
        })
        val record = Map(
          "precise" -> precise,
          "city" -> city,
          "admin" -> admin
        ).map({ case (field, data) =>
          var arrStr = "[]";
          if (data != null) {
              arrStr = new JSONArray(data).toString
          }
          field -> Bytes.toBytes(arrStr);
        })
        uid -> Map("location" -> record)
      }).toHBase("machineLearn2012")
      startRowPrefix += 1;
    }
  }
}
