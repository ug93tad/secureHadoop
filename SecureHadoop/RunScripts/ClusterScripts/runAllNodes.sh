#!/bin/bash

DIR=/home/dinhtta/SecureHadoop/secureHadoop/SecureHadoop/RunScripts

echo Running with 16 nodes
cd $DIR/Batch_16
./setup.sh
./runBatch1.sh


echo Running with 4 nodes
cd $DIR/Batch_4
./setup.sh
./runBatch1.sh


echo Running with 2 nodes
cd $DIR/Batch_2
./setup.sh
./runBatch1.sh


echo Running with 1 nodes
cd $DIR/Batch_1
./setup.sh
./runBatch1.sh

echo DONE
