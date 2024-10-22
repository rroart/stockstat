#!/usr/bin/python3

import classify
import json
import sys

from multiprocessing import Process, Queue

import config

gpt = classify.Classify()
queue = Queue()
cache = {}

def learn(ds = None, path = None, cf = 'tensorflowMiniatureGPTConfig', steps = None, take = None, vocab = None):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : False, 'mllearn' : True }
    cfname, modelInt, thecf = config.get(cf)
    if steps is not None:
        thecf['steps'] = steps
    if take is not None:
        thecf['take'] = take
    if vocab is not None:
        thecf['vocab'] = vocab
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'filename' : path, 'classifyarray' : None, 'neuralnetcommand' : neuralnetcommand }
    data[cfname] = thecf
    if isinstance(ds, list):
        myds = str(ds[0]) + str(ds[1])
    else:
        myds = ds
    cachedata = cache.get(cf+myds)
    myjson = json.dumps(data)
    response = gpt.do_gpt(queue, myjson, cachedata)
    cache[cf+myds] = response
    result = queue.get()
    print (result)

def chat(text, ds = None, path = None, cf = 'tensorflowMiniatureGPTConfig', take = None, size = 40):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : True, 'mllearn' : False }
    cfname, modelInt, thecf = config.get(cf)
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'filename' : path, 'classifyarray' : [ text ], 'classes' : size, 'neuralnetcommand' : neuralnetcommand }
    data[cfname] = thecf
    if take is not None:
        thecf['take'] = take
    if isinstance(ds, list):
        myds = str(ds[0]) + str(ds[1])
    else:
        myds = ds
    cachedata = cache.get(cf+myds)
    myjson = json.dumps(data)
    response = gpt.do_gpt(queue, myjson, cachedata)
    cache[cf+myds] = response
    result = queue.get()
    print (result)
