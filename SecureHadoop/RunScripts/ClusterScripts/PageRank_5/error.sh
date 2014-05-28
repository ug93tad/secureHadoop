#!/bin/bash
. configure.sh

IN_HOM=/HiBench/Pagerank/Output/pr_check
IN_TH=/HiBench/Pagerank/Output/pr_check_th
JAR=../../lib/secureHadoop.jar
JOB=tds.compute.examples.KendallDistance

#pre-processing the file
hadoop dfs -cat /HiBench/Pagerank/Output/pr_check/part-r-00000 | grep ^[0-9] | awk '{print $2}' > o
hadoop dfs -cat /HiBench/Pagerank/Output/pr_check_th/part-r-00000 | grep ^[0-9] | awk '{print $2}' > t

let P10=$PAGES*10/100
let P5=$PAGES*5/100
let P100=$PAGES
echo "top 5"
hadoop jar $JAR $JOB $PAGES 5 o t
echo "top 10"
hadoop jar $JAR $JOB $PAGES 10 o t
echo "top 20"
hadoop jar $JAR $JOB $PAGES 20 o t
echo "5 PERCENT"
hadoop jar $JAR $JOB $PAGES $P5 o t
echo "10 PERCENT"
hadoop jar $JAR $JOB $PAGES $P10 o t
echo "ALL PAGES"
hadoop jar $JAR $JOB $PAGES $PAGES o t
