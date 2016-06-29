package x.spirit.dynamicjob.mockingjay.hbase

/**
  * Created by zhangwei on 6/29/16.
  */
import org.apache.hadoop.hbase.client.{Result, Scan}
import org.apache.hadoop.hbase.filter.Filter
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.{IdentityTableMapper, TableInputFormat, TableMapReduceUtil}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{Cell, CellUtil}
import org.apache.hadoop.mapreduce.Job
import org.apache.spark._
import org.apache.spark.rdd.RDD

import scala.collection.JavaConversions._

/**
  * Adds implicit methods to SparkContext to read
  * from HBase sources.
  */
trait HBaseReadSupport {
  implicit def toHBaseSC(sc: SparkContext): HBaseSC = new HBaseSC(sc)
}

final class HBaseSC(@transient sc: SparkContext) extends Serializable {
  private def extract[A, B](data: Map[String, Set[String]], result: Result, read: Cell => B) =
    data map {
      case (cf, columns) =>
        val content = columns flatMap { column =>
          Option {
            result.getColumnLatestCell(Bytes.toBytes(cf), Bytes.toBytes(column))
          } map { cell =>
            column -> read(cell)
          }
        } toMap

        cf -> content
    }

  private def extractRow[A, B](data: Set[String], result: Result, read: Cell => B) =
    result.listCells groupBy { cell =>
      new String(CellUtil.cloneFamily(cell))
    } filterKeys data.contains map {
      // We cannot use mapValues here, because it returns a MapLike, which is not serializable,
      // instead we need a (serializable) Map (see https://issues.scala-lang.org/browse/SI-7005)
      case (k, cells) =>
        (k, cells map { cell =>
          val column = new String(CellUtil.cloneQualifier(cell))
          column -> read(cell)
        } toMap)
    }

  private def read[A](cell: Cell)(implicit reader: Reads[A]) = {
    val value = CellUtil.cloneValue(cell)
    reader.read(value)
  }

  private def readTS[A](cell: Cell)(implicit reader: Reads[A]) = {
    val value = CellUtil.cloneValue(cell)
    val timestamp = cell.getTimestamp
    (reader.read(value), timestamp)
  }

  private def makeConf(config: HBaseConfig, table: String, columns: Option[String] = None, scan: Scan = new Scan) = {
    val conf = config.get

    if (columns.isDefined)
      conf.set(TableInputFormat.SCAN_COLUMNS, columns.get)

    val job = Job.getInstance(conf)
    TableMapReduceUtil.initTableMapperJob(table, scan, classOf[IdentityTableMapper], null, null, job)

    job.getConfiguration
  }

  private def prepareScan(filter: Filter) = new Scan().setFilter(filter)

  /**
    * Provides an RDD of HBase rows. Here `data` is a map whose
    * keys represent the column families and whose values are
    * the list of columns to be read from the family.
    *
    * Returns an `RDD[(String, Map[String, Map[String, A]])]`, where
    * the first element is the rowkey and the second element is a
    * nested map which associates column family and column to
    * the value. Columns which are not found are omitted from the map.
    */
  def hbase[A](table: String, data: Map[String, Set[String]])(implicit config: HBaseConfig, reader: Reads[A]): RDD[(String, Map[String, Map[String, A]])] =
    hbase(table, data, new Scan)

  /**
    * Provides an RDD of HBase rows. Here `data` is a map whose
    * keys represent the column families and whose values are
    * the list of columns to be read from the family.
    * Accepts HBase filter as a parameter.
    */
  def hbase[A](table: String, data: Map[String, Set[String]], filter: Filter)(implicit config: HBaseConfig, reader: Reads[A]): RDD[(String, Map[String, Map[String, A]])] =
    hbase(table, data, prepareScan(filter))

  /**
    * Provides an RDD of HBase rows. Here `data` is a map whose
    * keys represent the column families and whose values are
    * the list of columns to be read from the family.
    * Accepts custom HBase Scan instance
    */
  def hbase[A](table: String, data: Map[String, Set[String]], scan: Scan)(implicit config: HBaseConfig, reader: Reads[A]) = {
    hbaseRaw(table, data, scan) map {
      case (key, row) =>
        Bytes.toString(key.get) -> extract(data, row, read[A])
    }
  }

  /**
    * Provides an RDD of HBase rows. Here `data` is a map whose
    * keys represent the column families and whose values are
    * the list of columns to be read from the family.
    *
    * Returns an `RDD[(String, Map[String, Map[String, (A, Long)]])]`, where
    * the first element is the rowkey and the second element is a
    * nested map which associates column family and column to
    * the tuple (value, timestamp). Columns which are not found are omitted from the map.
    */
  def hbaseTS[A](table: String, data: Map[String, Set[String]])(implicit config: HBaseConfig, reader: Reads[A]): RDD[(String, Map[String, Map[String, (A, Long)]])] =
    hbaseTS(table, data, new Scan)

  /**
    * Provides an RDD of HBase rows. Here `data` is a map whose
    * keys represent the column families and whose values are
    * the list of columns to be read from the family.
    * Accepts HBase filter as a parameter.
    */
  def hbaseTS[A](table: String, data: Map[String, Set[String]], filter: Filter)(implicit config: HBaseConfig, reader: Reads[A]): RDD[(String, Map[String, Map[String, (A, Long)]])] =
    hbaseTS(table, data, prepareScan(filter))

  /**
    * Provides an RDD of HBase rows. Here `data` is a map whose
    * keys represent the column families and whose values are
    * the list of columns to be read from the family.
    * Accepts custom HBase Scan instance
    */
  def hbaseTS[A](table: String, data: Map[String, Set[String]], scan: Scan)(implicit config: HBaseConfig, reader: Reads[A]) = {
    hbaseRaw(table, data, scan) map {
      case (key, row) =>
        Bytes.toString(key.get) -> extract(data, row, readTS[A])
    }
  }

  protected def hbaseRaw[A](table: String, data: Map[String, Set[String]], scan: Scan)(implicit config: HBaseConfig) = {
    val columns = (for {
      (cf, cols) <- data
      col <- cols
    } yield s"$cf:$col") mkString " "

    sc.newAPIHadoopRDD(makeConf(config, table, Some(columns), scan), classOf[TableInputFormat],
      classOf[ImmutableBytesWritable], classOf[Result])
  }

  /**
    * Provides an RDD of HBase rows. Here `data` is a set of
    * column families, which are read in full.
    *
    * Returns an `RDD[(String, Map[String, Map[String, A]])]`, where
    * the first element is the rowkey and the second element is a
    * nested map which associated column family and column to
    * the value.
    */
  def hbase[A](table: String, data: Set[String])(implicit config: HBaseConfig, reader: Reads[A]): RDD[(String, Map[String, Map[String, A]])] =
    hbase(table, data, new Scan)

  /**
    * Provides an RDD of HBase rows. Here `data` is a set of
    * column families, which are read in full.
    * Accepts HBase filter as a parameter.
    */
  def hbase[A](table: String, data: Set[String], filter: Filter)(implicit config: HBaseConfig, reader: Reads[A]): RDD[(String, Map[String, Map[String, A]])] =
    hbase(table, data, prepareScan(filter))

  /**
    * Provides an RDD of HBase rows. Here `data` is a set of
    * column families, which are read in full.
    * Accepts custom HBase Scan instance
    */
  def hbase[A](table: String, data: Set[String], scan: Scan)(implicit config: HBaseConfig, reader: Reads[A]) = {
    hbaseRaw(table, data, scan) map {
      case (key, row) =>
        Bytes.toString(key.get) -> extractRow(data, row, read[A])
    }
  }

  /**
    * Provides an RDD of HBase rows. Here `data` is a set of
    * column families, which are read in full.
    *
    * Returns an `RDD[(String, Map[String, Map[String, (A, Long)]])]`, where
    * the first element is the rowkey and the second element is a
    * nested map which associated column family and column to
    * the tuple (value, timestamp).
    */
  def hbaseTS[A](table: String, data: Set[String])(implicit config: HBaseConfig, reader: Reads[A]): RDD[(String, Map[String, Map[String, (A, Long)]])] =
    hbaseTS(table, data, new Scan)

  /**
    * Provides an RDD of HBase rows. Here `data` is a set of
    * column families, which are read in full.
    * Accepts HBase filter as a parameter.
    */
  def hbaseTS[A](table: String, data: Set[String], filter: Filter)(implicit config: HBaseConfig, reader: Reads[A]): RDD[(String, Map[String, Map[String, (A, Long)]])] =
    hbaseTS(table, data, prepareScan(filter))

  /**
    * Provides an RDD of HBase rows. Here `data` is a set of
    * column families, which are read in full.
    * Accepts custom HBase Scan instance
    */
  def hbaseTS[A](table: String, data: Set[String], scan: Scan)(implicit config: HBaseConfig, reader: Reads[A]) = {
    hbaseRaw(table, data, scan) map {
      case (key, row) =>
        Bytes.toString(key.get) -> extractRow(data, row, readTS[A])
    }
  }

  protected def hbaseRaw[A](table: String, data: Set[String], scan: Scan)(implicit config: HBaseConfig) = {
    val families = data mkString " "

    sc.newAPIHadoopRDD(makeConf(config, table, Some(families), scan), classOf[TableInputFormat],
      classOf[ImmutableBytesWritable], classOf[Result])
  }

  /**
    * Provides an RDD of HBase rows, without interpreting the content
    * of the rows.
    *
    * Returns an `RDD[(String, Result)]`, where the first element is the
    * rowkey and the second element is an instance of
    * `org.apache.hadoop.hbase.client.Result`.
    *
    * The client can then use the full HBase API to process the result.
    */
  def hbase(table: String, scan: Scan = new Scan)(implicit config: HBaseConfig) =
    sc.newAPIHadoopRDD(makeConf(config, table, scan = scan), classOf[TableInputFormat],
      classOf[ImmutableBytesWritable], classOf[Result]) map {
      case (key, row) =>
        Bytes.toString(key.get) -> row
    }

  /**
    * Provides an RDD of HBase rows, without interpreting the content
    * of the rows, with HBase filter support
    */
  def hbase(table: String, filter: Filter)(implicit config: HBaseConfig): RDD[(String, Result)] = hbase(table, prepareScan(filter))

}