#!/bin/bash

#stop map-reduce
DIR=/home/dinhtta/Hadoop/hadoop-0.22.0

cd $DIR/bin
./stop-dfs.sh
./stop-mapred.sh

#restart Hadoop
cd $DIR/conf
rm -rf slaves
cp slaves_2 slaves

cd $DIR/bin
./start-dfs.sh
sleep 5
hadoop dfsadmin -safemode leave
./start-mapred.sh
sleep 20
