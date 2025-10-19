#!/usr/bin/python3

import classify
import json
import sys

from multiprocessing import Process, Queue

import config

dataset = classify.Classify()
queue = Queue()
cache = {}

def learn(ds = "mnist", path = None, cf = config.PYTORCHMLP, steps = None, take = None, q = False, override = None):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : False, 'mllearn' : True }
    cfname, modelInt, thecf = config.get(cf)
    print("cfname", cfname)
    print("modelInt", modelInt)
    print("thecf", thecf)
    if steps is not None:
        thecf['steps'] = steps
    if take is not None:
        thecf['take'] = take
    if override is not None:
        thecf.update(override)
    myds = getdsname(ds)
    filename = getfilename(thecf, myds)
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'path' : path, 'filename' : filename, 'classifyarray' : None, 'neuralnetcommand' : neuralnetcommand, cfname : thecf, 'zero' : True }
    if q:
        data['normalizevalue'] = 255.0
    cachedata = cache.get(cf+myds)
    myjson = json.dumps(data)
    result = dataset.do_dataset(queue, myjson)
    #cache[cf+myds] = response
    #result = queue.get()
    print (result)
    return result

def classify(text, ds = "mnist", path = None, cf = config.PYTORCHMLP, take = None, size = 40):
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

def getfilename(cf, ds):
    return cf['name'] + ds

def getdsname(ds):
    if isinstance(ds, list):
        myds = str(ds[0]) + str(ds[1])
    else:
        myds = ds
    return myds
