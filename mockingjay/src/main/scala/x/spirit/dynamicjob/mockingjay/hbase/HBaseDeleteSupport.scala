package x.spirit.dynamicjob.mockingjay.hbase

/**
  * Created by zhangwei on 6/29/16.
  */
import org.apache.hadoop.hbase.client.Delete
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.rdd.RDD
import x.spirit.dynamicjob.mockingjay.hbase.HBaseDeleteMethods._
import HBaseUtils.createJob

/**
  * Adds implicit methods to `RDD[(String, Map[String, A])]`,
  * `RDD[(String, Seq[A])]` and
  * `RDD[(String, Map[String, Map[String, A]])]`
  * to write to HBase sources.
  */
trait HBaseDeleteSupport {

  implicit def deleteHBaseRDDKey(rdd: RDD[String]): HBaseDeleteRDDKey =
    new HBaseDeleteRDDKey(rdd, del)

  implicit def deleteHBaseRDDSimple(rdd: RDD[(String, Set[String])]): HBaseDeleteRDDSimple[String] =
    new HBaseDeleteRDDSimple(rdd, del)

  implicit def deleteHBaseRDDSimpleT(rdd: RDD[(String, Set[(String, Long)])]): HBaseDeleteRDDSimple[(String, Long)] =
    new HBaseDeleteRDDSimple(rdd, delT)

  implicit def deleteHBaseRDD(rdd: RDD[(String, Map[String, Set[String]])]): HBaseDeleteRDD[String] =
    new HBaseDeleteRDD(rdd, del)

  implicit def deleteHBaseRDDT(rdd: RDD[(String, Map[String, Set[(String, Long)]])]): HBaseDeleteRDD[(String, Long)] =
    new HBaseDeleteRDD(rdd, delT)
}

private[hbase] object HBaseDeleteMethods {
  type Deleter[A] = (Delete, Array[Byte], A) => Delete

  // Delete
  def del(delete: Delete, cf: Array[Byte], q: String) = delete.addColumns(cf, Bytes.toBytes(q))
  def delT(delete: Delete, cf: Array[Byte], qt: (String, Long)) = delete.addColumn(cf, Bytes.toBytes(qt._1), qt._2)
}

sealed abstract class HBaseDeleteHelpers[A] {
  protected def convert(id: String, values: Map[String, Set[A]], del: Deleter[A]) = {
    val d = new Delete(Bytes.toBytes(id))
    var empty = true
    for {
      (family, contents) <- values
      content <- contents
    } {
      empty = false
      del(d, Bytes.toBytes(family), content)
    }

    if (empty) None else Some(new ImmutableBytesWritable, d)
  }

  protected def convert(id: String, families: Set[String]) = {
    val d = new Delete(Bytes.toBytes(id))
    for (family <- families) d.addFamily(Bytes.toBytes(family))
    Some(new ImmutableBytesWritable, d)
  }

  protected def convert(id: String) = {
    val d = new Delete(Bytes.toBytes(id))
    Some(new ImmutableBytesWritable, d)
  }
}

final class HBaseDeleteRDDKey(val rdd: RDD[String], val del: Deleter[String]) extends HBaseDeleteHelpers[String] with Serializable {

  /**
    * Delete rows specified by rowkeys of the underlying RDD from HBase.
    *
    * The RDD is assumed to be an instance of `RDD[String]` of rowkeys.
    */
  def deleteHBase(table: String)(implicit config: HBaseConfig) = {
    val conf = config.get
    val job = createJob(table, conf)

    rdd.flatMap({ k => convert(k) }).saveAsNewAPIHadoopDataset(job.getConfiguration)
  }

  /**
    * Delete column families of the underlying RDD from HBase.
    *
    * The RDD is assumed to be an instance of `RDD[String]` of rowkeys.
    */
  def deleteHBase(table: String, families: Set[String])(implicit config: HBaseConfig) = {
    val conf = config.get
    val job = createJob(table, conf)

    rdd.flatMap({ k => convert(k, families) }).saveAsNewAPIHadoopDataset(job.getConfiguration)
  }

  /**
    * Delete columns of the underlying RDD from HBase.
    *
    * Columns are deleted from the same column family, and are the same for all rows.
    *
    * The RDD is assumed to be an instance of `RDD[String]` of rowkeys.
    */
  def deleteHBase(table: String, family: String, columns: Set[String])(implicit config: HBaseConfig) = {
    val conf = config.get
    val job = createJob(table, conf)

    rdd.flatMap({ k => convert(k, Map(family -> columns), del) }).saveAsNewAPIHadoopDataset(job.getConfiguration)
  }

  /**
    * Delete columns of the underlying RDD from HBase.
    *
    * Columns specified as a map of families / set of qualifiers, are the same for all rows.
    *
    * The RDD is assumed to be an instance of `RDD[String]` of rowkeys.
    */
  def deleteHBase(table: String, qualifiers: Map[String, Set[String]])(implicit config: HBaseConfig) = {
    val conf = config.get
    val job = createJob(table, conf)

    rdd.flatMap({ k => convert(k, qualifiers, del) }).saveAsNewAPIHadoopDataset(job.getConfiguration)
  }
}

final class HBaseDeleteRDDSimple[A](val rdd: RDD[(String, Set[A])], val del: Deleter[A]) extends HBaseDeleteHelpers[A] with Serializable {

  /**
    * Delete columns of the underlying RDD from HBase.
    *
    * Simplified form, where columns are deleted from the
    * same column family.
    *
    * The RDD is assumed to be an instance of `RDD[(String, Seq[A])]`,
    * where the first value is the rowkey and the second is a sequence of
    * column names w/ or w/o timestamps. If timestamp is specified, only the
    * corresponding version is deleted, otherwise all versions are deleted.
    */
  def deleteHBase(table: String, family: String)(implicit config: HBaseConfig) = {
    val conf = config.get
    val job = createJob(table, conf)

    rdd.flatMap({ case (k, v) => convert(k, Map(family -> v), del) }).saveAsNewAPIHadoopDataset(job.getConfiguration)
  }
}

final class HBaseDeleteRDD[A](val rdd: RDD[(String, Map[String, Set[A]])], val del: Deleter[A]) extends HBaseDeleteHelpers[A] with Serializable {
  /**
    * Delete columns of the underlying RDD from HBase.
    *
    * The RDD is assumed to be an instance of `RDD[(String, Map[String, Seq[A]])]`,
    * where the first value is the rowkey and the second is a map that associates
    * column families and sequence of column names w/ or w/o timestamps.
    * If timestamp is specified, only the corresponding version is deleted,
    * otherwise all versions are deleted.
    */
  def deleteHBase(table: String)(implicit config: HBaseConfig) = {
    val conf = config.get
    val job = createJob(table, conf)

    rdd.flatMap({ case (k, v) => convert(k, v, del) }).saveAsNewAPIHadoopDataset(job.getConfiguration)
  }
}