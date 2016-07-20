package x.spirit.dynamicjob.mockingjay.importer

import java.text.SimpleDateFormat

import geotrellis.vector.{MultiPolygon, Point, Polygon}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.sql.Row
import org.json.JSONArray
import x.spirit.dynamicjob.core.utils.StringUtils._
import x.spirit.dynamicjob.mockingjay._
import x.spirit.dynamicjob.mockingjay.corenlp.functions._

import scala.collection.mutable
import scala.collection.mutable.WrappedArray
import scala.collection.parallel.mutable.ParArray

/**
  * Created by zhangwei on 7/19/16.
  */
object DataTransformer {

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

  def createTweetDataFrame(row: Row, prefix: String = "", hasRt: Boolean = false): (String, Map[String, Map[String, Array[Byte]]]) = {

    val twitterDateFormat = new SimpleDateFormat("EEE MMM d kk:mm:ss ZZZZZ yyyy")

    val u_created_at = twitterDateFormat.parse(row.getAs[String](prefix + "u_created_at")).getTime;
    val created_at = twitterDateFormat.parse(row.getAs[String](prefix + "created_at")).getTime;

    val t_id = Option(row.getAs[Long](prefix + "id"));
    var text = Option(row.getAs[String](prefix + "text"));

    if (hasRt && "".equals(prefix)) {
      text = Option(text.getOrElse("").concat("   ") + row.getAs[String]("rt_text"));
    }

    var point: Point = Point(0.0, 0.0)
    val forRt = hasRt && prefix.equals("rt_");

    val place_id = Option(if (!forRt) {
      row.getAs[String](prefix + "place_id")
    } else {
      null
    })
    val place_type = Option(if (!forRt) {
      row.getAs[String](prefix + "place_type")
    } else {
      null
    })
    val place_full_name = Option(if (!forRt) {
      row.getAs[String](prefix + "place_full_name")
    } else {
      null
    })
    val place_bounding_box: Option[WrappedArray[WrappedArray[WrappedArray[Double]]]]
    = Option(if (!forRt) {
      row.getAs[WrappedArray[WrappedArray[WrappedArray[Double]]]](prefix + "place_bounding_box")
    } else {
      null
    })


    if (!row.isNullAt(row.fieldIndex(prefix + "coordinates"))) {
      val coordinates: WrappedArray[Double] = row.getAs[WrappedArray[Double]](prefix + "coordinates")
      point = Point((coordinates(0), coordinates(1)));
    } else {
      if (!forRt) {
        val boxes: WrappedArray[WrappedArray[WrappedArray[Double]]] = place_bounding_box.getOrElse(WrappedArray.make(WrappedArray.make(
          WrappedArray.make(1.0, 0.0), WrappedArray.make(0.0, 1.0), WrappedArray.make(1.0, 1.0), WrappedArray.make(0.0, 1.0))))
        val multipol: mutable.Buffer[Polygon] = mutable.Buffer[Polygon]()
        boxes.foreach({ pg =>
          val points: mutable.Buffer[Point] = mutable.Buffer[Point]()
          pg.foreach({ p =>
            val point = Point(p(0), p(1));
            points.append(point);
          })
          points.append(Point(pg(0)(0), pg(0)(1)));
          multipol.append(Polygon(points))
        })
        var mPolygon = MultiPolygon(multipol)
        point = mPolygon.centroid.as[Point].getOrElse(Point(0.0, 0.0))
      }
    }

    val u_id = Option(row.getAs[Long](prefix + "u_id"))
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

    val t_time = new JSONArray() {
      put(created_at)
    }
    val t_content = new JSONArray() {
      put(text.getOrElse(""));
      put(place_id.getOrElse(""));
      put(place_type.getOrElse(""));
      put(place_full_name.getOrElse(""));
    }
    val t_coord = new JSONArray() {
      put(point.x);
      put(point.y);
    }

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
        /**
          * JSON ARRAY
          * 00, created_at,
          * 10, text,
          * 11, place_id,
          * 12, place_type,
          * 13, place_full_name,
          * 20, x,
          * 21, y,
          * 30, red,
          * 31, blue,
          * 32, compound
          */
        t_id.get.toString -> Bytes.toBytes(
          new JSONArray() {
            put(t_time);
            put(t_content);
            put(t_coord);
          }.toString
        )
      )
    );
    u_id.get.toString -> content
  }

  def toSentimentRDD(rddRow: (String, Map[String, Map[String, Array[Byte]]])): (String, Map[String, Map[String, Array[Byte]]]) = {
    val tweetSenti = rddRow._2
      .get("tweet").getOrElse(Map[String, Array[Byte]]()) // Get column family "tweet"
      .map({ tweet => // Iterate each tweet,
      val jarr = new JSONArray(Bytes.toString(tweet._2));
      val text = jarr.getJSONArray(1).getString(0);

      // calculate the overall sentiment score for the entire content.
      val blue_red = ParArray.fromTraversables(purifyTweetAsSentences(text)).map({ sentence =>
        var hasBlue = false;
        var hasRed = false;
        for (word <- tokenize(sentence)) {
          if (Blue.contains(word)) hasBlue = true;
          if (Red.contains(word)) hasRed = true;
        } // determine whether this sentence ever talked about either RED or BLUE
      val sentimentScore = sentiment(removeMemtion(removeHashTag(sentence)));
        val blueScore = if (hasBlue) {
          sentimentScore
        } else {
          0
        } // calculate blue score
      val redScore = if (hasRed) {
          sentimentScore
        } else {
          0
        } // calculate red score
        (sentimentScore, blueScore, redScore, 1) // make a triple like bluescore , redscore, sentence count
      })
      val sumSentiment = blue_red.map(_._1).sum
      val sumBlueSenti = blue_red.map(_._2).sum
      val sumRedSenti = blue_red.map(_._3).sum
      var numSentences = blue_red.map(_._4).sum
      if (numSentences == 0) numSentences = 1
      val jsonArr = new JSONArray() {
        put(Math.round(sumSentiment.toDouble / numSentences.toDouble).toInt); // The overall sentiment score
        put(Math.round(sumBlueSenti.toDouble / numSentences.toDouble).toInt); // The blue sentiment score.
        put(Math.round(sumRedSenti.toDouble / numSentences.toDouble).toInt); // The red sentiment score.
        put(blue_red.map(_._4).sum); // The number of sentences that this tweet has.
      }
      tweet._1 -> Bytes.toBytes(jsonArr.toString)
    })
    val content = Map(
      "tsent" -> tweetSenti
    )
    rddRow._1 -> content
  }

}
