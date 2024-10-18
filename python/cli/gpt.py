#!/usr/bin/python3

#exec(open("./lib.py").read())

import os

from datetime import datetime, timedelta
import config
import request
import multiprocessing as mp
import json

from collections import OrderedDict

def learn(ds = None, path = None, cf = 'tensorflowMiniatureGPTConfig', steps = None, take = None):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : False, 'mllearn' : True }
    cfname, modelInt, thecf = config.get(cf)
    if steps is not None:
        thecf['steps'] = steps
    if take is not None:
        thecf['take'] = take
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'filename' : path, 'classifyarray' : None, 'neuralnetcommand' : neuralnetcommand }
    data[cfname] = thecf
    if ds is None:
        ds = 'gpt2';
    response = request.gptrequest1(ds, data)
    print(response.text)
    print(response)
    myobj = response.json() #.loads(response.text) #request.get_data(as_text=True)

def chat(text, ds = None, path = None, cf = 'tensorflowMiniatureGPTConfig', take = None):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : True, 'mllearn' : False }
    cfname, modelInt, thecf = config.get(cf)
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'filename' : path, 'classifyarray' : [ text ], 'neuralnetcommand' : neuralnetcommand }
    data[cfname] = thecf
    if take is not None:
        thecf['take'] = take
    if ds is None:
        ds = 'gpt2';
    response = request.gptrequest1(ds, data)
    print(response.text)
    print(response)
    myobj = response.json() #.loads(response.text) #request.get_data(as_text=True)
