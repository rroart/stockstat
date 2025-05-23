#!/usr/bin/python3

import classify
import json
import sys

from multiprocessing import Process, Queue

import config

dataset = classify.Classify()
queue = Queue()
cache = {}

def learn(ds = "mnist", path = None, cf = 'tensorflowMLPConfig', steps = None, take = None, q = False):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : False, 'mllearn' : True }
    cfname, modelInt, thecf = config.get(cf)
    if steps is not None:
        thecf['steps'] = steps
    if take is not None:
        thecf['take'] = take
    myds = getdsname(ds)
    filename = getfilename(thecf, myds)
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'path' : path, 'filename' : filename, 'classifyarray' : None, 'neuralnetcommand' : neuralnetcommand, cfname : thecf, 'zero' : True }
    if q:
        data['normalizevalue'] = 255.0
    cachedata = cache.get(cf+myds)
    myjson = json.dumps(data)
    response = dataset.do_dataset(queue, myjson)
    cache[cf+myds] = response
    result = queue.get()
    print (result)
    return result

def classify(text, ds = "mnist", path = None, cf = 'tensorflowMLPConfig', take = None, size = 40):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : True, 'mllearn' : False }
    cfname, modelInt, thecf = config.get(cf)
    if take is not None:
        thecf['take'] = take
    myds = getdsname(ds)
    filename = getfilename(thecf, myds)
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'path' : path, 'filename' : filename, 'classifyarray' : [ text ], 'classes' : size, 'neuralnetcommand' : neuralnetcommand, cfname : thecf }
    cachedata = cache.get(cf+myds)
    myjson = json.dumps(data)
    response = dataset.do_gpt(queue, myjson, cachedata)
    cache[cf+myds] = response
    result = queue.get()
    print (result)
    return result

def getfilename(cf, ds):
    return cf['name'] + ds

def getdsname(ds):
    if isinstance(ds, list):
        myds = str(ds[0]) + str(ds[1])
    else:
        myds = ds
    return myds
