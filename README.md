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
