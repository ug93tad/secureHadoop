#!/bin/bash
IN=/text
OUT=/text_encrypt
JAR=../../bin/secureHadoop.jar
JOB=tds.encode.examples.GrepEncryptor
CONF=mapred.xml
FILES=../../c++/libhryto.so
ENV="LD_LIBRARY_PATH=/home/dinhtta/local/lib"
#remove 
hadoop dfs -rmr $OUT

hadoop jar $JAR $JOB -conf $CONF -files $FILES -D mapred.child.env=$ENV $IN $OUT
