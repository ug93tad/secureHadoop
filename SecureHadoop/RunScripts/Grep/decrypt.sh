#!/bin/bash
IN=/text_compute
OUT=/text_decrypt
JAR=../../bin/secureHadoop.jar
JOB=tds.decode.examples.GrepDecryptor
CONF=mapred.xml
FILES=../../c++/libhryto.so
ENV="LD_LIBRARY_PATH=/home/dinhtta/local/lib"
#remove 
hadoop dfs -rmr $OUT

hadoop jar $JAR $JOB -conf $CONF -files $FILES -D mapred.child.env=$ENV $IN $OUT
