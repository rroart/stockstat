#!/bin/bash

eval "$(conda shell.bash hook)"
conda activate gem

PYTHON3=python3
[ -z "$PYTHON3" ] && PYTHON3=`command -v python3.5`
[ -z "$PYTHON3" ] && PYTHON3=`command -v python3.4`
[ -z "$PYTHON3" ] && PYTHON3=`command -v python3`
$PYTHON3 flaskptmain.py $@
