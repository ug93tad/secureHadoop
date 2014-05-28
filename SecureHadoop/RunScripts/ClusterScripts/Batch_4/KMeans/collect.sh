#!/bin/bash

for (( i=1; i<9; i++ ))
do
	NODE=awan-2-0$i-0
	mkdir -p logs/hadoop/$NODE
	scp -r $NODE:/data1/anh/logs/userlogs $1/$NODE/
	#then delete
	ssh $NODE "rm -rf /data1/anh/logs/userlogs"
done
