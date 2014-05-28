#!/bin/bash

JAR=../../lib/datatools.jar
JOB=org.apache.mahout.clustering.kmeans.KMeansDataEncryptor
CONF=mapred.xml
LIBJARS="../../lib/mahout-core-0.7.jar,../../lib/mahout-math-0.7.jar,../../lib/secureHadoop.jar"
ENV="LD_LIBRARY_PATH=/home/dinhtta/local/lib:/home/dinhtta/SecureHadoop/secureHadoop/SecureHadoop/lib"
FILES=../../c++/libhryto.so

SAMPLE_IN=/HiBench/KMeans/Input/samples
SAMPLE_OUT=/HiBench/KMeans/EncryptedInput/samples
CLUSTER_IN=/HiBench/KMeans/Input/cluster/part-00000
CLUSTER_OUT=/HiBench/KMeans/EncryptedInput/cluster/part-00000

export HADOOP_CLASSPATH=$HADOOP_CLASSPATH:../../lib/mahout-math-0.7.jar:../../lib/mahout-core-0.7.jar:../../lib/secureHadoop.jar

# generate data
#OPTION="-sampleDir ${INPUT_SAMPLE} -clusterDir ${INPUT_CLUSTER} -numClusters ${NUM_OF_CLUSTERS} -numSamples ${NUM_OF_SAMPLES} -samplesPerFile ${SAMPLES_PER_INPUTFILE} -sampleDimension ${DIMENSIONS}"

hadoop dfs -rmr $SAMPLE_OUT $CLUSTER_OUT

echo OPTIONS = $JAR $JOB -files $FILES -D mapred.child.env=$ENV  -libjars $LIBJARS $SAMPLE_IN $SAMPLE_OUT $CLUSTER_IN $CLUSTER_OUT
hadoop jar $JAR $JOB -conf $CONF -files $FILES -D mapred.child.env=$ENV  -libjars $LIBJARS $SAMPLE_IN $SAMPLE_OUT $CLUSTER_IN $CLUSTER_OUT
