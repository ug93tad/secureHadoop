#!/bin/bash

DIR=$HOME/SecureHadoop/secureHadoop/SecureHadoop/RunScripts/NewBatch
#running the experiments for performance

#For accuracy
echo Running Pagerank 2K
cd $DIR/PageRank_1
./runAll.sh > out 2> err

sleep 10

echo Running Pagerank 2K, 3 iter
cd $DIR/PageRank_3
./runAll.sh > out 2> err

sleep 10

echo Running Pagerank 2K, 5 iter
cd $DIR/PageRank_5
./runAll.sh > out 2> err

sleep 10

echo Running Pagerank 2K, 7 iter
cd $DIR/PageRank_7
./runAll.sh > out 2> err

sleep 10

echo Running KMeans 2K, 1 iter, 4 clusters
cd $DIR/KMeans_1_4
./runAll.sh > out 2> err

sleep 10

echo Running KMeans 2K, 3 iter, 4 clusters
cd $DIR/KMeans_3_4
./runAll.sh > out 2> err

sleep 10

echo Running KMeans 2K, 5 iter, 4 clusters
cd $DIR/KMeans_5_4
./runAll.sh > out 2> err

sleep 10

echo Running KMeans 2K, 7 iter, 4 clusters
cd $DIR/KMeans_7_4
./runAll.sh > out 2> err

sleep 10

echo Running KMeans 2K, 1 iter, 8 clusters
cd $DIR/KMeans_1_8
./runAll.sh > out 2> err

sleep 10

echo Running KMeans 2K, 3 iter, 8 clusters
cd $DIR/KMeans_3_8
./runAll.sh > out 2> err

sleep 10

echo Running KMeans 2K, 5 iter, 8 clusters
cd $DIR/KMeans_5_8
./runAll.sh > out 2> err

sleep 10

echo Running KMeans 2K, 7 iter, 8 clusters
cd $DIR/KMeans_7_8
./runAll.sh > out 2> err

echo DONE
