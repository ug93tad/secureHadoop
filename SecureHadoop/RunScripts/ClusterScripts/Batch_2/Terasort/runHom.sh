#!/bin/bash

#data already generated

#encrypt -> compute -> decrypt -> error
time ./encrypt.sh
time ./compute.sh $1 
#time ./decrypt.sh
