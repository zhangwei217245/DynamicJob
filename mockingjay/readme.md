# Introduction

This is a document about reproducing the data mining using Spark and Hbase. 

What is written here is specifically for **Hadoop 2.7.2**, **HBase 1.1.5** and **Spark 1.6.1**

# Installing Hadoop, HBase, Spark

If you didn't install them, you have to download them from the official site. 

Follow the official document, you should be able to install them on your cluster. 

| Software  |                  Official Site                    |
|:---------:|:-------------------------------------------------:|
| Hadoop    | <http://hadoop.apache.org/releases.html>          |
| HBase     | <http://hbase.apache.org>                         |
| Spark     | <http://spark.apache.org/downloads.html>          |

You can also follow other online references or tutorials for installation. 

# Save the Storage Capacity.

Modify the hdfs-site.xml file under Hadoop directory. Make sure the value for configuration `dfs.replication` is 1. 

```bash
$ cd ~/hadoop/etc/hadoop
$ vim hdfs-site.xml
```

Then remember to link hdfs-site.xml to the conf directory of HBase. In that case the replicate factor of HDFS will take effect on HBase

```bash
$ cd ~/hbase/conf
$ ln -s ~/hadoop/etc/hadoop/hdfs-site.xml ./
```

## Installing Snappy Codec for data compression on HBase and Spark. 



Note for snappy on HBase and Hadoop.

0. Make your operating system have snappy native library installed. Compile snappy library and install. Configure environment variables.
1. Make HDFS or Hadoop MapReduce/Yarn have snappy installed. Copy or sim-link the library into Hadoop native folder and modify configuration files.
2. Make HBase have snappy installed. Copy or sim-link the library into HBase lib folder, modify configuration files.
3. Make Spark have snappy supported. Modify the configuration file spark-defaults.conf to load snappy library.