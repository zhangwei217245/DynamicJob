#!/bin/bash

cd ~
sudo echo ""

wget "http://downloads.lightbend.com/scala/2.11.8/scala-2.11.8.tgz"

tar zxvf scala-2.11.8.tgz

mv scala-2.11.8 scala

wget "http://d3kbcqa49mib13.cloudfront.net/spark-1.6.1-bin-hadoop2.6.tgz"

tar zxvf spark-1.6.1-bin-hadoop2.6.tgz

mv spark-1.6.1-bin-hadoop2.6 spark

SPARK_RC=~/.spark_rc

SLAVEFILE=/home/hadoopuser/spark/slaves
cat slaves > $SLAVEFILE

echo 'export SCALA_HOME=/home/hadoopuser/scala' > $SPARK_RC 
echo 'export SPARK_HOME=/home/hadoopuser/spark' >> $SPARK_RC
echo 'export PATH=$SCALA_HOME/bin:$SPARK_HOME/bin:$SPARK_HOME/sbin:$PATH' >> $SPARK_RC
sudo chmod +x $SPARK_RC

echo "if [ -f $SPARK_RC ]; then" >> ~/.bashrc
echo "  . $SPARK_RC" >> ~/.bashrc
echo "fi" >> ~/.bashrc

cp -f spark/conf/spark-env.sh.template spark/conf/spark-env.sh

MASTERIP=`head -1 $SLAVEFILE`

echo 'SPARK_JAVA_OPTS=-Dspark.driver.port=53411' >> spark/conf/spark-env.sh
echo 'HADOOP_CONF_DIR=$HADOOP_HOME/conf' >> spark/conf/spark-env.sh
echo "SPARK_MASTER_IP=$MASTERIP" >> spark/conf/spark-env.sh
echo 'SPARK_LOCAL_DIRS=$SPARK_HOME' >> spark/conf/spark-env.sh


awk '{if (NR>1){print $0}}' $SLAVEFILE | while read line; do scp -r ~/spark $line:~/spark; scp -r ~/.bashrc $line:~/.bashrc; scp -r $SPARK_RC $line:$SPARK_RC; done
