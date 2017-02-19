# Introduction

This is a project for data mining task in Geospatial Analysis. 

In this project, there are several sub-projects, and they are basically in three 
categories:

* A series of projects written for geospatial demographic analysis on Twitter data.
* A series of projects written for generating Geotiff images relecting liveness of twitter users in entire U.S.
* Several auxiliary sub-projects.

|        Category             |Platform|    Sub-Projects  |    Language    |                             Descrption                             |   Output   |
|:---------------------------:|:------:|:----------------:|:--------------:|:-------------------------------------------------------------------|:----------:|
|   Demographic Analysis      | Spark  |     mockingjay   |   Scala/Java   |  Main Project where spark data mining scripts exist                |     CSV    |
|  Liveness of twitter users  | Redis  |     geotwitter   |   NodeJS       |  Generating geotiff image on liveness of twitter user              |   Geotiff  |
|                             | Redis  |    pygeotwitter  |   Python       |  The same function as above                                        |   Geotiff  |
|  Auxiliary Project          | VA     |   vampire        |   Java         |  Reading twitter streaming API and store tweets on disk            | Gzipped TXT|
|                             | VA     |    core          |   Java         |  Some utilit class serving for all JVM-based projects              |  N/A       |
|                             | VA     |    beak          |   Java         |  A project supposed to finish the funtion of geotwitter            |  Deprecated|
|                             | VA     |    script        |   Bash         |  A series of bash script for configuring the linux machine         |  N/A       |
|                             | VA     | Geos\_Chem\_Wiki |   markdown     |  A series of documents demonstrating the installation of GEOS_CHEM |  N/A       |

If you need instruction about any sub-project, just click on that sub-project you are interested and you will see the document. 
But here, we will only talk about the configuration of the entire computing environment. 

# Cluster Configuration

Overall, we design the entire software stack like this:

| Software |                                                                             Function                                                              |
|:--------:|:--------------------------------------------------------------------------------------------------------------------------------------------------|
|   SPARK  | To work on top of HBase and HDFS. Read txt file from HDFS and store extracted information into HBase, and then conduct data mining on top of HBase|
|   HBASE  | To work on top of HDFS. The database file is stored in a certain folder on HDFS                                                                   |
|   HDFS   | A distributed file system. All the raw data and database files are stored in this layer.                                                          |

In total, we have a cluster of 4 commodity machines running Ubuntu 16.04.1 LTS. 
The head node is the machine where people may initiate the login to this cluster, 
other computing nodes can be accessed once you login to the head node. 
They play different role in different software:


|        Machine Host         |          Function         |              Role in HDFS               |         Role in HBase    |          Role in Spark            |
|:---------------------------:|:-------------------------:|:---------------------------------------:|:------------------------:|:----------------------------------|
|      geotwitter.ttu.edu     |       Head Node           |   NameNode and Secondary NameNode     |            HMaster       | Both Master and Worker Node       |
|   geotwitter-comp1.ttu.edu  |  Computing/Storage Node   |         DataNode                        |         HRegionServer    |   Worker Node                     |
|   geotwitter-comp2.ttu.edu  |  Computing/Storage Node   |         DataNode                        |         HRegionServer    |   Worker Node                     |
|   geotwitter-comp3.ttu.edu  |  Computing/Storage Node   |         DataNode                        |         HRegionServer    |   Worker Node                     |

As shown in this table, basically, the head node serves as the controller/indexing server for each software, and the computing nodes serve as both the storage 
servers as well as the computing servers. The only exception happens with Spark. To increase the parallelism, we increase the number of worker nodes to four, which 
is to run one worker node on head node, so that more computation can be done at the same time. 

# Login Head Node and Computing Node

Currently, you can use the following command to login the head node. 

```bash
$ ssh hadoopuser@geotwitter.ttu.edu
```

For this step, you need to input the password. For the password, you need to contact <X-Spirit.zhang@ttu.edu.>

And after that, you can use the any of following commands to access the cooresponding computing node, without entering password. 

```bash
$ ssh hadoopuser@geotwitter-comp1.ttu.edu
$ ssh hadoopuser@geotwitter-comp2.ttu.edu
$ ssh hadoopuser@geotwitter-comp3.ttu.edu
```

For running Hadoop/Spark clusters, you need to ensure password-free mutual SSH access to all machines in the cluster. 
If you want to know how to configure password-free SSH access between linux machines, please follow this link -> [SSH login without password](http://www.linuxproblem.org/art_9.html)

# Boost everything from nothing.

Suppose you don't have any instance of Hadoop/HDFS/Hbase/Spark running on your cluster. Then you need to follow the boost order below:

    1. Boost HDFS
    2. Boost HBase
    3. Boost Spark

In case if you have any instance of Hadoop/HDFS/Hbase/Spark running on your cluster, you may want to stop all these instance, and you have to follow the shutdown order below:

    1. Shutdown Spark
    2. Shutdown Hbase
    3. Shutdown HDFS
 
Now, we will introduce how to boost each of them and how to shutdown each of them. For all demonstrations here, we suppose that you are at the home directory of `hadoopuser`

## To Boost HDFS

First, it is necessary to boost HDFS in this cluster. On the head node, you login as user `hadoopuser`, and then:

```bash
$ cd ~/hadoop/sbin/
$ ./start-all.sh
```

Although this step will also bring up the YARN instances, but in some cases, you might just need to run Spark jobs through YARN clusters.

After booting it, you need to wait 5-10 minutes to make sure all the initialization is done and the HDFS is providing service through 54310 port. 

Then you can check the functionality of HDFS through the following command that aims to list the direcotries and files stored on HDFS:

```bash
$ hadoop fs -ls
```

You will see something like this:

```
Found 10 items
drwxr-xr-x   - hadoopuser supergroup          0 2016-07-16 18:00 .sparkStaging
drwxr-xr-x   - hadoopuser supergroup          0 2016-05-04 04:03 geotestdata
drwxr-xr-x   - hadoopuser supergroup          0 2016-11-18 15:51 geotwitter
drwxr-xr-x   - hadoopuser supergroup          0 2016-11-18 15:53 geotwitterCSV
drwxr-xr-x   - hadoopuser supergroup          0 2016-11-24 20:28 geotwitterOutput
drwxr-xr-x   - hadoopuser supergroup          0 2016-12-09 15:51 hbase
drwxr-xr-x   - hadoopuser supergroup          0 2016-08-10 19:29 hbase-staging
drwxr-xr-x   - hadoopuser supergroup          0 2016-11-18 00:03 shapefiles
drwxr-xr-x   - hadoopuser supergroup          0 2016-12-09 15:54 spark_job
drwxr-xr-x   - hadoopuser supergroup          0 2016-12-09 16:59 spark_log
```

To know more about the HDFS shell, please click the link below:

**Notice: Do not delete or make changes to exisiting HDFS files/directories. You make create your own file/directory and play with it.**

[HDFS shell](https://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-common/FileSystemShell.html)

## To Boost HBase

```bash
$ cd ~/hbase/bin
$ ./start-hbase.sh
```

After boosting HBase, it's better to wait for 5-10 minutes for the cluster to fully initiate everything. 

Then you do the following to check whether HBase is ready.

```bash
$ cd ~/hbase/bin
$ ./hbase shell
```

After entering the hbase shell, you can type `list` command to list all the tables .

```
hbase(main):001:0> list
TABLE
machineLearn2012
sent_blue_red_2012
twitterUser
3 row(s) in 0.1940 seconds
```

Now, it means that HBase is successfully boosted.

To know more about the HBase shell command, please follow the link below:

**Notice: Do not delete or make changes to exisiting HBase tables. You make create your own HBase table and play with it.**

[HBase Tutorial](https://www.tutorialspoint.com/hbase/index.htm)

[HBase shell commands](https://learnhbase.wordpress.com/2013/03/02/hbase-shell-commands/)


## To Boost Spark

```bash
$ cd ~/spark/sbin
$ ./start-all.sh
```


## To Shutdown Spark

```bash
$ cd ~/spark/sbin
$ ./stop-all.sh
```

## To Shutdown HBase


```bash
$ cd ~/hbase/bin
$ ./stop-hbase.sh
```


## To Shutdown HDFS

```bash
$ cd ~/hadoop/sbin
$ ./stop-all.sh
```

