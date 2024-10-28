#!/usr/bin/python3

#exec(open("./lib.py").read())

import os

from datetime import datetime, timedelta
import config
import request
import multiprocessing as mp
import json

from collections import OrderedDict

def learn(ds = None, path = None, cf = 'tensorflowMiniatureGPTConfig', steps = None, take = None, vocab = None):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : False, 'mllearn' : True }
    cfname, modelInt, thecf = config.get(cf)
    if steps is not None:
        thecf['steps'] = steps
    if take is not None:
        thecf['take'] = take
    if vocab is not None:
        thecf['vocab'] = vocab
    myds = getdsname(ds)
    filename = getfilename(thecf, myds)
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'path' : path, 'filename' : filename, 'classifyarray' : None, 'neuralnetcommand' : neuralnetcommand, cfname : thecf }
    response = request.gptrequest1(cf, myds, data)
    print(response.text)
    print(response)
    myobj = response.json() #.loads(response.text) #request.get_data(as_text=True)

def chat(text, ds = None, path = None, cf = 'tensorflowMiniatureGPTConfig', take = None, size = 40):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : True, 'mllearn' : False }
    cfname, modelInt, thecf = config.get(cf)
    myds = getdsname(ds)
    filename = getfilename(thecf, myds)
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'path' : path, 'filename' : filename, 'classifyarray' : [ text ], 'classes' : size, 'neuralnetcommand' : neuralnetcommand, cfname : thecf }
    if take is not None:
        thecf['take'] = take
    response = request.gptrequest1(cf, myds, data)
    print(response.text)
    print(response)
    myobj = response.json() #.loads(response.text) #request.get_data(as_text=True)

def getdsname(ds):
    if isinstance(ds, list):
        myds = str(ds[0]) + str(ds[1])
    else:
        myds = ds
    return myds

def getfilename(cf, ds):
    return cf['name'] + ds

