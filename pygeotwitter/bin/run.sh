#!/bin/bash

if [ -f ./UAG.ctf ]
then
	python3 TwitterExtractor.py $@
else
	python3 TwitterExtractor.py -h
	echo "Do you confirm that you've read the instructions?"
	read input
	if [ "xY"="x$input" ];then
		python3 TwitterExtractor.py -h 2>&1 > ./UAG.ctf
	else
		echo "See you next time."
	fi
fi
