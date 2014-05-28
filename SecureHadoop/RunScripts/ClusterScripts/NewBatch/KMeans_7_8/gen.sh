#!/bin/bash

#configuration for uservisits is in configure.sh
hadoop dfs -rmr /HiBench/KMeans/Input /HiBench/KMeans/Encrypted*

. hibench-config.sh
. configure.sh

JAR=../../lib/datatools.jar
JOB=org.apache.mahout.clustering.kmeans.GenKMeansDatasetInteger
CONF=mapred.xml
LIBJARS="../../lib/mahout-core-0.7.jar,../../lib/mahout-math-0.7.jar,../../lib/secureHadoop.jar"

export HADOOP_CLASSPATH=$HADOOP_CLASSPATH:../../lib/mahout-math-0.7.jar:../../lib/mahout-core-0.7.jar

# generate data
OPTION="-sampleDir ${INPUT_SAMPLE} -clusterDir ${INPUT_CLUSTER} -numClusters ${NUM_OF_CLUSTERS} -numSamples ${NUM_OF_SAMPLES} -samplesPerFile ${SAMPLES_PER_INPUTFILE} -sampleDimension ${DIMENSIONS}"

echo OPTIONS = $OPTION
hadoop jar $JAR $JOB -libjars $LIBJARS $OPTION
