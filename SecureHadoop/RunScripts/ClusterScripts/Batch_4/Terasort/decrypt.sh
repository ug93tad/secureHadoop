#!/bin/bash
IN=/terasort_compute
OUT=/terasort_decrypt
JAR=../../lib/secureHadoop.jar
JOB=tds.decode.examples.TeraSortDecryptor
CONF=mapred.xml
FILES=../../c++/libhryto.so
ENV="LD_LIBRARY_PATH=/home/dinhtta/local/lib:/home/dinhtta/local/lib64"
#remove 
hadoop dfs -rmr $OUT

hadoop jar $JAR $JOB -conf $CONF -files $FILES -D mapred.child.env=$ENV $TOKEN $IN $OUT
