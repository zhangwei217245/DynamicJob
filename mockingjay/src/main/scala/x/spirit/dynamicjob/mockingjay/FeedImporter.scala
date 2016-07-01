package x.spirit.dynamicjob.mockingjay

import java.text.SimpleDateFormat
import java.util

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import x.spirit.dynamicjob.mockingjay.hbase._
import scala.collection.JavaConverters._


/**
  *
scala> df.printSchema()
root
 |-- coordinates: struct (nullable = true)
 |    |-- coordinates: array (nullable = true)
 |    |    |-- element: double (containsNull = true)
 |    |-- type: string (nullable = true)
 |-- created_at: string (nullable = true)
 |-- entities: struct (nullable = true)
 |    |-- user_mentions: array (nullable = true)
 |    |    |-- element: struct (containsNull = true)
 |    |    |    |-- id: long (nullable = true)
 |    |    |    |-- id_str: string (nullable = true)
 |    |    |    |-- indices: array (nullable = true)
 |    |    |    |    |-- element: long (containsNull = true)
 |    |    |    |-- name: string (nullable = true)
 |    |    |    |-- screen_name: string (nullable = true)
 |-- favorited: boolean (nullable = true)
 |-- id: long (nullable = true)
 |-- place: struct (nullable = true)
 |    |-- attributes: struct (nullable = true)
 |    |    |-- locality: string (nullable = true)
 |    |    |-- region: string (nullable = true)
 |    |    |-- street_address: string (nullable = true)
 |    |-- bounding_box: struct (nullable = true)
 |    |    |-- coordinates: array (nullable = true)
 |    |    |    |-- element: array (containsNull = true)
 |    |    |    |    |-- element: array (containsNull = true)
 |    |    |    |    |    |-- element: double (containsNull = true)
 |    |    |-- type: string (nullable = true)
 |    |-- country: string (nullable = true)
 |    |-- country_code: string (nullable = true)
 |    |-- full_name: string (nullable = true)
 |    |-- id: string (nullable = true)
 |    |-- name: string (nullable = true)
 |    |-- place_type: string (nullable = true)
 |    |-- url: string (nullable = true)
 |-- retweeted: boolean (nullable = true)
 |-- retweeted_status: struct (nullable = true)
 |    |-- coordinates: struct (nullable = true)
 |    |    |-- coordinates: array (nullable = true)
 |    |    |    |-- element: double (containsNull = true)
 |    |    |-- type: string (nullable = true)
 |    |-- created_at: string (nullable = true)
 |    |-- entities: struct (nullable = true)
 |    |    |-- user_mentions: array (nullable = true)
 |    |    |    |-- element: struct (containsNull = true)
 |    |    |    |    |-- id: long (nullable = true)
 |    |    |    |    |-- id_str: string (nullable = true)
 |    |    |    |    |-- indices: array (nullable = true)
 |    |    |    |    |    |-- element: long (containsNull = true)
 |    |    |    |    |-- name: string (nullable = true)
 |    |    |    |    |-- screen_name: string (nullable = true)
 |    |-- id: long (nullable = true)
 |    |-- text: string (nullable = true)
 |    |-- user: struct (nullable = true)
 |    |    |-- created_at: string (nullable = true)
 |    |    |-- description: string (nullable = true)
 |    |    |-- followers_count: long (nullable = true)
 |    |    |-- friends_count: long (nullable = true)
 |    |    |-- id: long (nullable = true)
 |    |    |-- lang: string (nullable = true)
 |    |    |-- location: string (nullable = true)
 |    |    |-- name: string (nullable = true)
 |    |    |-- screen_name: string (nullable = true)
 |    |    |-- statuses_count: long (nullable = true)
 |    |    |-- time_zone: string (nullable = true)
 |    |    |-- verified: boolean (nullable = true)
 |-- source: string (nullable = true)
 |-- text: string (nullable = true)
 |-- user: struct (nullable = true)
 |    |-- created_at: string (nullable = true)
 |    |-- description: string (nullable = true)
 |    |-- followers_count: long (nullable = true)
 |    |-- friends_count: long (nullable = true)
 |    |-- id: long (nullable = true)
 |    |-- lang: string (nullable = true)
 |    |-- location: string (nullable = true)
 |    |-- name: string (nullable = true)
 |    |-- screen_name: string (nullable = true)
 |    |-- statuses_count: long (nullable = true)
 |    |-- time_zone: string (nullable = true)
 |    |-- verified: boolean (nullable = true)
  */


object FeedImporter extends App{

  override def main(args: Array[String]) {
    if (args.length < 1) {
      System.err.println("Usage: FeedImporter <file>")
      System.exit(1);
    }
    val sparkConf = new SparkConf().setAppName("FeedImport")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)

    val conf : Configuration = new Configuration()

    implicit val config = HBaseConfig(
      hbaseXmlConfigFile = "hbase-site.xml"
    )

    val twitterDateFormat = new SimpleDateFormat("EEE MMM d kk:mm:ss ZZZZZ yyyy");
    val fields = Array[String]("user.created_at as u_created_at",
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
      "retweeted",
      "retweeted_status.created_at as rt_created_at",
      "retweeted_status.id as rt_id",
      "retweeted_status.text as rt_text",
      "retweeted_status.coordinates.coordinates as rt_coordinates",
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

    val files = sc.textFile("hdfs://geotwitter.ttu.edu:54310/user/hadoopuser/geotestdata/20120801/*.gz")
    val txtrdd = files.filter(line => line.length>0 ).map(line=>line.split("\\|")(1))
    val df = sqlContext.read.json(txtrdd).filter("user.geo_enabled=true").selectExpr(fields:_*)

    // Transfer data frame into RDD, and prepare it for writing to HBase
    val twRdd = df.map({row =>
      val u_created_at = twitterDateFormat.parse(row.getAs("u_created_at").toString()).getTime;
      val created_at = twitterDateFormat.parse(row.getAs("created_at").toString()).getTime;
      val retweeted = Option(row.getBoolean(row.fieldIndex("retweeted")));
      var text = Option(row.getString(row.fieldIndex("text")));
      if (retweeted.getOrElse(false)) {
        text = Option(text.getOrElse("").concat("   ") + row.getString(row.fieldIndex("rt_text")));
      }

      var coordinates = "0.0, 0.0"
      if (!row.isNullAt(row.fieldIndex("coordinates"))){
        coordinates = row.getList[Double](row.fieldIndex("coordinates"))
          .toString.replace("[","").replace("]","")
      }

      val name = Option(row.getString(row.fieldIndex("u_name")))
      val screen_name = Option(row.getString(row.fieldIndex("u_screen_name")))
      val lang = Option(row.getString(row.fieldIndex("u_lang")))
      val time_zone = Option(row.getString(row.fieldIndex("u_time_zone")))
      val verified = Option(row.getBoolean(row.fieldIndex("u_verified")))
      val description = Option(row.getString(row.fieldIndex("u_description")))
      val location = Option(row.getString(row.fieldIndex("u_location")))
      val followers_count = Option(row.getLong(row.fieldIndex("u_followers_count")))
      val friends_count = Option(row.getLong(row.fieldIndex("u_friends_count")))
      val statuses_count = Option(row.getLong(row.fieldIndex("u_statuses_count")))

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
          row.getAs("id").toString() -> Bytes.toBytes(text.getOrElse(""))
        ),
        "location" -> Map(
          created_at.toString -> Bytes.toBytes(coordinates)
        )
      );
      row.getAs("u_id").toString -> content;
    })

    // Transfer data frame of all retweeted
    val rtRdd = df.filter("retweeted=true").map({row =>
      val u_created_at = twitterDateFormat.parse(row.getAs("rt_u_created_at").toString()).getTime;
      val created_at = twitterDateFormat.parse(row.getAs("rt_created_at").toString()).getTime;
      var text = Option(row.getString(row.fieldIndex("rt_text")));

      var coordinates = "0.0, 0.0"
      if (!row.isNullAt(row.fieldIndex("rt_coordinates"))){
        coordinates = row.getList[Double](row.fieldIndex("rt_coordinates"))
          .toString.replace("[","").replace("]","")
      }

      val name = Option(row.getString(row.fieldIndex("rt_u_name")))
      val screen_name = Option(row.getString(row.fieldIndex("rt_u_screen_name")))
      val lang = Option(row.getString(row.fieldIndex("rt_u_lang")))
      val time_zone = Option(row.getString(row.fieldIndex("rt_u_time_zone")))
      val verified = Option(row.getBoolean(row.fieldIndex("rt_u_verified")))
      val description = Option(row.getString(row.fieldIndex("rt_u_description")))
      val location = Option(row.getString(row.fieldIndex("rt_u_location")))
      val followers_count = Option(row.getLong(row.fieldIndex("rt_u_followers_count")))
      val friends_count = Option(row.getLong(row.fieldIndex("rt_u_friends_count")))
      val statuses_count = Option(row.getLong(row.fieldIndex("rt_u_statuses_count")))

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
          row.getAs("rt_id").toString() -> Bytes.toBytes(text.getOrElse(""))
        ),
        "location" -> Map(
          created_at.toString -> Bytes.toBytes(coordinates)
        )
      );
      row.getAs("rt_u_id").toString -> content;
    })

    val admin = Admin()
    val table = "twitterUser";
    val families = Set("user", "tweet", "location", "guess1", "guess2");
    if (admin.tableExists(table, families)) {
      (twRdd ++ rtRdd).toHBaseBulk(table);
    } else {
      admin.createTable(table, families);
      (twRdd ++ rtRdd).toHBaseBulk(table);

    }

    admin.close


  }
}