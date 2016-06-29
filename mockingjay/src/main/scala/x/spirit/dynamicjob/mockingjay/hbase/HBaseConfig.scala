package x.spirit.dynamicjob.mockingjay.hbase

/**
  * Created by zhangwei on 6/29/16.
  */

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.{HBaseConfiguration, HConstants}

/**
  * Wrapper for HBaseConfiguration
  */
class HBaseConfig(defaults: Configuration) extends Serializable {
  def get = HBaseConfiguration.create(defaults)
}

/**
  * Factories to generate HBaseConfig instances.
  *
  * We can generate an HBaseConfig
  * - from an existing HBaseConfiguration
  * - from a sequence of String key/value pairs
  * - from an existing object with a `rootdir` and `quorum` members
  *
  * The two latter cases are provided for simplicity
  * (ideally a client should not have to deal with the native
  * HBase API).
  *
  * The last constructor contains the minimum information to
  * be able to read and write to the HBase cluster. It can be used
  * in tandem with a case class containing job configuration.
  */
object HBaseConfig {
  val DefaultHBaseHost = "localhost"
  def apply(conf: Configuration): HBaseConfig = new HBaseConfig(conf)

  def apply(options: (String, String)*): HBaseConfig = {
    val conf = HBaseConfiguration.create

    for ((key, value) <- options) { conf.set(key, value) }

    apply(conf)
  }

  def apply(hbaseHost: Option[String] = None,
            hbaseXmlConfigFile: String = "hbase-site.xml"): HBaseConfig = {

    val conf = HBaseConfiguration.create

    val xmlFile = Option(getClass.getClassLoader.getResource(hbaseXmlConfigFile))
    xmlFile.foreach(f => conf.addResource(f))

    hbaseHost.foreach(h => conf.set(HConstants.ZOOKEEPER_QUORUM, h))
    if(Option(conf.get(HConstants.ZOOKEEPER_QUORUM)).isEmpty)
      conf.set(HConstants.ZOOKEEPER_QUORUM, HBaseConfig.DefaultHBaseHost)

    apply(conf)
  }

  def apply(conf: { def rootdir: String; def quorum: String }): HBaseConfig = apply(
    "hbase.rootdir" -> conf.rootdir,
    "hbase.zookeeper.quorum" -> conf.quorum)
}


