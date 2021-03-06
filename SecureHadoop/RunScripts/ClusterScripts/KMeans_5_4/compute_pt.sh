#!/bin/bash

. hibench-config.sh
. configure.sh

JAR=../../lib/mahout-examples-0.7-job.jar
JOB=org.apache.mahout.clustering.kmeans.KMeansDriver
CONF=mapred.xml
LIBJARS="../../lib/mahout-core-0.7.jar,../../lib/mahout-math-0.7.jar,../../lib/mahout-examples-0.7-job.jar,../../lib/secureHadoop.jar"
ENV="LD_LIBRARY_PATH=/home/dinhtta/local/lib:/home/dinhtta/SecureHadoop/secureHadoop/SecureHadoop/lib"
FILES=../../c++/libhryto.so

SAMPLE_IN=/HiBench/KMeans/Input/samples
CLUSTER_IN=/HiBench/KMeans/Input/cluster
OUTPUT=/HiBench/KMeans/Output

export HADOOP_CLASSPATH=$HADOOP_CLASSPATH:../../lib/mahout-math-0.7.jar:../../lib/mahout-core-0.7.jar:../../lib/mahout-examples-0.7-job.jar:../../lib/secureHadoop.jar

OPTION="-i ${SAMPLE_IN} -c ${CLUSTER_IN} -o ${OUTPUT_HDFS} -x ${MAX_ITERATION} -ow -cl -cd 0.5 -dm org.apache.mahout.common.distance.SquaredEuclideanDistanceMeasure -xm mapreduce"

hadoop dfs -rmr $OUTPUT

echo OPTION = $OPTION
hadoop jar $JAR $JOB -conf $CONF -files $FILES -D mapred.child.env=$ENV  -libjars $LIBJARS $OPTION
