#!/bin/bash

#if [ -d ~/tensorflow ]; then
#    source ~/tensorflow/bin/activate
#fi

#rm -rf /tmp/tf*

#PYTHON3=`command -v python3.6`
PYTHON3=python
[ -z "$PYTHON3" ] && PYTHON3=`command -v python3.5`
[ -z "$PYTHON3" ] && PYTHON3=`command -v python3.4`
[ -z "$PYTHON3" ] && PYTHON3=`command -v python3`
$PYTHON3 flaskptmain.py $@