#!/bin/bash

if [ -d ~/tensorflow ]; then
    source ~/tensorflow/bin/activate
fi

rm -rf /tmp/tf*

ulimit -n 16384

python3 flasktfmain.py



