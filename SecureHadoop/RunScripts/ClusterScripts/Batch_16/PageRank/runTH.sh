#!/bin/bash

#data already generated
#change mapred.xml (HOM option to false)
sed 's/true/false/g' mapred.xml > o
mv o mapred.xml

#encrypt -> compute -> decrypt -> error
time ./encrypt.sh
echo ******* DONE ENCRYPT ***********
time ./compute_th.sh
echo ******* DONE COMPUTE ***********
time ./error.sh
echo ******* DONE ERROR *******

#change back mapred.xml to HOM option
sed 's/false/true/g' mapred.xml > o
mv o mapred.xml

