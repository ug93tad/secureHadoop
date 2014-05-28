#!/bin/bash
. configure.sh

REDUCERS=1
PR_IN=/HiBench/Pagerank/Input/pr_vector
PREF_IN=/HiBench/Pagerank/Input/pref_vector
EDGE_IN=/HiBench/Pagerank/Input/edges
PR_OUT=/HiBench/Pagerank/Input/pr_vector_encrypt
PREF_OUT=/HiBench/Pagerank/Input/pref_vector_encrypt

JAR=../../lib/secureHadoop.jar
JOB=tds.encode.examples.PagerankEncryptor
CONF=mapred.xml
FILES=../../c++/libhryto.so
LIBJARS="../../lib/mahout-core-0.7.jar,../../lib/mahout-math-0.7.jar"
ENV="LD_LIBRARY_PATH=/home/dinhtta/local/lib:/home/dinhtta/SecureHadoop/secureHadoop/SecureHadoop/lib"
#remove 
hadoop dfs -rmr $PR_OUT $PREF_OUT

hadoop jar $JAR $JOB -conf $CONF -files $FILES -D mapred.child.env=$ENV -libjars $LIBJARS $PAGES  $REDUCERS $PR_IN $PREF_IN $EDGE_IN $PR_OUT $PREF_OUT
