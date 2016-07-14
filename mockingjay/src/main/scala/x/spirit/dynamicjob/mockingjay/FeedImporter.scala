package x.spirit.dynamicjob.mockingjay

import java.text.SimpleDateFormat
import java.util.Date

import geotrellis.vector.{MultiPolygon, Point, Polygon}
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}
import x.spirit.dynamicjob.mockingjay.hbase._

import scala.collection.mutable
import scala.collection.mutable.WrappedArray



/**
  *
  * scala> df.printSchema()
  * root
  * |-- coordinates: struct (nullable = true)
  * |    |-- coordinates: array (nullable = true)
  * |    |    |-- element: double (containsNull = true)
  * |    |-- type: string (nullable = true)
  * |-- created_at: string (nullable = true)
  * |-- entities: struct (nullable = true)
  * |    |-- user_mentions: array (nullable = true)
  * |    |    |-- element: struct (containsNull = true)
  * |    |    |    |-- id: long (nullable = true)
  * |    |    |    |-- id_str: string (nullable = true)
  * |    |    |    |-- indices: array (nullable = true)
  * |    |    |    |    |-- element: long (containsNull = true)
  * |    |    |    |-- name: string (nullable = true)
  * |    |    |    |-- screen_name: string (nullable = true)
  * |-- favorited: boolean (nullable = true)
  * |-- id: long (nullable = true)
  * |-- place: struct (nullable = true)
  * |    |-- attributes: struct (nullable = true)
  * |    |    |-- locality: string (nullable = true)
  * |    |    |-- region: string (nullable = true)
  * |    |    |-- street_address: string (nullable = true)
  * |    |-- bounding_box: struct (nullable = true)
  * |    |    |-- coordinates: array (nullable = true)
  * |    |    |    |-- element: array (containsNull = true)
  * |    |    |    |    |-- element: array (containsNull = true)
  * |    |    |    |    |    |-- element: double (containsNull = true)
  * |    |    |-- type: string (nullable = true)
  * |    |-- country: string (nullable = true)
  * |    |-- country_code: string (nullable = true)
  * |    |-- full_name: string (nullable = true)
  * |    |-- id: string (nullable = true)
  * |    |-- name: string (nullable = true)
  * |    |-- place_type: string (nullable = true)
  * |    |-- url: string (nullable = true)
  * |-- retweeted: boolean (nullable = true)
  * |-- retweeted_status: struct (nullable = true)
  * |    |-- coordinates: struct (nullable = true)
  * |    |    |-- coordinates: array (nullable = true)
  * |    |    |    |-- element: double (containsNull = true)
  * |    |    |-- type: string (nullable = true)
  * |    |-- created_at: string (nullable = true)
  * |    |-- entities: struct (nullable = true)
  * |    |    |-- user_mentions: array (nullable = true)
  * |    |    |    |-- element: struct (containsNull = true)
  * |    |    |    |    |-- id: long (nullable = true)
  * |    |    |    |    |-- id_str: string (nullable = true)
  * |    |    |    |    |-- indices: array (nullable = true)
  * |    |    |    |    |    |-- element: long (containsNull = true)
  * |    |    |    |    |-- name: string (nullable = true)
  * |    |    |    |    |-- screen_name: string (nullable = true)
  * |    |-- id: long (nullable = true)
  * |    |-- text: string (nullable = true)
  * |    |-- user: struct (nullable = true)
  * |    |    |-- created_at: string (nullable = true)
  * |    |    |-- description: string (nullable = true)
  * |    |    |-- followers_count: long (nullable = true)
  * |    |    |-- friends_count: long (nullable = true)
  * |    |    |-- id: long (nullable = true)
  * |    |    |-- lang: string (nullable = true)
  * |    |    |-- location: string (nullable = true)
  * |    |    |-- name: string (nullable = true)
  * |    |    |-- screen_name: string (nullable = true)
  * |    |    |-- statuses_count: long (nullable = true)
  * |    |    |-- time_zone: string (nullable = true)
  * |    |    |-- verified: boolean (nullable = true)
  * |-- source: string (nullable = true)
  * |-- text: string (nullable = true)
  * |-- user: struct (nullable = true)
  * |    |-- created_at: string (nullable = true)
  * |    |-- description: string (nullable = true)
  * |    |-- followers_count: long (nullable = true)
  * |    |-- friends_count: long (nullable = true)
  * |    |-- id: long (nullable = true)
  * |    |-- lang: string (nullable = true)
  * |    |-- location: string (nullable = true)
  * |    |-- name: string (nullable = true)
  * |    |-- screen_name: string (nullable = true)
  * |    |-- statuses_count: long (nullable = true)
  * |    |-- time_zone: string (nullable = true)
  * |    |-- verified: boolean (nullable = true)
  */


object FeedImporter extends App {


  def createTweetDataFrame(row: Row, prefix: String = "", hasRt: Boolean = false): (String, Map[String, Map[String, Array[Byte]]]) = {

    val twitterDateFormat = new SimpleDateFormat("EEE MMM d kk:mm:ss ZZZZZ yyyy")

    val u_created_at = twitterDateFormat.parse(row.getAs[String](prefix + "u_created_at")).getTime;
    val created_at = twitterDateFormat.parse(row.getAs[String](prefix + "created_at")).getTime;
    var text = Option(row.getAs[String](prefix + "text"));

    val place_id = Option(row.getAs[String](prefix + "place_id"))
    val place_type = Option(row.getAs[String](prefix + "place_type"))
    val place_full_name = Option(row.getAs[String](prefix + "place_full_name"))
    val place_bounding_box : Option[WrappedArray[WrappedArray[WrappedArray[Double]]]]
    = Option(row.getAs[WrappedArray[WrappedArray[WrappedArray[Double]]]](prefix + "place_bounding_box"))

    if (hasRt && "".equals(prefix)) {
      text = Option(text.getOrElse("").concat("   ") + row.getAs[String]("rt_text"));
    }

    var point : Point = Point(0.0, 0.0)
    if (!row.isNullAt(row.fieldIndex(prefix + "coordinates"))) {
      val coordinates : WrappedArray[Double] = row.getAs[WrappedArray[Double]](prefix + "coordinates")
      point = Point((coordinates(0), coordinates(1)));
    } else {
      val boxes : WrappedArray[WrappedArray[WrappedArray[Double]]] = place_bounding_box.getOrElse(WrappedArray.make(WrappedArray.make(
        WrappedArray.make(1.0, 0.0), WrappedArray.make(0.0, 1.0), WrappedArray.make(1.0, 1.0), WrappedArray.make(0.0, 1.0))))
      val multipol : mutable.Buffer[Polygon] = mutable.Buffer[Polygon]()
      boxes.foreach({pg =>
        val points : mutable.Buffer[Point] = mutable.Buffer[Point]()
        pg.foreach({p=>
          val point = Point(p(0),p(1));
          points.append(point);
        })
        points.append(Point(pg(0)(0), pg(0)(1)));
        multipol.append(Polygon(points))
      })
      var mPolygon = MultiPolygon(multipol)
      point = mPolygon.centroid.as[Point].getOrElse(Point(0.0, 0.0))
    }

    val name = Option(row.getAs[String](prefix + "u_name"))
    val screen_name = Option(row.getAs[String](prefix + "u_screen_name"))
    val lang = Option(row.getAs[String](prefix + "u_lang"))
    val time_zone = Option(row.getAs[String](prefix + "u_time_zone"))
    val verified = Option(row.getAs[Boolean](prefix + "u_verified"))
    val description = Option(row.getAs[String](prefix + "u_description"))
    val location = Option(row.getAs[String](prefix + "u_location"))
    val followers_count = Option(row.getAs[Long](prefix + "u_followers_count"))
    val friends_count = Option(row.getAs[Long](prefix + "u_friends_count"))
    val statuses_count = Option(row.getAs[Long](prefix + "u_statuses_count"))

    val content = Map(
      "user" -> Map(
        "created_at" -> Bytes.toBytes(u_created_at),
        "name" -> Bytes.toBytes(name.getOrElse("")),
        "screen_name" -> Bytes.toBytes(screen_name.getOrElse("")),
        "lang" -> Bytes.toBytes(lang.getOrElse("")),
        "time_zone" -> Bytes.toBytes(time_zone.getOrElse("")),
        "verified" -> Bytes.toBytes(verified.getOrElse(false)),
        "description" -> Bytes.toBytes(description.getOrElse("")),
        "location" -> Bytes.toBytes(location.getOrElse("")),
        "followers_count" -> Bytes.toBytes(followers_count.getOrElse(0l)),
        "friends_count" -> Bytes.toBytes(friends_count.getOrElse(0l)),
        "statuses_count" -> Bytes.toBytes(statuses_count.getOrElse(0l))
      ),
      "tweet" -> Map(
        created_at.toString() -> Bytes.toBytes(text.getOrElse(""))
      ),
      "location" -> Map(
        // JSON ARRAY 0, id, 1, type, 2, full_name, 3, coordinates
        created_at.toString -> Bytes.toBytes(
          ("[[\"%s\",\"%s\",\"%s\"][%f,%f]]")
              .format(place_id.getOrElse(""),
                place_type.getOrElse(""),
                place_full_name.getOrElse(""),
                point.x, point.y)
          )
      )
    );
    row.getAs(prefix + "u_id").toString -> content
  }


  override def main(args: Array[String]) {
    if (args.length < 1) {
      System.err.println("Usage: FeedImporter <file>")
      System.exit(1);
    }
    val sparkConf = new SparkConf().setAppName("FileImporter")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)

    implicit val config = HBaseConfig(
      hbaseXmlConfigFile = "hbase-site.xml"
    )

    val dirs = FileSystem.get(sc.hadoopConfiguration)
      .listFiles(new Path("hdfs://geotwitter.ttu.edu:54310/user/hadoopuser/geotwitter/"), true)

    val monthlyDirs = scala.collection.mutable.Set[String]()

    while (dirs.hasNext) {
      monthlyDirs.add(dirs.next().getPath.getParent.toString)
    }

    //Prefetch the data schema from testing data. For the data on 2012-08-01, the data schema is thorough.
    val testFiles = sc.textFile("hdfs://geotwitter.ttu.edu:54310/user/hadoopuser/geotestdata/20120801/*.gz")
    val testText = testFiles.filter(line => line.length > 0).map(line => line.split("\\|")(1))
    val dfschema = sqlContext.read.json(testText).schema

    val admin = Admin()
    val table = "twitterUser";
    val families = Set("user", "tweet", "location", "sentiment", "residential", "political", "race", "age", "gender");
    if (!admin.tableExists(table, families)) {
      admin.createTable(table, families)
    }

    monthlyDirs.toList.sorted.foreach({ case (path) =>
      try {
        // This is the SQL selection criteria for JSON DataFrame
        val fieldsWithRT = Array[String](
          "user.created_at as u_created_at",
          "user.description as u_description",
          "user.id as u_id",
          "user.lang as u_lang",
          "user.location as u_location",
          "user.name as u_name",
          "user.screen_name as u_screen_name",
          "user.time_zone as u_time_zone",
          "user.verified as u_verified",
          "user.followers_count as u_followers_count",
          "user.friends_count as u_friends_count",
          "user.statuses_count as u_statuses_count",
          "created_at",
          "id",
          "text",
          "coordinates.coordinates",
          "place.id as place_id",
          "place.place_type as place_type",
          "place.full_name as place_full_name",
          "place.bounding_box.coordinates as place_bounding_box",
          "retweeted_status.created_at as rt_created_at",
          "retweeted_status.id as rt_id",
          "retweeted_status.text as rt_text",
          "retweeted_status.coordinates.coordinates as rt_coordinates",
          "retweeted_status.place.id as rt_place_id",
          "retweeted_status.place.place_type as rt_place_type",
          "retweeted_status.place.full_name as rt_place_full_name",
          "retweeted_status.place.bounding_box.coordinates as rt_place_bounding_box",
          "retweeted_status.user.created_at as rt_u_created_at",
          "retweeted_status.user.description as rt_u_description",
          "retweeted_status.user.id as rt_u_id",
          "retweeted_status.user.lang as rt_u_lang",
          "retweeted_status.user.location as rt_u_location",
          "retweeted_status.user.name as rt_u_name",
          "retweeted_status.user.screen_name as rt_u_screen_name",
          "retweeted_status.user.time_zone as rt_u_time_zone",
          "retweeted_status.user.verified as rt_u_verified",
          "retweeted_status.user.followers_count as rt_u_followers_count",
          "retweeted_status.user.friends_count as rt_u_friends_count",
          "retweeted_status.user.statuses_count as rt_u_statuses_count")

        val fieldsWithoutRT = Array[String](
          "user.created_at as u_created_at",
          "user.description as u_description",
          "user.id as u_id",
          "user.lang as u_lang",
          "user.location as u_location",
          "user.name as u_name",
          "user.screen_name as u_screen_name",
          "user.time_zone as u_time_zone",
          "user.verified as u_verified",
          "user.followers_count as u_followers_count",
          "user.friends_count as u_friends_count",
          "user.statuses_count as u_statuses_count",
          "created_at",
          "id",
          "text",
          "coordinates.coordinates",
          "place.id as place_id",
          "place.place_type as place_type",
          "place.full_name as place_full_name",
          "place.bounding_box.coordinates as place_bounding_box")

        val fileSuffix = "/*.gz";

        val content = sc.textFile(path + fileSuffix)
        println("Processing file :" + path + " @ " + new Date())
        val txtrdd = content.filter(line => line.length > 0).map(line => line.split("\\|")(1))
        val dfWithoutRT = sqlContext.read.schema(dfschema).json(txtrdd).filter("user.geo_enabled=true").where("retweeted_status is null")

        if (dfWithoutRT.count > 0) {
          // Transfer data frame into RDD, and prepare it for writing to HBase
          var twRdd = dfWithoutRT.selectExpr(fieldsWithoutRT: _*).map({ row => createTweetDataFrame(row, "", false) })
          twRdd.toHBaseBulk(table);
        }
        val dfWithRT = sqlContext.read.schema(dfschema).json(txtrdd).filter("user.geo_enabled=true").where("retweeted_status is not null")

        if (dfWithRT.count > 0) {
          // Transfer data frame of all retweeted
          var twRdd = dfWithRT.selectExpr(fieldsWithRT: _*).map({ row => createTweetDataFrame(row, "", true) })

          twRdd = twRdd ++ dfWithRT.selectExpr(fieldsWithRT: _*).map({ row => createTweetDataFrame(row, "rt_", true) })

          twRdd.toHBaseBulk(table);
        }

      } catch {
        case e: Throwable =>
          println("Failed to import file : " + path + " due to the following error:" + e.getMessage)
          e.printStackTrace(System.err)
      }
    })
    admin.close
  }
}