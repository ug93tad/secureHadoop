#!/bin/bash
#Compute cluster's square means error

. configure.sh

IN=/clusters_decrypt
JAR=../../lib/datatools.jar
JOB=org.apache.mahout.clustering.test.Error

#prepare, copy part-m-0000i to local
for (( i=0; i<${NUM_OF_CLUSTERS}; i++ ))
do
	hadoop dfs -cat $IN/part-m-0000$i | grep ^[0-9] > o$i
done

#run
LIBJARS="../../lib/mahout-core-0.7.jar,../../lib/mahout-math-0.7.jar,../../lib/secureHadoop.jar,../../lib/mahout-examples-0.7-job.jar"

export HADOOP_CLASSPATH=$HADOOP_CLASSPATH:../../lib/mahout-math-0.7.jar:../../lib/mahout-core-0.7.jar:../../lib/mahout-examples-0.7-job.jar

CONF=mapred.xml
FILES=../../c++/libhryto.so
ENV="LD_LIBRARY_PATH=/home/dinhtta/local/lib:/home/dinhtta/local/lib64:/home/dinhtta/SecureHadoop/secureHadoop/SecureHadoop/lib"
#remove 

hadoop jar $JAR $JOB $DIMENSIONS $NUM_OF_CLUSTERS o  
