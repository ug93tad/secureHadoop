#!/bin/bash
IN=/text_encrypt
OUT=/text_compute
JAR=../../bin/secureHadoop.jar
JOB=tds.compute.examples.Wordcount
CONF=mapred.xml
FILES=../../c++/libhryto.so
ENV="LD_LIBRARY_PATH=/home/dinhtta/local/lib"
#remove 
hadoop dfs -rmr $OUT

hadoop jar $JAR $JOB -conf $CONF -files $FILES -D mapred.child.env=$ENV $IN $OUT
