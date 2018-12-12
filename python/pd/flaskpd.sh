#!/bin/bash

if [ -d ~/tensorflow ]; then
    source ~/tensorflow/bin/activate
fi

python3 flaskpdmain.py



