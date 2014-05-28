#!/bin/bash

rm -rf logs/hadoop
mkdir -p logs/hadoop/hom
mkdir -p logs/hadoop/th

#generate data
./gen.sh > logs/gen_out$1 2> logs/gen_err$1
sleep 3

#first argument to suffix the log files
./runHom.sh > logs/hom_out$1 2> logs/hom_err$1
./collect.sh logs/hadoop/hom

sleep 10

./runTH.sh > logs/th_out$1 2> logs/th_err$1
./collect.sh logs/hadoop/th
