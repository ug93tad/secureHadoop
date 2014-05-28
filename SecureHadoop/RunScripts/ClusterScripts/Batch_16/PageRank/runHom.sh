#!/bin/bash

#data already generated

#encrypt -> compute -> decrypt -> error
time ./encrypt.sh
echo ******** DONE ENCRYPTION *********
time ./compute.sh
echo ******** DONE COMPUTE *********
