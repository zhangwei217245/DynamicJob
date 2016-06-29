package x.spirit.dynamicjob.mockingjay

import org.apache.hadoop.hbase.client.Put
import org.apache.spark.rdd.RDD
import x.spirit.dynamicjob.mockingjay.hbase.Writes


/**
  * Created by zhangwei on 6/29/16.
  */
package object hbase extends HBaseWriteSupport
  with DefaultWrites
  with HBaseReadSupport
  with DefaultReads
  with HBaseDeleteSupport
  with HFileSupport
  with HBaseUtils{


}