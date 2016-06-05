#!/bin/bash


sudo echo "Suppose that you have successfully configured the password-less login for master and all slave nodes."
echo "If you didn't finish that, please generate key-pair on every machine with 'ssh-keygen -t rsa',"
echo "Then do 'scp ~/.ssh/id_rsa.pub hadoopuser@geotwitter.ttu.edu:~/.ssh/id_rsa.pub.\$HOSTNAME' on every machine"
echo "On master node, do 'cat ~/.ssh/id_rsa.pub* >> ~/.ssh/authorized_keys'"
echo "On master node, do 'scp ~/.ssh/authorized_keys hadoopuser@slave:~/.ssh/' to distributed authorized keys on every slave node."

HOSTNM=`hostname`

cd ~
wget "http://www.trieuvan.com/apache/hadoop/common/hadoop-2.7.2/hadoop-2.7.2.tar.gz"
tar zxvf hadoop-2.7.2.tar.gz
mv hadoop-2.7.2 hadoop

HADOOP_RC=~/.hadooprc

echo "# Set HADOOP_HOME" > $HADOOP_RC 
echo "export HADOOP_HOME=/home/hadoopuser/hadoop" >> $HADOOP_RC 
echo "# Set JAVA_HOME" >> $HADOOP_RC 
echo "export JAVA_HOME=/usr/lib/jvm/java-8-oracle" >> $HADOOP_RC 
echo "# Add Hadoop bin and sbin directory to PATH" >> $HADOOP_RC 
echo 'export PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin' >> $HADOOP_RC 

sudo chmod +x $HADOOP_RC

echo "if [ -f $HADOOP_RC ]; then" >> ~/.bashrc
echo "	. $HADOOP_RC" >> ~/.bashrc
echo "fi" >> ~/.bashrc

mkdir -p hdfs/namenode
mkdir -p hdfs/datanode

wget "https://s3-us-west-2.amazonaws.com/apprepo4xspirit/hadoop/conf/hadoop_confs.tar.gz"
tar zxvf hadoop_confs.tar.gz

ls -l hadoop_confs | grep ".xml"|grep -v "mapred-site.xml" |awk '{print $NF}'|while read line; do cp hadoop/etc/hadoop/$line hadoop/etc/hadoop/$line.bak; cp -f hadoop_confs/$line hadoop/etc/hadoop/ ; done

ls -l hadoop_confs | grep "env.sh" |awk '{print $NF}'|while read line; do cp hadoop/etc/hadoop/$line hadoop/etc/hadoop/$line.bak; cp -f hadoop_confs/$line hadoop/etc/hadoop/ ; done

MASTERFILE=/home/hadoopuser/hadoop/etc/hadoop/masters
SLAVEFILE=/home/hadoopuser/hadoop/etc/hadoop/slaves
cat slaves > $SLAVEFILE

head -1 $SLAVEFILE > $MASTERFILE

awk '{if(NR==1){print $0}}' $SLAVEFILE| while read line; do scp -r ~/hadoop $line:~/hadoop; scp -r ~/.bashrc $line:~/.bashrc; scp -r $HADOOP_RC $line:$HADOOP_RC; done

ls -l hadoop_confs | grep "mapred-site.xml" |awk '{print $NF}'|while read line; do cp hadoop/etc/hadoop/$line hadoop/etc/hadoop/$line.bak; cp -f hadoop_confs/$line hadoop/etc/hadoop/;done

exit 0;
