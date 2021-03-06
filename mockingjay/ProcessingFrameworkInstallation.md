Back to [README](readme.md)

# Installing Hadoop/HDFS, HBase, Spark.

What is written here is specifically for **Hadoop 2.7.2**, **HBase 1.1.5** and **Spark 1.6.1**

If you didn't install them, you have to download them from the official site. 

Follow the official document, you should be able to install them on your cluster. 

| Software  |                  Official Site                    |
|:---------:|:-------------------------------------------------:|
| Hadoop    | <http://hadoop.apache.org/releases.html>          |
| HBase     | <http://hbase.apache.org>                         |
| Spark     | <http://spark.apache.org/downloads.html>          |

You can also follow other online references or tutorials for installation. 

# Save the Storage Capacity.

## Modify the replication factor. 

Smaller replication factor means smaller storage consumption.

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

If you just installed Hadoop/HBase/Spark from scratch, you need to also install snappy data compression library. 

But if this step is done before, just jump over this entire section and go to HBase Table Creation section.

### Preconditions:

Make sure you have the following software. 

```
gcc c++, autoconf, automake, libtool, Java 8, JAVA_HOME set, Maven 3
```

If you don't have them all, you can use `apt-get install` to install them all.
But if you have executed [install\_new\_machine.sh](/script/install_new_machine.sh), you are guaranteed to have them already. 

### Installing Snappy Library:

On every machine of your cluster, you need to do the following:

Go to <http://google.github.io/snappy/>, and download the release.

```bash
$ mkdir ~/download/snappy
$ cd ~/download/snappy
$ wget https://codeload.github.com/google/snappy/legacy.tar.gz/master
$ tar zxvf master
```

Now, compile and generate the library.

```bash
$ cd google-snappy-2d99bd1
$ ./autogen.sh
$ ./configure
$ sudo make
$ sudo make install
```

After this, you should be able to see the following notification on the screen. 

```
----------------------------------------------------------------------
Libraries have been installed in:
   /usr/local/lib

If you ever happen to want to link against installed libraries
in a given directory, LIBDIR, you must either use libtool, and
specify the full pathname of the library, or use the `-LLIBDIR'
flag during linking and do at least one of the following:
   - add LIBDIR to the `LD_LIBRARY_PATH' environment variable
     during execution
   - add LIBDIR to the `LD_RUN_PATH' environment variable
     during linking
   - use the `-Wl,-rpath -Wl,LIBDIR' linker flag
   - have your system administrator add LIBDIR to `/etc/ld.so.conf'

See any operating system documentation about shared libraries for
more information, such as the ld(1) and ld.so(8) manual pages.
----------------------------------------------------------------------
```

Follow the the above instruction to setup the specified environment variables. 

Now, if you go to `/usr/local/lib`, you will see the following files:

```
-rw-r--r-- 1 root root  536K Feb 26 16:52 libsnappy.a
-rwxr-xr-x 1 root root   955 Feb 26 16:52 libsnappy.la*
lrwxrwxrwx 1 root root    18 Feb 26 16:52 libsnappy.so -> libsnappy.so.1.3.0*
lrwxrwxrwx 1 root root    18 Feb 26 16:52 libsnappy.so.1 -> libsnappy.so.1.3.0*
-rwxr-xr-x 1 root root  265K Feb 26 16:52 libsnappy.so.1.3.0*
```

### Installing Hadoop-Snappy 

Go to <https://github.com/electrum/hadoop-snappy>

```bash
$ cd ~/download
$ git clone "https://github.com/electrum/hadoop-snappy.git"
$ cd hadoop-snappy
$ mvn package -Dsnappy.prefix=/usr/local
$ cp target/hadoop-snappy-0.0.1-SNAPSHOT.tar.gz ~/download
$ cd ~/download
$ tar zxvf hadoop-snappy-0.0.1-SNAPSHOT.tar.gz
```

Copy necessary library to Hadoop

```bash
$ cp -r hadoop-snappy-0.0.1-SNAPSHOT/lib/* <HADOOP_HOME>/lib
```

Here, we suppose `<HADOOP_HOME>` to be `/home/hadoopuser/hadoop`.

Add the following key/value pair into `<HADOOP_HOME>/etc/hadoop/core-site.xml`

```xml
  <property>
    <name>io.compression.codecs</name>
    <value>
      org.apache.hadoop.io.compress.GzipCodec,
      org.apache.hadoop.io.compress.DefaultCodec,
      org.apache.hadoop.io.compress.BZip2Codec,
      org.apache.hadoop.io.compress.SnappyCodec
    </value>
  </property>
```

Now, add the following line to `<HADOOP_HOME>/etc/hadoop/hadoop-env.sh`

```bash
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$HADOOP_HOME/lib/native/Linux-amd64-64/:/usr/local/lib/
```

In the file `<HADOOP_HOME>/etc/hadoop/mapred-site.xml`, add one or several of these configurations about compression as much as you need it:

```xml
<property>
  <name>mapred.output.compress</name>
  <value>false</value>
  <description>Should the job outputs be compressed?</description>
</property>

 

<property>
  <name>mapred.output.compression.type</name>
  <value>RECORD</value>
  <description>If the job outputs are to compressed as SequenceFiles, how should they
 be compressed? Should be one of NONE, RECORD or BLOCK.
  </description>
</property>

<property>
  <name>mapred.output.compression.codec</name>
  <value>org.apache.hadoop.io.compress.DefaultCodec</value>
  <description>If
 the job outputs are compressed, how should they be compressed?
  </description>
</property>

<property>
  <name>mapred.compress.map.output</name>
  <value>false</value>
  <description>Should the outputs of the maps be compressed before being sent
 across the network. Uses SequenceFile compression.
  </description>
</property>

<property>
  <name>mapred.map.output.compression.codec</name>
  <value>org.apache.hadoop.io.compress.DefaultCodec</value>
  <description>If the map outputs are compressed, how should they be compressed?
  </description>
</property>

```

Make sure you make the above changes on all machines of you cluster. 

Then, reboot hadoop on all machines of your cluster. 

For testing the installation, you may upload a txt file to HDFS by the following command

```bash
$ hadoop fs -put txtfile.txt ~/
```

Then run a wordcount program to calculate the word count of that file and store the result on HDFS. 

If everything is fine and the result is written in the text file, then clearly snappy is successfully installed for Hadoop

### Install Snappy for HBase

```bash
$ cd ~/hbase/lib
$ cp -r ~/hadoop/lib/hadoop-snappy-0.0.1-SNAPSHOT.jar ./
$ cp -r ~/hadoop/lib/native ./
```

Edit the file `~/hbase/conf/hbase-env.sh`, add the following lines:

```bash
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$HADOOP_HOME/lib/native/Linux-amd64-64/:/usr/local/lib/
export HBASE_LIBRARY_PATH=$HBASE_LIBRARY_PATH:$HBASE_HOME/lib/native/Linux-amd64-64/:/usr/local/lib/
```

Make this change to all the data nodes as well. 

Restart HBase.

For testing, run the following command:

```bash
$ cd ~/hbase/bin 
$ ./hbase org.apache.hadoop.hbase.util.CompressionTest hdfs://<HDFS_NAME_NODE>:<NAME_NODE_PORT>/output/part-r-00000 snappy
```

If you can see `SUCCESS` at the end of running, then you are good to go. 

Now you may try to create an HBase table to see if snappy is supported. 

```bash
$ cd ~/hbase/bin 
$ ./hbase shell
> create 'tsnappy', { NAME => 'cf', COMPRESSION => 'snappy'}
> describe 'tsnappy'
> put 'tsnappy','row1','f:col1','value'
> scan 'tsnappy'
```

If you passed all test, then you are good to go. Don't forget press `Ctrl+D` to exit HBase console.

### Install Snappy for Spark.

Installing Snappy for Spark is easy. Just add the following lines to the file `<SPARK_HOME>/conf/spark-defaults.conf`

```
spark.executor.extraClassPath	 /home/hadoopuser/hadoop/share/hadoop/common/lib/snappy-java-1.0.4.1.jar
spark.executor.extraLibraryPath    /home/hadoopuser/hadoop/lib/native
spark.driver.extraClassPath	 /home/hadoopuser/hadoop/share/hadoop/common/lib/snappy-java-1.0.4.1.jar
spark.driver.extraLibraryPath    /home/hadoopuser/hadoop/lib/native
```

Make this change to all the computing nodes as well. 

Reboot Spark cluster. 


Back to [README](readme.md)