package x.spirit.dynamicjob.mockingjay.twitteruser

import java.io.File

import org.apache.hadoop.hbase.client.Scan
import org.apache.hadoop.hbase.filter.PrefixFilter
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.types._
import org.geotools.data.shapefile.ShapefileDataStore
import org.json.JSONArray
import x.spirit.dynamicjob.mockingjay.hbase.{HBaseConfig, _}

/**
  * Created by zhangwei on 11/4/16.
  */
object GenderPredictor extends App{


  override def main(args: Array[String]){
    val sparkConf = new SparkConf().setAppName(this.getClass.getSimpleName)
    val sc = new SparkContext(sparkConf)

    implicit val config = HBaseConfig(
      hbaseXmlConfigFile = "hbase-site.xml"
    )
    config.get.set("hbase.rpc.timeout", "18000000")

    val surnamePath = "hdfs://geotwitter.ttu.edu:54310/user/hadoopuser/geotwitter/surname.csv"

    val sqlContext = new SQLContext(sc)
    val customSchema = StructType(Array(
      StructField("name", StringType, true),
      StructField("rank", IntegerType, true),
      StructField("count", IntegerType, true),
      StructField("prop100k", DoubleType, true),
      StructField("cum_prop100k", DoubleType, true),
      StructField("pctwhite", DoubleType, true),
      StructField("pctblack", DoubleType, true),
      StructField("pctapi", DoubleType, true),
      StructField("pctaian", DoubleType, true),
      StructField("pct2prace", DoubleType, true),
      StructField("pcthispanic", DoubleType, true)
    ))

    val df = sqlContext.read.format("com.databricks.spark.csv").option("header", "true").schema(customSchema).load(surnamePath)


    val shapeFileRootDir = "/home/hadoopuser/shapefiles";
    val shapeFileAddrTemplate = "%s/%s/%s.shp"

    val tractFeature = "Tract_2010Census_DP1"
    val countyFeature = "County_2010Census_DP1"
    val stateFeature = "State_2010Census_DP1"

    val tractFileName = shapeFileAddrTemplate.format(shapeFileRootDir, tractFeature, tractFeature)
    val countyFileName = shapeFileAddrTemplate.format(shapeFileRootDir, countyFeature, countyFeature)
    val stateFileName = shapeFileAddrTemplate.format(shapeFileRootDir, stateFeature, stateFeature)

    val tractDataStore: ShapefileDataStore = new ShapefileDataStore(new File(tractFileName).toURI.toURL)
    val countyDataStore: ShapefileDataStore = new ShapefileDataStore(new File(countyFileName).toURI.toURL)
    val stateDataStore: ShapefileDataStore = new ShapefileDataStore(new File(stateFileName).toURI.toURL)

    var startRowPrefix = 10

    while (startRowPrefix <= 99) {
//      println("Start row prefix = %d".format(startRowPrefix))
//      val scan = new Scan()
//      scan.setCaching(100)
//      scan.setCacheBlocks(true)
//      scan.setAttribute(Scan.HINT_LOOKAHEAD, Bytes.toBytes(2))
//      scan.setFilter(new PrefixFilter(Bytes.toBytes(startRowPrefix.toString)))
//      val scanRst = sc.hbase[String]("machineLearn2012", Set("location", "username"), scan)
//      scanRst.map({ case (k, v) =>
//        val uid = k;
//        val locationsAtDifferentLevel = v("location")
//        val username = v("username").map({case(col, nameBytes) =>
//          (col, Bytes.toString(nameBytes).toUpperCase)
//        })
//
////        val record = locationsAtDifferentLevel.map({ case (precision, jsonBytes) =>
////
////          val jsonArr = new JSONArray(Bytes.toString(jsonBytes))
////          val x = jsonArr.getDouble(0)
////          val y = jsonArr.getDouble(1)
////
////          var key = "Race_State"
////          var shapeDataStore:ShapefileDataStore = stateDataStore
////          var featureName = stateFeature
////          if (precision.equals("precise")) {
////            key = "Race_Tract"
////            shapeDataStore = tractDataStore
////            featureName = tractFeature
////          } else if (precision. equals("city")){
////            key = "Race_County"
////            shapeDataStore = countyDataStore
////            featureName = countyFeature
////          } else if (precision.equals("admin")){
////            key = "Race_State"
////            shapeDataStore = stateDataStore
////            featureName = stateFeature
////          }
////
////          val raceProbMap = getRaceProbability(shapeDataStore, x, y, featureName, raceNames.map(_._2))
////          val snProbMap = getSurnameProbability(df, username.getOrElse("lastName", ""))
////
////          val finalProbMap = raceProbMap.map({case(k,v)=>
////            var snProb = snProbMap.getOrElse(k, 0.0d)
////            if (snProb == 0.0d){
////              snProb = 0.01d
////            }
////
////            var racProb = v;
////            if (racProb == 0.0d) {
////              racProb = 0.01d
////            }
////
////            val compoundProb = racProb * snProb;
////            (k, compoundProb)
////          })
////
////          val denominator = finalProbMap.map(_._2).sum
////
////          key -> finalProbMap
////            .map({case(fieldname, numerator) =>
////              var prob = numerator;
////              if (denominator != 0.0d){
////                prob = numerator/denominator
////              }
////              fieldname -> Bytes.toBytes(prob)
////            })
////        })
////        uid -> record
//      }).toHBase("machineLearn2012")
      startRowPrefix += 1;
    }
  }

}
