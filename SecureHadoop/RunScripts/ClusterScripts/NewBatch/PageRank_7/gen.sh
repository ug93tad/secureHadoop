#!/bin/bash

#configuration for uservisits is in configure.sh
hadoop dfs -rmr /HiBench/Pagerank

. prepare.sh

ORIGINAL_EDGE=/HiBench/Pagerank/Input/edges_original
NEW_EDGE=/HiBench/Pagerank/Input/edges
JAR=../../lib/secureHadoop.jar
JOB=tds.encode.examples.PagerankGenerator
CONF=mapred.xml
FILES=../../c++/libhryto.so
LIBJARS="../../lib/mahout-core-0.7.jar,../../lib/mahout-math-0.7.jar"
ENV="LD_LIBRARY_PATH=/home/dinhtta/local/lib:/home/dinhtta/SecureHadoop/secureHadoop/SecureHadoop/lib"
PR_VECTOR=/HiBench/Pagerank/Input/pr_vector
PREF_VECTOR=/HiBench/Pagerank/Input/pref_vector
#remove 
echo ARGUMENTS = $JAR $JOB $PAGES $ORIGINAL_EDGE $NEW_EDGE $PR_VECTOR $PREF_VECTOR
hadoop jar $JAR $JOB $PAGES $ORIGINAL_EDGE $NEW_EDGE $PR_VECTOR $PREF_VECTOR
