#!/bin/bash
OUT=/terasort
JAR=hadoop-mapred-examples-0.22.0.jar
JOB=teragen
MAPS="-D mapreduce.job.maps="$2
#remove text files
hadoop dfs -rmr $OUT

#then generate new input, $1=number of row, $2=number of maps
hadoop jar $JAR $JOB $MAPS $1 $OUT
