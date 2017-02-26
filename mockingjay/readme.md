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

Now, if you go to `/usr/local/lib`, you will see the following files:

```
-rw-r--r-- 1 root root  536K Feb 26 16:52 libsnappy.a
-rwxr-xr-x 1 root root   955 Feb 26 16:52 libsnappy.la*
lrwxrwxrwx 1 root root    18 Feb 26 16:52 libsnappy.so -> libsnappy.so.1.3.0*
lrwxrwxrwx 1 root root    18 Feb 26 16:52 libsnappy.so.1 -> libsnappy.so.1.3.0*
-rwxr-xr-x 1 root root  265K Feb 26 16:52 libsnappy.so.1.3.0*
```


Note for snappy on HBase and Hadoop.

0. Make your operating system have snappy native library installed. Compile snappy library and install. Configure environment variables.
1. Make HDFS or Hadoop MapReduce/Yarn have snappy installed. Copy or sim-link the library into Hadoop native folder and modify configuration files.
2. Make HBase have snappy installed. Copy or sim-link the library into HBase lib folder, modify configuration files.
3. Make Spark have snappy supported. Modify the configuration file spark-defaults.conf to load snappy library.