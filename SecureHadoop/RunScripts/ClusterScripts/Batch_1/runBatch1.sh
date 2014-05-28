#!/bin/bash

DIR=$HOME/SecureHadoop/secureHadoop/SecureHadoop/RunScripts/Batch_1
#running the experiments for performance

cd $DIR
./setup.sh

#Wordcount
echo Running Wordcount
cd $DIR/Wordcount
./runAll.sh > out 2> err

echo Running Grep
cd $DIR/Grep
./runAll.sh > out 2> err

echo Running Terasort
cd $DIR/Terasort
./runAll.sh > out 2> err

echo Running aggregate
cd $DIR/Aggregate
./runAll.sh > out 2> err

echo Running Pagerank
cd $DIR/PageRank
./runAll.sh > out 2> err

echo Runnign KMeans
cd $DIR/KMeans
./runAll.sh > out 2> err
