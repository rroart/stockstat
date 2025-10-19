#!/bin/bash

#if [ -d ~/tensorflow ]; then
#    source ~/tensorflow/bin/activate
#fi

rm -rf /tmp/tf*

eval "$(conda shell.bash hook)"
conda activate tf220

PYTHON3=`command -v python3.6`
PYTHON3=`command -v python3`
[ -z "$PYTHON3" ] && PYTHON3=`command -v python3.5`
[ -z "$PYTHON3" ] && PYTHON3=`command -v python3.4`
[ -z "$PYTHON3" ] && PYTHON3=`command -v python3`
$PYTHON3 flasktfmain.py $@
