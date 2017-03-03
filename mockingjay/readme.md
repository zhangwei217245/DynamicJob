Back to [Project README](/README.md)

# Introduction

This is a document about reproducing the data mining using Spark and Hbase. 

What is written here is specifically for **Hadoop 2.7.2**, **HBase 1.1.5** and **Spark 1.6.1**

# Installing Hadoop, HBase, Spark

See [Installation Guide](ProcessingFrameworkInstallation.md) for details.

# Running the Data Mining Program 

## Preparing HBase Table.

In HBase shell, type the following

```
create twitterUser, {NAME => 'tweet', COMPRESSION => 'SNAPPY'}, {NAME => 'user', COMPRESSION => 'SNAPPY'}
create sent_blue_red_2012, {NAME => 'tsent', COMPRESSION => 'SNAPPY'}
create machineLearn, {NAME => 'Age', COMPRESSION => 'SNAPPY'}, {NAME => 'Gender', COMPRESSION => 'SNAPPY'}, {NAME => 'Race_County', COMPRESSION => 'SNAPPY'}, {NAME => 'Race_State', COMPRESSION => 'SNAPPY'}, {NAME => 'Race_Tract', COMPRESSION => 'SNAPPY'}, {NAME => 'location', COMPRESSION => 'SNAPPY'}, {NAME => 'political', COMPRESSION => 'SNAPPY'}, {NAME => 'username', COMPRESSION => 'SNAPPY'}
```

You may follow the link <https://learnhbase.wordpress.com/2013/03/02/hbase-shell-commands/> for more details on hbase shell commands.

## Prepare raw data in HDFS

```bash
$ hadoop fs -mkdir geotwitter
$ hadoop fs -put <path_to_raw_data> geotwitter/
```

This will take quite a while. Just be patient.

## Prepare important census data

Export census shapefile data into CSV file, including the column of WKT format for boundaries of areas.
Also, make sure you have the facebook surname data and SSO surname data. 

Upload them to `geotwitterCSV`

```bash
$ hadoop fs -mkdir geotwitterCSV
$ hadoop fs -put *.csv geotwitterCSV
```

You should have the following files:

```
-rw-r--r--   1 hadoopuser supergroup   22665036 2016-11-05 19:02 geotwitterCSV/1912_2000.txt
-rw-r--r--   1 hadoopuser supergroup  179301885 2016-11-18 15:53 geotwitterCSV/county.csv
-rw-r--r--   1 hadoopuser supergroup     317077 2016-11-05 19:02 geotwitterCSV/firstname_list.csv
-rw-r--r--   1 hadoopuser supergroup   20249573 2016-11-18 15:53 geotwitterCSV/state.csv
-rw-r--r--   1 hadoopuser supergroup    3941347 2016-11-04 00:57 geotwitterCSV/surname.csv
-rw-r--r--   1 hadoopuser supergroup  943458812 2016-11-18 15:52 geotwitterCSV/tract.csv
```

## Run your Spark jobs

Each time when you make change to the source code of this spark project, then you have to run the following command to compile the entire project and then upload the jar file to HDFS. 

#### 1. Download the project code

```bash
$ mkdir ~/software/; cd ~/software
$ git clone "git@gitlab.com:zhangwei217245/DynamicJob.git" 
```
#### 2. Update the code

```
$ cd ~/software/DynamicJob
$ mvn clean package
```
#### 3. Upload distribution to HDFS

```bash
$ hadoop fs -mkdir spark_job
$ hadoop fs -put -f /home/hadoopuser/software/DynamicJob/mockingjay/target/mockingjay-1.1-SNAPSHOT-jar-with-dependencies.jar /user/hadoopuser/spark_job/
```
#### 4. Run spark job to import raw data into HBase Table `twitterUser`

```bash
$ cd ~/spark/bin
$ ./spark-submit --class x.spirit.dynamicjob.mockingjay.importer.FeedImporter --master spark://geotwitter.ttu.edu:6066 --deploy-mode cluster hdfs://geotwitter.ttu.edu:54310/user/hadoopuser/spark_job/mockingjay-1.1-SNAPSHOT-jar-with-dependencies.jar 2012
```
Notice that the argument `2012` here corresponds to the table `sent_blue_red_2012`. For 2016, you need to pass another parameter. And you possibly need to create another table in advance, and make corresponding changes to the code. 
#### 5. Run spark job to do sentiment analysis on the basis of `twitterUser` table and put the output into `sent_blue_red_2012` table.

```bash
$ cd ~/spark/bin
$ ./spark-submit --class x.spirit.dynamicjob.mockingjay.twitter.UserSentiment --master spark://geotwitter.ttu.edu:6066 --deploy-mode cluster hdfs://geotwitter.ttu.edu:54310/user/hadoopuser/spark_job/mockingjay-1.1-SNAPSHOT-jar-with-dependencies.jar
```
#### 6. Run spark job to do clustering based residential locating.

```bash
$ cd ~/spark/bin
$ ./spark-submit --class x.spirit.dynamicjob.mockingjay.twitter.ResidencyLocator --master spark://geotwitter.ttu.edu:6066 --deploy-mode cluster hdfs://geotwitter.ttu.edu:54310/user/hadoopuser/spark_job/mockingjay-1.1-SNAPSHOT-jar-with-dependencies.jar
```
#### 7. Run spark job to fill in the blanks of those unresolved places.

```bash
$ cd ~/spark/bin
$ ./spark-submit --class x.spirit.dynamicjob.mockingjay.twitter.LocationFillInBlank --master spark://geotwitter.ttu.edu:6066 --deploy-mode cluster hdfs://geotwitter.ttu.edu:54310/user/hadoopuser/spark_job/mockingjay-1.1-SNAPSHOT-jar-with-dependencies.jar
```
#### 8. Run spark job to extract first name and last name in the HBase table

```bash
$ cd ~/spark/bin
$ ./spark-submit --class x.spirit.dynamicjob.mockingjay.twitter.NameExtractor --master spark://geotwitter.ttu.edu:6066 --deploy-mode cluster hdfs://geotwitter.ttu.edu:54310/user/hadoopuser/spark_job/mockingjay-1.1-SNAPSHOT-jar-with-dependencies.jar
```
#### 9. Run spark job to do Age Gender prediction

```bash
$ cd ~/spark/bin
$ ./spark-submit --class x.spirit.dynamicjob.mockingjay.twitter.AgeGenderPredictor --master spark://geotwitter.ttu.edu:6066 --deploy-mode cluster hdfs://geotwitter.ttu.edu:54310/user/hadoopuser/spark_job/mockingjay-1.1-SNAPSHOT-jar-with-dependencies.jar
```
#### 10. Run spark job to predict race probablity. 

```bash
$ cd ~/spark/bin
$ ./spark-submit --class x.spirit.dynamicjob.mockingjay.twitter.RaceProbabilityWithCSV --master spark://geotwitter.ttu.edu:6066 --deploy-mode cluster hdfs://geotwitter.ttu.edu:54310/user/hadoopuser/spark_job/mockingjay-1.1-SNAPSHOT-jar-with-dependencies.jar
```

Once a job is submitted, you can go to <http://geotwitter.ttu.edu:8080/> to check the status of the job you just submitted.

## Generate output

```bash
$ cd ~/spark/bin
$ ./spark-submit --class x.spirit.dynamicjob.mockingjay.exporter.csv.CSVExporter --master spark://geotwitter.ttu.edu:6066 --deploy-mode cluster hdfs://geotwitter.ttu.edu:54310/user/hadoopuser/spark_job/mockingjay-1.1-SNAPSHOT-jar-with-dependencies.jar
```

Now, you can save the result on to your local disk on head node by running:

```bash
$ hadoop fs -ls geotwitterOutput/csv/|grep "geotwitter"|awk '{print $NF}'|awk -F'/' '{print $NF}'| while read line; do echo $line; hadoop fs -cat geotwitterOutput/csv/$line/* > $line; done
```

Everything is done now. 

Please refer to [Output Intepretation](Output.md)