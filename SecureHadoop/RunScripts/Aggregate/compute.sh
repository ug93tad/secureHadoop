#!/bin/bash
IN=/HiBench/Hive/Output/aggregate
OUT=/HiBench/Hive/Output/aggregate_compute
JAR=../../lib/secureHadoop.jar
JOB=tds.compute.examples.Aggregate
CONF=mapred.xml
FILES=../../c++/libhryto.so
LIBJARS="../../lib/mahout-core-0.7.jar,../../lib/mahout-math-0.7.jar"
ENV="LD_LIBRARY_PATH=/home/dinhtta/local/lib:/home/dinhtta/SecureHadoop/secureHadoop/SecureHadoop/lib"
#remove 
hadoop dfs -rmr $OUT

hadoop jar $JAR $JOB -conf $CONF -files $FILES -D mapred.child.env=$ENV -libjars $LIBJARS $IN $OUT
