#!/bin/bash

DIR=$HOME/SecureHadoop/secureHadoop/SecureHadoop/RunScripts
#running the experiments for performance

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


#For accuracy
echo Running Pagerank 2K
cd $DIR/PageRank_1
./runAll.sh > out 2> err

echo Running Pagerank 2K, 3 iter
cd $DIR/PageRank_3
./runAll.sh > out 2> err

echo Running Pagerank 2K, 5 iter
cd $DIR/PageRank_5
./runAll.sh > out 2> err

echo Running KMeans 2K, 1 iter, 4 clusters
cd $DIR/KMeans_1_4
./runAll.sh > out 2> err

echo Running KMeans 2K, 1 iter, 8 clusters
cd $DIR/KMeans_1_8
./runAll.sh > out 2> err

echo Running KMeans 2K, 3 iter, 4 clusters
cd $DIR/KMeans_3_4
./runAll.sh > out 2> err

echo Running KMeans 2K, 3 iter, 8 clusters
cd $DIR/KMeans_3_8
./runAll.sh > out 2> err

echo Running KMeans 2K, 5 iter, 4 clusters
cd $DIR/KMeans_5_4
./runAll.sh > out 2> err

echo Running KMeans 2K, 5 iter, 8 clusters
cd $DIR/KMeans_5_8
./runAll.sh > out 2> err

echo Running KMeans 2K, 3 iter, 8 clusters, 8 dimensions
cd $DIR/KMeans_3_8_8
./runAll.sh > out 2> err
