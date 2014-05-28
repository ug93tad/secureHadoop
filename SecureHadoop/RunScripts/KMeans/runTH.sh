#!/bin/bash

#data already generated

#change mapred.xml (HOM option to false)
sed 's/true/false/g' mapred.xml > o
mv o mapred.xml

#encrypt -> compute -> decrypt -> error
./encrypt.sh
./compute.sh
./decrypt_th.sh
./error_th.sh

#change back mapred.xml to HOM option
sed 's/false/true/g' mapred.xml > o
mv o mapred.xml


