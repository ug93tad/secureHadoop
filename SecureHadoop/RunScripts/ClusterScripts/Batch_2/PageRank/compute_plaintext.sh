#!/bin/bash
NODES=100
REDUCERS=1
ITERATIONS=2
EDGE_IN=/HiBench/Pagerank/Input/edges_original
OUTPUT=/HiBench/Pagerank/Output

JAR=../../bin/secureHadoop.jar
JOB=tds.compute.examples.PagerankPlaintext
CHECKJOB=tds.compute.examples.PagerankPlaintextCheck
CONF=mapred.xml
FILES=../../c++/libhryto.so
LIBJARS="../../lib/mahout-core-0.7.jar,../../lib/mahout-math-0.7.jar"
ENV="LD_LIBRARY_PATH=/home/dinhtta/local/lib:/home/dinhtta/SecureHadoop/secureHadoop/SecureHadoop/lib"
#remove 
hadoop dfs -rmr $OUTPUT /pr_tmp 

hadoop jar $JAR $JOB -conf $CONF -files $FILES -D mapred.child.env=$ENV -libjars $LIBJARS $EDGE_IN $OUTPUT $NODES  $REDUCERS $ITERATIONS nosym new
hadoop jar $JAR $CHECKJOB -conf $CONF -files $FILES -D mapred.child.env=$ENV -libjars $LIBJARS /HiBench/Pagerank/Output/pr_vector /HiBench/Pagerank/Output/pr_check
