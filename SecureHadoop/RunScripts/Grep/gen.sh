#!/bin/bash

OUT=/text
JAR=hadoop-mapred-examples-0.22.0.jar
JOB=randomtextwriter
CONF=config.xml
FORMAT=org.apache.hadoop.mapreduce.lib.output.TextOutputFormat
#remove text files
hadoop dfs -rmr $OUT

#then generate new input
hadoop jar $JAR $JOB -conf $CONF -outFormat $FORMAT $OUT
