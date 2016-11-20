package x.spirit.dynamicjob.mockingjay.twitteruser

import scala.util.control.Breaks._
import geotrellis.vector.io.wkt.WKT
import org.apache.hadoop.hbase.client.Scan
import org.apache.hadoop.hbase.filter.PrefixFilter
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.graphx.Graph
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.types._
import org.apache.spark.{SparkConf, SparkContext}
import org.json.{JSONArray, JSONException}
import x.spirit.dynamicjob.mockingjay.hbase.{HBaseConfig, _}
import x.spirit.dynamicjob.mockingjay.spatial.{QuadTreeIndex, ShapeRecord}

/**
  * Created by zhangwei on 11/19/16.
  */
object RaceProbabilityWithCSV extends App {


  def getSurnameProbability(data: collection.Map[String, Array[Double]], surname: String): Map[String, Double] = {
    val pctRst: Array[Double] = data.getOrElse(surname.toUpperCase, Array(0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d))
    Map[String, Double](
      "pctwhite" -> pctRst(0),
      "pctblack" -> pctRst(1),
      "pctapi" -> pctRst(2),
      "pctaian" -> pctRst(3),
      "pcthispanic" -> pctRst(4),
      "pct2prace" -> pctRst(5)
    )
  }


  override def main(args: Array[String]) {

    var startRowPrefix = 10
    var keySuffix = "State"
    if (args.length >= 2) {
      startRowPrefix = args(0).toInt
      keySuffix = args(1).toString.toLowerCase
      keySuffix = keySuffix.take(1).toUpperCase+keySuffix.drop(1).toLowerCase
    }
    val sparkConf = new SparkConf().setAppName(this.getClass.getSimpleName)
        .set("spark.kryoserializer.buffer.max","2000")
    val sc = new SparkContext(sparkConf)

    implicit val config = HBaseConfig(
      hbaseXmlConfigFile = "hbase-site.xml"
    )
    config.get.set("hbase.rpc.timeout", "18000000")

    val sqlContext = new SQLContext(sc)

    // READ surname.csv
    val surnamePath = "hdfs://geotwitter.ttu.edu:54310/user/hadoopuser/geotwitterCSV/surname.csv"

    val customSchema = StructType(Array(
      StructField("name", StringType),
      StructField("rank", IntegerType),
      StructField("count", IntegerType),
      StructField("prop100k", DoubleType),
      StructField("cum_prop100k", DoubleType),
      StructField("pctwhite", DoubleType),
      StructField("pctblack", DoubleType),
      StructField("pctapi", DoubleType),
      StructField("pctaian", DoubleType),
      StructField("pct2prace", DoubleType),
      StructField("pcthispanic", DoubleType)
    ))

    val surnameDF = sqlContext.read.format("com.databricks.spark.csv").option("header", "true").schema(customSchema).load(surnamePath)

    val surnameMap = surnameDF.map({ row =>
      val name = row.getAs[String]("name").toString
      val pctwhite = row.getAs[Double]("pctwhite")
      val pctblack = row.getAs[Double]("pctblack")
      val pctapi = row.getAs[Double]("pctapi")
      val pctaian = row.getAs[Double]("pctaian")
      val pct2prace = row.getAs[Double]("pct2prace")
      val pcthispanic = row.getAs[Double]("pcthispanic")
      name -> Array(pctwhite, pctblack, pctapi, pctaian, pcthispanic, pct2prace)
    }).collectAsMap()


    // READ tract.csv
    val tractPath = "hdfs://geotwitter.ttu.edu:54310/user/hadoopuser/geotwitterCSV/tract.csv"

    val tractFieldName = Array("WKT","GEOID10","NAMELSAD10","ALAND10","AWATER10","INTPTLAT10","INTPTLON10",
      "DP0010001","DP0010002","DP0010003","DP0010004","DP0010005","DP0010006","DP0010007","DP0010008",
      "DP0010009","DP0010010","DP0010011","DP0010012","DP0010013","DP0010014","DP0010015","DP0010016",
      "DP0010017","DP0010018","DP0010019","DP0010020","DP0010021","DP0010022","DP0010023","DP0010024",
      "DP0010025","DP0010026","DP0010027","DP0010028","DP0010029","DP0010030","DP0010031","DP0010032",
      "DP0010033","DP0010034","DP0010035","DP0010036","DP0010037","DP0010038","DP0010039","DP0010040",
      "DP0010041","DP0010042","DP0010043","DP0010044","DP0010045","DP0010046","DP0010047","DP0010048",
      "DP0010049","DP0010050","DP0010051","DP0010052","DP0010053","DP0010054","DP0010055","DP0010056",
      "DP0010057","DP0020001","DP0020002","DP0020003","DP0030001","DP0030002","DP0030003","DP0040001",
      "DP0040002","DP0040003","DP0050001","DP0050002","DP0050003","DP0060001","DP0060002","DP0060003",
      "DP0070001","DP0070002","DP0070003","DP0080001","DP0080002","DP0080003","DP0080004","DP0080005",
      "DP0080006","DP0080007","DP0080008","DP0080009","DP0080010","DP0080011","DP0080012","DP0080013",
      "DP0080014","DP0080015","DP0080016","DP0080017","DP0080018","DP0080019","DP0080020","DP0080021",
      "DP0080022","DP0080023","DP0080024","DP0090001","DP0090002","DP0090003","DP0090004","DP0090005",
      "DP0090006","DP0100001","DP0100002","DP0100003","DP0100004","DP0100005","DP0100006","DP0100007",
      "DP0110001","DP0110002","DP0110003","DP0110004","DP0110005","DP0110006","DP0110007","DP0110008",
      "DP0110009","DP0110010","DP0110011","DP0110012","DP0110013","DP0110014","DP0110015","DP0110016",
      "DP0110017","DP0120001","DP0120002","DP0120003","DP0120004","DP0120005","DP0120006","DP0120007",
      "DP0120008","DP0120009","DP0120010","DP0120011","DP0120012","DP0120013","DP0120014","DP0120015",
      "DP0120016","DP0120017","DP0120018","DP0120019","DP0120020","DP0130001","DP0130002","DP0130003",
      "DP0130004","DP0130005","DP0130006","DP0130007","DP0130008","DP0130009","DP0130010","DP0130011",
      "DP0130012","DP0130013","DP0130014","DP0130015","DP0140001","DP0150001","DP0160001","DP0170001",
      "DP0180001","DP0180002","DP0180003","DP0180004","DP0180005","DP0180006","DP0180007","DP0180008",
      "DP0180009","DP0190001","DP0200001","DP0210001","DP0210002","DP0210003","DP0220001","DP0220002",
      "DP0230001","DP0230002","Shape_Leng","Shape_Area")

    val tractSchema = StructType(
      tractFieldName.take(3).map({str=>
      StructField(str,StringType)
    })++:tractFieldName.drop(3).map({str=>
      StructField(str,DoubleType)
    }))


    // READ county.csv
    val countyPath = "hdfs://geotwitter.ttu.edu:54310/user/hadoopuser/geotwitterCSV/county.csv"

    val countyFieldName = Array("WKT","GEOID10","NAMELSAD10","FUNCSTAT10","ALAND10","AWATER10","INTPTLAT10","INTPTLON10",
      "DP0010001","DP0010002","DP0010003","DP0010004","DP0010005","DP0010006","DP0010007","DP0010008","DP0010009",
      "DP0010010","DP0010011","DP0010012","DP0010013","DP0010014","DP0010015","DP0010016","DP0010017","DP0010018",
      "DP0010019","DP0010020","DP0010021","DP0010022","DP0010023","DP0010024","DP0010025","DP0010026","DP0010027",
      "DP0010028","DP0010029","DP0010030","DP0010031","DP0010032","DP0010033","DP0010034","DP0010035","DP0010036",
      "DP0010037","DP0010038","DP0010039","DP0010040","DP0010041","DP0010042","DP0010043","DP0010044","DP0010045",
      "DP0010046","DP0010047","DP0010048","DP0010049","DP0010050","DP0010051","DP0010052","DP0010053","DP0010054",
      "DP0010055","DP0010056","DP0010057","DP0020001","DP0020002","DP0020003","DP0030001","DP0030002","DP0030003",
      "DP0040001","DP0040002","DP0040003","DP0050001","DP0050002","DP0050003","DP0060001","DP0060002","DP0060003",
      "DP0070001","DP0070002","DP0070003","DP0080001","DP0080002","DP0080003","DP0080004","DP0080005","DP0080006",
      "DP0080007","DP0080008","DP0080009","DP0080010","DP0080011","DP0080012","DP0080013","DP0080014","DP0080015",
      "DP0080016","DP0080017","DP0080018","DP0080019","DP0080020","DP0080021","DP0080022","DP0080023","DP0080024",
      "DP0090001","DP0090002","DP0090003","DP0090004","DP0090005","DP0090006","DP0100001","DP0100002","DP0100003",
      "DP0100004","DP0100005","DP0100006","DP0100007","DP0110001","DP0110002","DP0110003","DP0110004","DP0110005",
      "DP0110006","DP0110007","DP0110008","DP0110009","DP0110010","DP0110011","DP0110012","DP0110013","DP0110014",
      "DP0110015","DP0110016","DP0110017","DP0120001","DP0120002","DP0120003","DP0120004","DP0120005","DP0120006",
      "DP0120007","DP0120008","DP0120009","DP0120010","DP0120011","DP0120012","DP0120013","DP0120014","DP0120015",
      "DP0120016","DP0120017","DP0120018","DP0120019","DP0120020","DP0130001","DP0130002","DP0130003","DP0130004",
      "DP0130005","DP0130006","DP0130007","DP0130008","DP0130009","DP0130010","DP0130011","DP0130012","DP0130013",
      "DP0130014","DP0130015","DP0140001","DP0150001","DP0160001","DP0170001","DP0180001","DP0180002","DP0180003",
      "DP0180004","DP0180005","DP0180006","DP0180007","DP0180008","DP0180009","DP0190001","DP0200001","DP0210001",
      "DP0210002","DP0210003","DP0220001","DP0220002","DP0230001","DP0230002","Shape_Leng","Shape_Area")

    val countySchema = StructType(
      countyFieldName.take(4).map({str=>
      StructField(str,StringType)
    })++:countyFieldName.drop(4).map({str=>
      StructField(str,DoubleType)
    }))


    // READ state.csv
    val statePath = "hdfs://geotwitter.ttu.edu:54310/user/hadoopuser/geotwitterCSV/state.csv"

    val stateFieldName = Array("WKT","GEOID10","STUSPS10","NAME10","ALAND10","AWATER10","INTPTLAT10","INTPTLON10",
      "DP0010001","DP0010002","DP0010003","DP0010004","DP0010005","DP0010006","DP0010007","DP0010008","DP0010009",
      "DP0010010","DP0010011","DP0010012","DP0010013","DP0010014","DP0010015","DP0010016","DP0010017","DP0010018",
      "DP0010019","DP0010020","DP0010021","DP0010022","DP0010023","DP0010024","DP0010025","DP0010026","DP0010027",
      "DP0010028","DP0010029","DP0010030","DP0010031","DP0010032","DP0010033","DP0010034","DP0010035","DP0010036",
      "DP0010037","DP0010038","DP0010039","DP0010040","DP0010041","DP0010042","DP0010043","DP0010044","DP0010045",
      "DP0010046","DP0010047","DP0010048","DP0010049","DP0010050","DP0010051","DP0010052","DP0010053","DP0010054",
      "DP0010055","DP0010056","DP0010057","DP0020001","DP0020002","DP0020003","DP0030001","DP0030002","DP0030003",
      "DP0040001","DP0040002","DP0040003","DP0050001","DP0050002","DP0050003","DP0060001","DP0060002","DP0060003",
      "DP0070001","DP0070002","DP0070003","DP0080001","DP0080002","DP0080003","DP0080004","DP0080005","DP0080006",
      "DP0080007","DP0080008","DP0080009","DP0080010","DP0080011","DP0080012","DP0080013","DP0080014","DP0080015",
      "DP0080016","DP0080017","DP0080018","DP0080019","DP0080020","DP0080021","DP0080022","DP0080023","DP0080024",
      "DP0090001","DP0090002","DP0090003","DP0090004","DP0090005","DP0090006","DP0100001","DP0100002","DP0100003",
      "DP0100004","DP0100005","DP0100006","DP0100007","DP0110001","DP0110002","DP0110003","DP0110004","DP0110005",
      "DP0110006","DP0110007","DP0110008","DP0110009","DP0110010","DP0110011","DP0110012","DP0110013","DP0110014",
      "DP0110015","DP0110016","DP0110017","DP0120001","DP0120002","DP0120003","DP0120004","DP0120005","DP0120006",
      "DP0120007","DP0120008","DP0120009","DP0120010","DP0120011","DP0120012","DP0120013","DP0120014","DP0120015",
      "DP0120016","DP0120017","DP0120018","DP0120019","DP0120020","DP0130001","DP0130002","DP0130003","DP0130004",
      "DP0130005","DP0130006","DP0130007","DP0130008","DP0130009","DP0130010","DP0130011","DP0130012","DP0130013",
      "DP0130014","DP0130015","DP0140001","DP0150001","DP0160001","DP0170001","DP0180001","DP0180002","DP0180003",
      "DP0180004","DP0180005","DP0180006","DP0180007","DP0180008","DP0180009","DP0190001","DP0200001","DP0210001",
      "DP0210002","DP0210003","DP0220001","DP0220002","DP0230001","DP0230002","Shape_Leng","Shape_Area")

    val stateSchema = StructType(
      stateFieldName.take(4).map({str=>
      StructField(str,StringType)
    })++:stateFieldName.drop(4).map({str=>
      StructField(str,DoubleType)
    }))

    val key = "Race_%s".format(keySuffix)

    var precisionStr = "admin"
    var schema = stateSchema
    var csvPath = statePath

    if (keySuffix.equalsIgnoreCase("state")){
      schema = stateSchema
      csvPath = statePath
      precisionStr = "admin"
    } else if (keySuffix.equalsIgnoreCase("county")){
      schema = countySchema
      csvPath = countyPath
      precisionStr = "city"
    } else if (keySuffix.equalsIgnoreCase("tract")){
      schema = tractSchema
      csvPath = tractPath
      precisionStr = "precise"
    }


    val dataFrame = sqlContext.read.format("com.databricks.spark.csv").option("header", "true").schema(schema).load(csvPath)

    val quadTree : QuadTreeIndex[(Double,Double,String)] = new QuadTreeIndex[(Double,Double,String)](
      xmin = -179.23108599999995, xmax = 179.85968100000002, ymin=17.83150900000004, ymax = 71.44105900000005
    )
    // Do a for each loop to generate objects and create tree index
    val shapeRecordPairRDD = dataFrame.select("WKT","GEOID10",
      "DP0110001",
      "DP0110002",
      "DP0110011",
      "DP0110012",
      "DP0110013",
      "DP0110014",
      "DP0110015",
      "DP0110017").map({row =>

      val WKTString = row.getAs[String]("WKT").toString
      val GEOID10 = row.getAs[String]("GEOID10").toString
      val total = row.getAs[Double]("DP0110001")
      val pcthispanic = 100.0 * row.getAs[Double]("DP0110002")/total
      val pctwhite = 100.0 * row.getAs[Double]("DP0110011")/total
      val pctblack = 100.0 * row.getAs[Double]("DP0110012")/total
      val pctaian = 100.0 * row.getAs[Double]("DP0110013")/total
      val pctapi = 100.0 * (row.getAs[Double]("DP0110014")+row.getAs[Double]("DP0110015"))/total
      val pct2prace = 100.0 * row.getAs[Double]("DP0110017")/total
      val geom = WKT.read(WKTString)
      val dataFields = Map[String, Double](
        "pctwhite" -> pctwhite,
        "pctblack" -> pctblack,
        "pctapi" -> pctapi,
        "pctaian" -> pctaian,
        "pcthispanic" -> pcthispanic,
        "pct2prace" -> pct2prace
      )
      val sr = new ShapeRecord[Double](geom, dataFields, GEOID10)
      GEOID10 -> sr
    })


    shapeRecordPairRDD.map({case (geoID, shapeRecord)=>
        val centroid = shapeRecord.getCentroidCoordinates
      (centroid._1,centroid._2,geoID)
    }).collect().foreach({
      case tuple =>
      quadTree.add(tuple)
    })

    while (startRowPrefix <= 99) {
      println("Start row prefix = %d".format(startRowPrefix))
      val scan = new Scan()
      scan.setCaching(100)
      scan.setCacheBlocks(true)
      scan.setAttribute(Scan.HINT_LOOKAHEAD, Bytes.toBytes(2))
      scan.setFilter(new PrefixFilter(Bytes.toBytes(startRowPrefix.toString)))
      val scanRst = sc.hbase[String]("machineLearn2012", Set("location", "username"), scan)
      val userProbsArray = scanRst.collect().map({ case (k, v) =>

        val uid = k
        val locationsAtDifferentLevel = v("location")
        val username = v("username").map({ case (col, nameBytes) =>
          (col, Bytes.toString(nameBytes).toUpperCase)
        })

        val record = locationsAtDifferentLevel.filter(_._1.equalsIgnoreCase(precisionStr)).map({ case (precision, jsonBytes) =>

          val snProbMap = getSurnameProbability(surnameMap, username.getOrElse("lastName", ""))

          var finalProbMap = snProbMap

          try {
            val jsonArr = new JSONArray(Bytes.toString(jsonBytes))

            val x = jsonArr.getDouble(0)
            val y = jsonArr.getDouble(1)

            var raceProbMap : Map[String,Double] = Map[String,Double]()
            quadTree.searchByCoordinates(x,y).foreach({tuple=>
              val lookupRst = shapeRecordPairRDD.lookup(tuple._3)
              if (lookupRst.nonEmpty && raceProbMap == null){
                raceProbMap = lookupRst.head.getDataFields
                println("ShapeRecord found in the QuadTree for (%f, %f) : %s".format(tuple._1,tuple._2,raceProbMap))
              }
            })

            if (raceProbMap.isEmpty){
              shapeRecordPairRDD.filter({pair=>pair._2.covers(x, y)}).collect().foreach({pair =>
                raceProbMap = pair._2.getDataFields
                println("ShapeRecord Found outside of QuadTree for (%f, %f) : %s".format(x, y, raceProbMap))
              })
            }

            if (raceProbMap.nonEmpty) {
              finalProbMap = raceProbMap.map({ case (kk, vv) =>
                var snProb = snProbMap.getOrElse(kk, 0.0d)
                if (snProb == 0.0d) {
                  snProb = 0.01d
                }

                var racProb = vv
                if (racProb == 0.0d) {
                  racProb = 0.01d
                }

                val compoundProb = racProb * snProb
                (kk, compoundProb)
              })
            }
          } catch {
            case jsone: JSONException =>
              println("uid : %s , coord: %s"
                .format(uid, Bytes.toString(jsonBytes)))
          }

          val denominator = finalProbMap.values.sum

          key -> finalProbMap
            .map({ case (fieldname, numerator) =>
              var prob = numerator
              if (denominator != 0.0d) {
                prob = numerator / denominator
              }
              fieldname -> Bytes.toBytes(prob)
            })
        })
        uid -> record
      })

      sc.parallelize(userProbsArray).toHBase("machineLearn2012")

      startRowPrefix += 1
    }
  }
}