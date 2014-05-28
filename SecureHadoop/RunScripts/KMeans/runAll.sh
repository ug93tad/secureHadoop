#!/bin/bash

LOG_DIR=/home/dinhtta/Hadoop/hadoop-0.22.0/logs/userlogs
rm -rf $LOG_DIR

mkdir -p logs/hadoop/gen
mkdir -p logs/hadoop/hom
mkdir -p logs/hadoop/th

#generate data
./gen.sh > logs/gen_out$1 2> logs/gen_err$1
sleep 3

cp -r $LOG_DIR logs/hadoop/gen/
rm -rf $LOG_DIR

#first argument to suffix the log files
./runHom.sh > logs/hom_out$1 2> logs/hom_err$1

cp -r $LOG_DIR logs/hadoop/hom/
rm -rf $LOG_DIR

sleep 10

./runTH.sh > logs/th_out$1 2> logs/th_err$1
cp -r $LOG_DIR logs/hadoop/th
rm -rf $LOG_DIR
