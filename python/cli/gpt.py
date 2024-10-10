#!/usr/bin/python3

#exec(open("./lib.py").read())

import os

from datetime import datetime, timedelta
import config
import request
import multiprocessing as mp
import json

from collections import OrderedDict

def learn(ds = None, path = None, cf = 'tensorflowMiniatureGPTConfig'):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : False, 'mllearn' : True }
    cfname, modelInt, thecf = config.get(cf)
    thecf['steps'] = 1
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'filename' : path, 'classifyarray' : None, 'neuralnetcommand' : neuralnetcommand }
    data[cfname] = thecf
    response = request.gptrequest1(None, data)
    print(response.text)
    print(response)
    myobj = response.json() #.loads(response.text) #request.get_data(as_text=True)

def chat(text, ds = None, path = None, cf = 'tensorflowMiniatureGPTConfig'):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : True, 'mllearn' : False }
    cfname, modelInt, thecf = config.get(cf)
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'filename' : path, 'classifyarray' : [ text ], 'neuralnetcommand' : neuralnetcommand }
    data[cfname] = thecf
    response = request.gptrequest1(None, data)
    print(response.text)
    print(response)
    myobj = response.json() #.loads(response.text) #request.get_data(as_text=True)
