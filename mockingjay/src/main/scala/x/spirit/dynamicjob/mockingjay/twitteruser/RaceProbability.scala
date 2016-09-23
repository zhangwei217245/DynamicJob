package x.spirit.dynamicjob.mockingjay.twitteruser

import java.io.File

import org.apache.hadoop.hbase.client.Scan
import org.apache.hadoop.hbase.filter.PrefixFilter
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.{SparkConf, SparkContext}
import org.geotools.data.shapefile.ShapefileDataStore
import org.geotools.filter.text.cql2.CQL
import org.json.JSONArray
import org.opengis.filter.Filter
import x.spirit.dynamicjob.core.utils.ShapeFileUtils
import x.spirit.dynamicjob.mockingjay.hbase.{HBaseConfig, _}

import scala.collection.mutable

/**
  * Created by zhangwei on 9/16/16.
  */
object RaceProbability extends App{


  val raceNames = Array(
    ("total" , "DP0110001"),
    ("pcthispanic", "DP0110002"),
    ("pctwhite", "DP0110011"),
    ("pctblack", "DP0110012"),
    ("pctaian", "DP0110013"),
    ("pctapi1", "DP0110014"),
    ("pctapi2", "DP0110015"),
    ("pct2prace", "DP0110017")
  )


  def getRaceProbability(dataStore: ShapefileDataStore, x_coord: Double, y_coord: Double,
                         featureTypeName: String, attrNames: Array[String]): Map[String, Double] = {
    val gisFilter: Filter = CQL.toFilter("CONTAINS(the_geom, POINT(%1$.10f %2$.10f))".format(x_coord, y_coord))
    val attrValues = ShapeFileUtils.getAttribute(dataStore, gisFilter, featureTypeName, attrNames)
    val totalPopulation = Int.unbox(attrValues.get(0)).toDouble;
    val result : scala.collection.mutable.Map[String, Double] = mutable.Map()
    for (i <- 1 until attrValues.size()) {
      val raceProbability = ((Int.unbox(attrValues.get(i)) * 100).toDouble / totalPopulation)
      result+=((raceNames(i)._1, raceProbability))
    }
    result.toMap
  }

  override def main(args: Array[String]){
    val sparkConf = new SparkConf().setAppName("ResidencyLocator")
    val sc = new SparkContext(sparkConf)

    implicit val config = HBaseConfig(
      hbaseXmlConfigFile = "hbase-site.xml"
    )
    config.get.set("hbase.rpc.timeout", "18000000")

    val attrNames: Array[String] = new Array[String](19)
    for (n <- 1 to 19) {
      val base: Int = 80000 + n
      attrNames(n - 1) = "DP00" + base
    }

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

          val record = locationsAtDifferentLevel.map({ case (precision, jsonBytes) =>

            val jsonArr = new JSONArray(Bytes.toString(jsonBytes))
            val x = jsonArr.getDouble(0)
            val y = jsonArr.getDouble(1)

            var key = "Race_State"
            var shapeDataStore:ShapefileDataStore = stateDataStore
            var featureName = stateFeature
            if (precision.equals("precise")) {
              key = "Race_Tract"
              shapeDataStore = tractDataStore
              featureName = tractFeature
            } else if (precision. equals("city")){
              key = "Race_County"
              shapeDataStore = countyDataStore
              featureName = countyFeature
            } else if (precision.equals("admin")){
              key = "Race_State"
              shapeDataStore = stateDataStore
              featureName = stateFeature
            }

            key -> getRaceProbability(shapeDataStore, x, y, featureName, raceNames.map(_._2)).map({case(fieldname, percentage) =>
              fieldname -> Bytes.toBytes(percentage)
            })
          })
          uid -> record
      }).toHBase("machineLearn2012")
      startRowPrefix += 1;
    }
  }
}
