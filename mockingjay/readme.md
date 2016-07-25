Remember to link hdfs-site.xml to the conf directory. In that case the replicate factor of HDFS will take effect on HBase.

Note for snappy on HBase and Hadoop.

0. Make your operating system have snappy native library installed. Compile snappy library and install. Configure environment variables.
1. Make HDFS or Hadoop MapReduce/Yarn have snappy installed. Copy or sim-link the library into Hadoop native folder and modify configuration files.
2. Make HBase have snappy installed. Copy or sim-link the library into HBase lib folder, modify configuration files.
3. Make Spark have snappy supported. Modify the configuration file spark-defaults.conf to load snappy library.