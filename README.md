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

In total, we have 4 commodity machines running Ubuntu 16.04.1 LTS. 
They play different role in different software:


|        Machine Host         |          Function         |              Role in HDFS               |         Role in HBase    |          Role in Spark            |
|:---------------------------:|:-------------------------:|:---------------------------------------:|:------------------------:|:----------------------------------|
|      geotwitter.ttu.edu     |       Head Node           |   Name Node and Secondary Name Node     |            HMaster       | Both Master and Worker Node       |
|   geotwitter-comp1.ttu.edu  |  Computing/Storage Node   |         DataNode                        |         HRegionServer    |   Worker Node                     |
|   geotwitter-comp2.ttu.edu  |  Computing/Storage Node   |         DataNode                        |         HRegionServer    |   Worker Node                     |
|   geotwitter-comp3.ttu.edu  |  Computing/Storage Node   |         DataNode                        |         HRegionServer    |   Worker Node                     |


Overall, we design the entire software stack like this:

| Software |                                                                             Function                                                              |
|:--------:|:--------------------------------------------------------------------------------------------------------------------------------------------------|
|   SPARK  | To work on top of HBase and HDFS. Read txt file from HDFS and store extracted information into HBase, and then conduct data mining on top of HBase|
|   HBASE  | To work on top of HDFS. The database file is stored in a certain folder on HDFS                                                                   |
|   HDFS   | A distributed file system. All the raw data and database files are stored in this layer.                                                          |

