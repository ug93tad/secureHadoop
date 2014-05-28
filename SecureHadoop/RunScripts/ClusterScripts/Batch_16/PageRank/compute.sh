#!/bin/bash
. configure.sh

EDGE_IN=/HiBench/Pagerank/Input/edges
PR_IN=/HiBench/Pagerank/Input/pr_vector_encrypt
PREF_IN=/HiBench/Pagerank/Input/pref_vector_encrypt
PR_OUT=/HiBench/Pagerank/Input/pr_out_encrypt
VERIFICATION=/HiBench/Pagerank/Output/pr_check
JAR=../../lib/secureHadoop.jar
JOB=tds.compute.examples.Pagerank
CONF=mapred.xml
FILES=../../c++/libhryto.so
LIBJARS="../../lib/mahout-core-0.7.jar,../../lib/mahout-math-0.7.jar"
ENV="LD_LIBRARY_PATH=/home/dinhtta/local/lib:/home/dinhtta/local/lib64:/home/dinhtta/SecureHadoop/secureHadoop/SecureHadoop/lib"


#remove 
hadoop dfs -rmr $PR_OUT /pr_tmp $VERIFICATION

hadoop jar $JAR $JOB -conf $CONF -files $FILES -D mapred.child.env=$ENV -libjars $LIBJARS $PAGES  $NUM_REDS $NUM_ITERATIONS $EDGE_IN $PR_IN $PREF_IN $PR_OUT $VERIFICATION
