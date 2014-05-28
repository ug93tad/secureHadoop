#!/bin/bash
IN=/HiBench/Hive/Output/aggregate_compute
OUT=/HiBench/Hive/Output/aggregate_decrypt
JAR=../../lib/secureHadoop.jar
JOB=tds.decode.examples.AggregateDecryptor
CONF=mapred.xml
FILES=../../c++/libhryto.so
LIBJARS="../../lib/mahout-core-0.7.jar,../../lib/mahout-math-0.7.jar"
ENV="LD_LIBRARY_PATH=/home/dinhtta/local/lib:/home/dinhtta/local/lib64:/home/dinhtta/SecureHadoop/secureHadoop/SecureHadoop/lib"
#remove 
hadoop dfs -rmr $OUT

hadoop jar $JAR $JOB -conf $CONF -files $FILES -D mapred.child.env=$ENV -libjars $LIBJARS $IN $OUT
