#!/bin/bash

. hibench-config.sh
. configure.sh

JAR=../../lib/mahout-examples-0.7-job.jar
JOB=org.apache.mahout.clustering.kmeans.encrypt.EncryptedKMeansDriver
CONF=mapred.xml
LIBJARS="../../lib/mahout-core-0.7.jar,../../lib/mahout-math-0.7.jar,../../lib/mahout-examples-0.7-job.jar,../../lib/secureHadoop.jar"
ENV="LD_LIBRARY_PATH=/home/dinhtta/local/lib:/home/dinhtta/SecureHadoop/secureHadoop/SecureHadoop/lib"
FILES=../../c++/libhryto.so

SAMPLE_IN=/HiBench/KMeans/EncryptedInput/samples
CLUSTER_IN=/HiBench/KMeans/EncryptedInput/cluster
OUTPUT=/HiBench/KMeans/EncryptedOutput

export HADOOP_CLASSPATH=$HADOOP_CLASSPATH:../../lib/mahout-math-0.7.jar:../../lib/mahout-core-0.7.jar:../../lib/mahout-examples-0.7-job.jar:../../lib/secureHadoop.jar

# generate data
#OPTION="-sampleDir ${INPUT_SAMPLE} -clusterDir ${INPUT_CLUSTER} -numClusters ${NUM_OF_CLUSTERS} -numSamples ${NUM_OF_SAMPLES} -samplesPerFile ${SAMPLES_PER_INPUTFILE} -sampleDimension ${DIMENSIONS}"

OPTION="-i ${SAMPLE_IN} -c ${CLUSTER_IN} -o ${OUTPUT} -x ${MAX_ITERATION} -ow -cl -cd 0.5 -dm org.apache.mahout.common.distance.encrypt.EncryptedSquaredEuclideanDistanceMeasure -xm mapreduce"

hadoop dfs -rmr $OUTPUT

echo OPTION = $OPTION
hadoop jar $JAR $JOB -conf $CONF -files $FILES -D mapred.child.env=$ENV  -libjars $LIBJARS $OPTION
