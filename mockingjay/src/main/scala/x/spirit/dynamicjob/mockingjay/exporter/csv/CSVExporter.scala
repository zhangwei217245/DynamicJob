package x.spirit.dynamicjob.mockingjay.exporter.csv

import org.apache.hadoop.hbase.client.Scan
import org.apache.hadoop.hbase.filter.{BinaryComparator, CompareFilter, PrefixFilter, SingleColumnValueFilter}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.json.{JSONArray, JSONException}
import x.spirit.dynamicjob.mockingjay.hbase.{HBaseConfig, _}
import x.spirit.dynamicjob.mockingjay.twitteruser.PoliticalPreference

/**
  * Created by zhangwei on 11/21/16.
  */
object CSVExporter extends App{


  override def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName(this.getClass.getSimpleName)
      .set("spark.kryoserializer.buffer.max","2000")
    val sc = new SparkContext(sparkConf)

    implicit val config = HBaseConfig(
      hbaseXmlConfigFile = "hbase-site.xml"
    )
    config.get.set("hbase.rpc.timeout", "18000000")

    val sqlContext = new SQLContext(sc)


    val headers = Array(
      "uid",
      "username:firstName",
      "username:lastName",
      "Gender:gender",
      "Gender:female_prob",
      "Gender:male_prob",
      "Gender:year_prob",
      "Age:prob",
      "Age:year",
      "political:sumblue",
      "political:sumred",
      "political:type",
      "location:State_geoid",
      "location:State_name",
      "location:state",
      "Coord:state_WKT",
      "Coord:state_x",
      "Coord:state_y",
      "location:County_geoid",
      "location:County_name",
      "location:county",
      "Coord:county_WKT",
      "Coord:county_x",
      "Coord:county_y",
      "location:Tract_geoid",
      "location:Tract_name",
      "location:tract",
      "Coord:tract_WKT",
      "Coord:tract_x",
      "Coord:tract_y",
      "Race_State:pct2prace",
      "Race_State:pctaian",
      "Race_State:pctapi",
      "Race_State:pctblack",
      "Race_State:pcthispanic",
      "Race_State:pctwhite",
      "Race_County:pct2prace",
      "Race_County:pctaian",
      "Race_County:pctapi",
      "Race_County:pctblack",
      "Race_County:pcthispanic",
      "Race_County:pctwhite",
      "Race_Tract:pct2prace",
      "Race_Tract:pctaian",
      "Race_Tract:pctapi",
      "Race_Tract:pctblack",
      "Race_Tract:pcthispanic",
      "Race_Tract:pctwhite"
    )

    val headerStr = headers.map({row=> "\"%s\"".format(row)}).mkString(",")

    // READ state.csv
    val statePath = "hdfs://geotwitter.ttu.edu:54310/user/hadoopuser/geotwitterCSV/state.csv"

    val stateName = "NAME10"

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

    val dataFrame = sqlContext.read.format("com.databricks.spark.csv").option("header", "true")
      .schema(stateSchema).load(statePath)

    val allStates = dataFrame.select(stateName).map(_.getAs[String](stateName)).collect()

    for (state:String <- allStates) {
      val scan = new Scan()
      scan.setCaching(100)
      scan.setCacheBlocks(true)
      scan.setAttribute(Scan.HINT_LOOKAHEAD, Bytes.toBytes(2))
      val filter = new SingleColumnValueFilter(
        Bytes.toBytes("location"),
        Bytes.toBytes("State_name"),
        CompareFilter.CompareOp.EQUAL,
        Bytes.toBytes(state)
      )
      filter.setFilterIfMissing(true)
      filter.setLatestVersionOnly(true)
      scan.setFilter(filter)
      val columnFamilies = Set(
        "location", "political",
        "Age", "Gender",
        "Race_Tract", "Race_County", "Race_State",
        "username"
      )
      val scanRst = sc.hbase[Array[Byte]]("machineLearn2012", columnFamilies , scan)

      val dfBaseRdd = scanRst.map({bigRow=>
        val uid = bigRow._1

        var x_state:Double = 0.0d
        var y_state:Double = 0.0d

        var x_county:Double = 0.0d
        var y_county:Double = 0.0d

        var x_tract:Double = 0.0d
        var y_tract:Double = 0.0d

        val cfMap = bigRow._2 .map({cfPair =>
          val cfName = cfPair._1

          val colMap = cfPair._2.map({colPair =>
            var colName:String = "%s:%s".format(cfName, colPair._1)

            var value:String = ""
            if (cfName.equalsIgnoreCase("political")){
              value = Bytes.toInt(colPair._2).toString
              if (colName.equalsIgnoreCase("political:type")) {
                value = "\"%s\"".format(PoliticalPreference(Bytes.toInt(colPair._2)).toString)
              }
            } else if (cfName.startsWith("Race_")){
              if (colPair._1.equalsIgnoreCase("max_race")){
                value = "\"%s\"".format(Bytes.toString(colPair._2))
              } else {
                value = Bytes.toDouble(colPair._2).toString
              }

              try {
                val prec_json = Bytes.toString(cfPair._2("precise"))
                val city_json = Bytes.toString(cfPair._2("city"))
                val admin_json = Bytes.toString(cfPair._2("admin"))
                if (cfName.endsWith("Tract") && prec_json.equalsIgnoreCase("[]")) {
                  value = (-99999.0d).toString
                }
                if (cfName.endsWith("County") && city_json.equalsIgnoreCase("[]")) {
                  value = (-99999.0d).toString
                }
                if (cfName.endsWith("State") && admin_json.equalsIgnoreCase("[]")) {
                  value = (-99999.0d).toString
                }
              } catch {
                case t: Throwable =>
                  println("uid : %s , fail to filter out inexist level of coordinates: %s"
                    .format(uid, value))
              }
            } else if (colPair._1.endsWith("prob")){
               value = Bytes.toDouble(colPair._2).toString
            } else if (colPair._1.equalsIgnoreCase("year")){
              value = Bytes.toLong(colPair._2).toString
            } else {
              value = Bytes.toString(colPair._2)
              if (colName.equalsIgnoreCase("location:admin")){
                try {
                  val jsonArr = new JSONArray(value)
                  x_state = jsonArr.getDouble(0)
                  y_state = jsonArr.getDouble(1)
                } catch {
                  case jsone: JSONException =>
                    println("uid : %s , coord: %s"
                      .format(uid, value))
                }
                colName = "location:state"

              } else if (colName.equalsIgnoreCase("location:city")){
                try {
                  val jsonArr = new JSONArray(value)
                  x_county = jsonArr.getDouble(0)
                  y_county = jsonArr.getDouble(1)
                } catch {
                  case jsone: JSONException =>
                    println("uid : %s , coord: %s"
                      .format(uid, value))
                }
                colName = "location:county"

              } else if (colName.equalsIgnoreCase("location:precise")) {
                try {
                  val jsonArr = new JSONArray(value)
                  x_tract = jsonArr.getDouble(0)
                  y_tract = jsonArr.getDouble(1)
                } catch {
                  case jsone: JSONException =>
                    println("uid : %s , coord: %s"
                      .format(uid, value))
                }
                colName = "location:tract"
              }
              value = "\"%s\"".format(value.replace("\"","\"\""))
            }
            colName -> value
          })

          cfName -> colMap
        })

        val tmpMap = cfMap.flatMap(_._2) ++ Map[String, String](
          "Coord:tract_WKT" -> "\"POINT(%1$.10f %2$.10f)\"".format(x_tract, y_tract),
          "Coord:tract_x" -> "%1$.10f".format(x_tract),
          "Coord:tract_y" -> "%1$.10f".format(y_tract),
          "Coord:state_WKT" -> "\"POINT(%1$.10f %2$.10f)\"".format(x_state, y_state),
          "Coord:state_x" -> "%1$.10f".format(x_state),
          "Coord:state_y" -> "%1$.10f".format(y_state),
          "Coord:county_WKT" -> "\"POINT(%1$.10f %2$.10f)\"".format(x_county, y_county),
          "Coord:county_x" -> "%1$.10f".format(x_county),
          "Coord:county_y" -> "%1$.10f".format(y_county),
          "uid" -> uid
        )
        headers.map({row => tmpMap.getOrElse(row,"")}).mkString(",")
      })

      val headerRDD = sc.makeRDD(Seq[String](headerStr))

      headerRDD.union(dfBaseRdd).saveAsTextFile("hdfs://geotwitter.ttu.edu:54310/user/hadoopuser/geotwitterOutput/csv/%s.csv"
        .format(state.replace(" ","_")))

    }

  }

}
