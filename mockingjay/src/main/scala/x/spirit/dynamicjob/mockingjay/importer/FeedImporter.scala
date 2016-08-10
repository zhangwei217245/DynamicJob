package x.spirit.dynamicjob.mockingjay.importer

import java.util.Date

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import x.spirit.dynamicjob.mockingjay.hbase._
import x.spirit.dynamicjob.mockingjay.importer.DataTransformer._



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

  override def main(args: Array[String]) {
    var sentiSuffix = "blue_red_";
    if (args.length >= 1) {
      sentiSuffix += args(0);
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
    val sentable = "sent_"+sentiSuffix;
    val families = Set("user", "tweet"); //"location", "sentiment", "residential", "political", "race", "age", "gender"
    val sent_families = Set("tsent");
    if (admin.tableExists(table, families)) {
      monthlyDirs.toList.sorted.foreach({ case (path) =>
        try {
          // This is the SQL selection criteria for JSON DataFrame

          val fileSuffix = "/*.gz";

          val content = sc.textFile(path + fileSuffix)
          println("Processing file :" + path + " @ " + new Date())
          val txtrdd = content.filter(line => line.length > 0).map(line => line.split("\\|")(1))
          val dfWithoutRT = sqlContext.read.schema(dfschema).json(txtrdd).filter("user.geo_enabled=true").where("retweeted_status is null")
          var twRdd = sc.parallelize(Seq[(String, Map[String, Map[String, Array[Byte]]])]())
          if (dfWithoutRT.count > 0) {
            // Transfer data frame into RDD, and prepare it for writing to HBase
            twRdd = twRdd ++ dfWithoutRT.selectExpr(fieldsWithoutRT: _*).map({ row => createTweetDataFrame(row, "", false) })
          }
          val dfWithRT = sqlContext.read.schema(dfschema).json(txtrdd).filter("user.geo_enabled=true").where("retweeted_status is not null")

          if (dfWithRT.count > 0) {
            // Transfer data frame of all retweeted
            twRdd = twRdd ++ dfWithRT.selectExpr(fieldsWithRT: _*).map({ row => createTweetDataFrame(row, "", true) })
            twRdd = twRdd ++ dfWithRT.selectExpr(fieldsWithRT: _*).map({ row => createTweetDataFrame(row, "rt_", true) })
          }

          twRdd.toHBaseBulk(table)
          System.out.println("'%s' table saved!".format(table))

          //twRdd.map(toSentimentRDD(_)).toHBaseBulk(sentable)
          //System.out.println("'%s' table saved!".format(sentable))

        } catch {
          case e: Throwable =>
            println("Failed to import file : " + path + " due to the following error:" + e.getMessage)
            e.printStackTrace(System.err)
        }
      })

    } else {
      System.out.println("Table '%s' with column families '%s' must be created."
        .format(table, families.toString()))
    }

    admin.close
  }
}