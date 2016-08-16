package x.spirit.dynamicjob.mockingjay.twitteruser

import java.time.{Instant, ZoneId, ZonedDateTime}

import breeze.linalg.DenseMatrix
import nak.cluster.Kmeans
import org.apache.hadoop.hbase.filter.PrefixFilter
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.{SparkConf, SparkContext}
import org.json.JSONArray
import x.spirit.dynamicjob.mockingjay.cluster.{DBSCAN, GDBSCAN}
import x.spirit.dynamicjob.mockingjay.hbase.{HBaseConfig, _}
import x.spirit.dynamicjob.mockingjay.utils.TimeZoneMapping

/**
  * Created by zhangwei on 8/10/16.
  */
object ResidencyLocator extends App {

  // Please refer to this link: https://www.oreilly.com/ideas/clustering-geolocated-data-using-spark-and-dbscan
  // Since DBSCAN is not provided in nak V1.3, we fall back to adding in this code.
  def dbscan(v: breeze.linalg.DenseMatrix[Double]) = {
    val gdbscan = new GDBSCAN(
      DBSCAN.getNeighbours(epsilon = 0.001, distance = Kmeans.euclideanDistance),
      DBSCAN.isCorePoint(minPoints = 3)
    )
    val cluster = gdbscan cluster v
    val clusterPoints = cluster.map(_.points.map(_.value.toArray)).sortBy(_.size)
    println("clusterPoints = "+clusterPoints)
    clusterPoints.foreach({item => println(item); item.foreach({arritem=> println(arritem);arritem.foreach(println(_))})})
    clusterPoints
  }

  override def main(args: Array[String]) {
    val sparkConf = new SparkConf().setAppName("ResidencyLocator")
    val sc = new SparkContext(sparkConf)

    implicit val config = HBaseConfig(
      hbaseXmlConfigFile = "hbase-site.xml"
    )

    val validPlaceType = Set("exact", "poi", "neighborhood", "city", "admin", "country")
    val precisePlaceType = Set("exact", "poi", "neighborhood")
    /**
      * For all hbase filters, refer to :
      * https://hbase.apache.org/apidocs/org/apache/hadoop/hbase/filter/package-summary.html
      * Here, it's better to use PageFilter and
      */
    var startRowPrefix = 100;
    var allRst: Array[(String, Int)] = Array();
    while (startRowPrefix <= 999) {
      System.out.println("Start row prefix = %d".format(startRowPrefix))
      val scanRst = sc.hbase[String]("twitterUser", Set("tweet", "user"),
        new PrefixFilter(Bytes.toBytes(startRowPrefix.toString)))
      scanRst.map({ case (k, v) =>
        val uid = k
        //FIXME: The default timezone has to be decided.
        val user_time_zone_str = v("user").getOrElse("time_zone", "Central Time (US & Canada)");
        val user_time_zone = ZoneId.of(TimeZoneMapping.getTimeZoneId(user_time_zone_str));
        val tweet = v("tweet")
        val addr = tweet.map({ case (tid, jsonBytes) =>
          val jsonArr = new JSONArray(Bytes.toString(jsonBytes))

          val timestamp = jsonArr.getJSONArray(0).getLong(0)
          val placeType = jsonArr.getJSONArray(1).getString(2)
          val coord_x = jsonArr.getJSONArray(2).getDouble(0)
          val coord_y = jsonArr.getJSONArray(2).getDouble(1)

          val zonedHour = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), user_time_zone).getHour

          (placeType, coord_x, coord_y, zonedHour)
        })

        val adminLocations = addr.filter({
          case (placeType, x, y, zonedHour) => "admin".equalsIgnoreCase(placeType.trim)
        })
        val cityLocations = addr.filter({
          case (placeType, x, y, zonedHour) => "city".equalsIgnoreCase(placeType.trim)
        })
        // TODO: Currently we don't need a coordinate for the country.
        //        val countryLocation = addr.filter({
        //          case (placeType, x, y, zonedHour) => "country".equalsIgnoreCase(placeType.trim)
        //        })
        val preciseLocations = addr.filter({
          case (placeType, x, y, zonedHour) => precisePlaceType.contains(placeType.trim)
        })

        val locationsAtDifferentLevel = Map(
          "precise" -> preciseLocations,
          "city" -> cityLocations,
          "admin" -> adminLocations
        ).map({
          case (k, v) =>
            var coord = Array[Double]()
            val nightLocations = v.filter({
              //match the night time corresponding to the timezone;
              case (placeType, x, y, zonedHour) =>
                (zonedHour >= 20 || zonedHour <= 6)
            })

            var matrixData = v.map({
              case (placeType, x, y, timestamp) => Seq[Double](y, x)
            })

            if (nightLocations.nonEmpty) {
              matrixData = nightLocations.map({
                case (placeType, x, y, timestamp) => Seq[Double](y, x)
              })
            }

            if (matrixData.nonEmpty) {
              // determine whether it contains precise location
              val dmatrix = new DenseMatrix(
                matrixData.size, 2, matrixData.flatMap(seq => seq).toArray
              )
              val clusterPoints = dbscan(dmatrix)
              // TODO: currently, we take the cluster where the users posted the largest number of tweets
              coord = clusterPoints.lastOption.getOrElse(List(matrixData.head.toArray)).head
            }
            // if not, take the most precise coordinate and
            k -> Bytes.toBytes(new JSONArray(coord).toString)
        })
        uid -> Map("location" -> locationsAtDifferentLevel)
      }).toHBase("machineLearn2012")
      startRowPrefix += 1;
    }
  }
}
