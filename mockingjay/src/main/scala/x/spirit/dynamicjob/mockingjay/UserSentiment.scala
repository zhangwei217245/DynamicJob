package x.spirit.dynamicjob.mockingjay

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import x.spirit.dynamicjob.mockingjay.hbase.HBaseConfig

import x.spirit.dynamicjob.mockingjay.corenlp.functions._

/**
  * Created by zhangwei on 7/7/16.
  */
object UserSentiment {


  def main(args: Array[String]) {
    val doc = "Hilary is a bitch, Obama is awesome."
    ssplit(doc).map(sentiment(_)).foreach(System.out.println(_))
  }
}
