package x.spirit.dynamicjob.mockingjay.twitteruser

import java.io.File
import java.time.{Clock, Duration, Instant}
import java.util.Collections

import org.apache.hadoop.hbase.client.Scan
import org.apache.hadoop.hbase.filter.PrefixFilter
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.types._
import org.apache.spark.{SparkConf, SparkContext}
import org.geotools.filter.text.cql2.CQL
import org.json.{JSONArray, JSONException}
import org.opengis.filter.Filter
import x.spirit.dynamicjob.core.utils.ShapeFileUtils
import x.spirit.dynamicjob.mockingjay.hbase.{HBaseConfig, _}
import x.spirit.dynamicjob.shapefile.datastore.SerializableShapeFileStore

import scala.collection.mutable

/**
 * DEPRECATED!!!!
 * Notice: This piece of code is already DPRECATED!!!
 * Please use RaceProbabilityWithCSV instead.
 * Created by zhangwei on 9/16/16.
 */
object RaceProbability extends App {


  def getSurnameProbability(data: collection.Map[String, Array[Double]], surname: String): Map[String, Double] = {
    val pctRst: Array[Double] = data.getOrElse(surname.toUpperCase, Array(0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d))
    return Map[String, Double](
      ("pctwhite" -> pctRst(0)),
      ("pctblack" -> pctRst(1)),
      ("pctapi" -> pctRst(2)),
      ("pctaian" -> pctRst(3)),
      ("pcthispanic" -> pctRst(4)),
      ("pct2prace" -> pctRst(5))
    )
  }

  def getRaceProbabilityFromShapeFile(dataStore: SerializableShapeFileStore, x_coord: Double, y_coord: Double,
                                      featureTypeName: String, attrNames: Array[(String, String)]): Map[String, Double] = {
    val gisFilter: Filter = CQL.toFilter("CONTAINS(the_geom, POINT(%1$.10f %2$.10f))".format(x_coord, y_coord))
    //println("func: getRaceProbability -> attrNames.length = "+ attrNames.map(_._2).length)
    val t1 = Instant.now()
    val attrValues = ShapeFileUtils.getAttribute(dataStore, gisFilter, featureTypeName, attrNames.map(_._2))
    val t2 = Instant.now()
    println("Get Attribuites from Shapefile after %dms".format(Duration.between(t1, t2).toMillis))

    val result: scala.collection.mutable.Map[String, Double] = mutable.Map()
    if (attrValues.isEmpty){
        return result.toMap
    }
    val totalPopulation = Int.unbox(attrValues.get(0)).toDouble;

    for (i <- 1 until attrValues.size()) {
      val raceProbability = ((Int.unbox(attrValues.get(i)) * 100).toDouble / totalPopulation)
      result += ((attrNames(i)._1, raceProbability))
    }
    result += (("pctapi", (result.getOrElse("pctapi1", 0.0d) + result.getOrElse("pctapi2", 0.0d))))
    return result.filterKeys({ k => (!(k.equalsIgnoreCase("pctapi1") || k.equalsIgnoreCase("pctapi2"))) }).toMap
  }


  override def main(args: Array[String]) {

    var startRowPrefix = 23
    if (args.length >= 1) {
      startRowPrefix = args(0).toInt;
    }
    val sparkConf = new SparkConf().setAppName(this.getClass.getSimpleName)
    val sc = new SparkContext(sparkConf)

    implicit val config = HBaseConfig(
      hbaseXmlConfigFile = "hbase-site.xml"
    )
    config.get.set("hbase.rpc.timeout", "18000000")

    val surnamePath = "hdfs://geotwitter.ttu.edu:54310/user/hadoopuser/geotwitterCSV/surname.csv"

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

    val surnameDF = sqlContext.read.format("com.databricks.spark.csv").option("header", "true").schema(customSchema).load(surnamePath)


    val surnameMap = surnameDF.map({ case row =>
      val name = row.getAs[String]("name").toString
      val pctwhite = row.getAs[Double]("pctwhite")
      val pctblack = row.getAs[Double]("pctblack")
      val pctapi = row.getAs[Double]("pctapi")
      val pctaian = row.getAs[Double]("pctaian")
      val pct2prace = row.getAs[Double]("pct2prace")
      val pcthispanic = row.getAs[Double]("pcthispanic")
      (name -> Array(pctwhite, pctblack, pctapi, pctaian, pcthispanic, pct2prace))
    }).collectAsMap();


    while (startRowPrefix <= 99) {
      println("Start row prefix = %d".format(startRowPrefix))
      val scan = new Scan()
      scan.setCaching(100)
      scan.setCacheBlocks(true)
      scan.setAttribute(Scan.HINT_LOOKAHEAD, Bytes.toBytes(2))
      scan.setFilter(new PrefixFilter(Bytes.toBytes(startRowPrefix.toString)))
      val scanRst = sc.hbase[String]("machineLearn2012", Set("location", "username"), scan)
      scanRst.map({ case (k, v) =>

        val uid = k;
        val locationsAtDifferentLevel = v("location")
        val username = v("username").map({ case (col, nameBytes) =>
          (col, Bytes.toString(nameBytes).toUpperCase)
        })

        val record = locationsAtDifferentLevel.map({ case (precision, jsonBytes) =>

          val snProbMap = getSurnameProbability(surnameMap, username.getOrElse("lastName", ""))

          var finalProbMap = snProbMap;

          val shapeFileRootDir = "/home/hadoopuser/shapefiles";
          val shapeFileAddrTemplate = "%s/%s/%s.shp"

          val tractFeature = "Tract_2010Census_DP1"
          val countyFeature = "County_2010Census_DP1"
          val stateFeature = "State_2010Census_DP1"

          val tractFileName = shapeFileAddrTemplate.format(shapeFileRootDir, tractFeature, tractFeature)
          val countyFileName = shapeFileAddrTemplate.format(shapeFileRootDir, countyFeature, countyFeature)
          val stateFileName = shapeFileAddrTemplate.format(shapeFileRootDir, stateFeature, stateFeature)

          val tractDataStore: SerializableShapeFileStore = new SerializableShapeFileStore()
          tractDataStore.init(new File(tractFileName).toURI.toURL)
          val countyDataStore: SerializableShapeFileStore = new SerializableShapeFileStore()
          countyDataStore.init(new File(countyFileName).toURI.toURL)
          val stateDataStore: SerializableShapeFileStore = new SerializableShapeFileStore()
          stateDataStore.init(new File(stateFileName).toURI.toURL)

          var key = "Race_State"
          var shapeDataStore: SerializableShapeFileStore = stateDataStore
          var featureName = stateFeature
          if (precision.equals("precise")) {
            key = "Race_Tract"
            shapeDataStore = tractDataStore
            featureName = tractFeature
          } else if (precision.equals("city")) {
            key = "Race_County"
            shapeDataStore = countyDataStore
            featureName = countyFeature
          } else if (precision.equals("admin")) {
            key = "Race_State"
            shapeDataStore = stateDataStore
            featureName = stateFeature
          }

          try {
            val jsonArr = new JSONArray(Bytes.toString(jsonBytes))

            val x = jsonArr.getDouble(0)
            val y = jsonArr.getDouble(1)

            val raceNames = Array(
              ("total", "DP0110001"),
              ("pcthispanic", "DP0110002"),
              ("pctwhite", "DP0110011"),
              ("pctblack", "DP0110012"),
              ("pctaian", "DP0110013"),
              ("pctapi1", "DP0110014"),
              ("pctapi2", "DP0110015"),
              ("pct2prace", "DP0110017")
            )

            val raceProbMap = getRaceProbabilityFromShapeFile(shapeDataStore, x, y, featureName, raceNames)

            if (!raceProbMap.isEmpty) {
              finalProbMap = raceProbMap.map({ case (k, v) =>
                var snProb = snProbMap.getOrElse(k, 0.0d)
                if (snProb == 0.0d) {
                  snProb = 0.01d
                }

                var racProb = v;
                if (racProb == 0.0d) {
                  racProb = 0.01d
                }

                val compoundProb = racProb * snProb;
                (k, compoundProb)
              })
            }
          } catch {
            case jsone: JSONException => {
                println("uid : %s , coord: %s"
                  .format(uid, Bytes.toString(jsonBytes)));
            }
          }

          val denominator = finalProbMap.map(_._2).sum

          key -> finalProbMap
            .map({ case (fieldname, numerator) =>
              var prob = numerator;
              if (denominator != 0.0d) {
                prob = numerator / denominator
              }
              fieldname -> Bytes.toBytes(prob)
            })

          //key-> raceProbMap

        })
        uid -> record
      }).toHBase("machineLearn2012")
      startRowPrefix += 1;
    }
  }
}
