#!/bin/bash

if [ -d ~/tensorflow ]; then
    source ~/tensorflow/bin/activate
fi

rm -rf /tmp/tf*

python3 flasktfmain.py



