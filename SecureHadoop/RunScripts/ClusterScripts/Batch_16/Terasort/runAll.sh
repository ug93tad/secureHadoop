#!/bin/bash

#generate data
./gen.sh 1600000 16 > logs/gen_out$1 2> logs/gen_err$1
sleep 3


#first argument to suffix the log files
./runHom.sh 16 > logs/hom_out$1 2> logs/hom_err$1

sleep 10

./runTH.sh 16 > logs/th_out$1 2> logs/th_err$1

