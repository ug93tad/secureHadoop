#!/bin/bash

#data already generated

#change mapred.xml (HOM option to false)
sed 's/true/false/g' mapred.xml > o
mv o mapred.xml

#encrypt -> compute -> decrypt -> error
time ./encrypt.sh
time ./compute.sh
time ./decrypt_th.sh
time ./error_th.sh

#change back mapred.xml to HOM option
sed 's/false/true/g' mapred.xml > o
mv o mapred.xml


