#!/bin/bash
#Decrypt the cluster points
. configure.sh

IN=/HiBench/KMeans/EncryptedOutput/clusteredPoints
OUT=/clusters_decrypt
JAR=../../lib/datatools.jar
JOB=org.apache.mahout.clustering.test.EClusters
CONF=mapred.xml
LIBJARS="../../lib/mahout-core-0.7.jar,../../lib/mahout-math-0.7.jar,../../lib/secureHadoop.jar,../../lib/mahout-examples-0.7-job.jar"

export HADOOP_CLASSPATH=$HADOOP_CLASSPATH:../../lib/mahout-math-0.7.jar:../../lib/mahout-core-0.7.jar:../../lib/mahout-examples-0.7-job.jar

CONF=mapred.xml
FILES=../../c++/libhryto.so
ENV="LD_LIBRARY_PATH=/home/dinhtta/local/lib:/home/dinhtta/local/lib64:/home/dinhtta/SecureHadoop/secureHadoop/SecureHadoop/lib"
#remove 
hadoop dfs -rmr $OUT

hadoop jar $JAR $JOB -conf $CONF -files $FILES -D mapred.child.env=$ENV -libjars $LIBJARS $IN $OUT
