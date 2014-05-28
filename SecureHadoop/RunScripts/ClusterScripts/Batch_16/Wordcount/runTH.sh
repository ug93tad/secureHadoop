#!/bin/bash

#data already generated
#change mapred.xml (HOM option to false)
sed 's/true/false/g' mapred.xml > o
mv o mapred.xml

#encrypt -> compute -> decrypt -> error
time ./encrypt.sh
time ./compute_th.sh
time ./decrypt.sh

#change back mapred.xml to HOM option
sed 's/false/true/g' mapred.xml > o
mv o mapred.xml

