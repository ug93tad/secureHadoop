#!/bin/bash
IN=/text_encrypt
OUT=/text_compute
JAR=../../bin/secureHadoop.jar
JOB=tds.compute.examples.Grep
CONF=mapred.xml
FILES=../../c++/libhryto.so
ENV="LD_LIBRARY_PATH=/home/dinhtta/local/lib"
TOKEN=oblongly
#remove 
hadoop dfs -rmr $OUT

hadoop jar $JAR $JOB -conf $CONF -files $FILES -D mapred.child.env=$ENV $TOKEN $IN $OUT
