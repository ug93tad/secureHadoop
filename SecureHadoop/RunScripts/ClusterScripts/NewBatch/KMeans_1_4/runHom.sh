#!/bin/bash

#data already generated

#encrypt -> compute -> decrypt -> error
time ./encrypt.sh
time ./compute.sh
time ./decrypt.sh
time ./error.sh


