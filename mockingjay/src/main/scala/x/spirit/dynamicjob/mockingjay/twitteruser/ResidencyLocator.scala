package x.spirit.dynamicjob.mockingjay.twitteruser

import java.time.{Duration, Instant, ZoneId, ZonedDateTime}

import breeze.linalg.DenseMatrix
import nak.cluster.Kmeans
import org.apache.hadoop.hbase.client.Scan
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
    val dbscan_called = Instant.now
    val gdbscan = new GDBSCAN(
      DBSCAN.getNeighbours(epsilon = 0.01, distance = Kmeans.euclideanDistance),
      DBSCAN.isCorePoint(minPoints = 3)
    )
    val cluster = gdbscan cluster v
    val clusterPoints = cluster.map(_.points.map(_.value.toArray)).sortBy(_.size)
    val dbscan_end = Instant.now
    println("[DBSCAN DURATION] = " + Duration.between(dbscan_called, dbscan_end).toMillis)
    clusterPoints
  }

  override def main(args: Array[String]) {
    val sparkConf = new SparkConf().setAppName(this.getClass.getSimpleName)
    val sc = new SparkContext(sparkConf)

    implicit val config = HBaseConfig(
      hbaseXmlConfigFile = "hbase-site.xml"
    )
    config.get.set("hbase.rpc.timeout", "18000000")

    val max_dbscan_samples = 1000

    val validPlaceType = Set("exact", "poi", "neighborhood", "city", "admin", "country")
    val precisePlaceType = Set("exact", "poi", "neighborhood")
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
      val scanRst = sc.hbase[String]("twitterUser", Set("tweet", "user"), scan)
      scanRst.map({ case (k, v) =>
        val uid = k
        //The default timezone has to be decided.
        val user_time_zone_str = v("user").getOrElse("time_zone", "Central Time (US & Canada)")
        val user_time_zone = ZoneId.of(TimeZoneMapping.getTimeZoneId(user_time_zone_str))
        val tweet = v("tweet")
        println(s"uid=$uid and num_tweets = ${tweet.size}")
        val tweet_start = Instant.now
        val addr = tweet.map({ case (tid, jsonBytes) =>
          val jsonArr = new JSONArray(Bytes.toString(jsonBytes))

          val timestamp = jsonArr.getJSONArray(0).getLong(0)
          val placeType = jsonArr.getJSONArray(1).getString(2)
          val coord_x = jsonArr.getJSONArray(2).getDouble(0)
          val coord_y = jsonArr.getJSONArray(2).getDouble(1)

          val zonedHour = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), user_time_zone).getHour

          (placeType, coord_x, coord_y, zonedHour)
        })
        val filter_start = Instant.now
        println("[TIME LOADING TWEET] = " + Duration.between(tweet_start, filter_start).toMillis)
        val adminLocations = addr.filter({
          case (placeType, x, y, zonedHour) => "admin".equalsIgnoreCase(placeType.trim)
        })
        val cityLocations = addr.filter({
          case (placeType, x, y, zonedHour) => "city".equalsIgnoreCase(placeType.trim)
        })
        // Currently we don't need a coordinate for the country.
        //        val countryLocation = addr.filter({
        //          case (placeType, x, y, zonedHour) => "country".equalsIgnoreCase(placeType.trim)
        //        })
        val preciseLocations = addr.filter({
          case (placeType, x, y, zonedHour) => precisePlaceType.contains(placeType.trim)
        })

        val mapping_start = Instant.now
        println("[TIME FILTER TWEET] = " + Duration.between(filter_start, mapping_start).toMillis)

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
              case (placeType, x, y, timestamp) => Seq[Double](x, y)
            })

            if (nightLocations.nonEmpty) {
              matrixData = nightLocations.map({
                case (placeType, x, y, timestamp) => Seq[Double](x, y)
              })
            }


            if (matrixData.nonEmpty) {
              // determine whether it contains precise location

              //For now, due to the limitation of memory capacity, all we can do is to reduce number of samples that DBSCAN needs.
              val num_col = 2
              var num_row = matrixData.size
              var majorStride = num_col * 1
              if (max_dbscan_samples > 0 && matrixData.size >= max_dbscan_samples) {
                num_row = max_dbscan_samples
                majorStride = num_col * Math.floorDiv(matrixData.size, max_dbscan_samples)
              }

              val dmatrix = new DenseMatrix(
                num_row, num_col, matrixData.flatten.toArray, 0, majorStride, true
              )
              val clusterPoints = dbscan(dmatrix)
              //currently, we take the cluster where the users posted the largest number of tweets
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
