#!/usr/bin/python3

import classify
import json
import sys

from multiprocessing import Process, Queue

import config

cl = classify.Classify()
queue = Queue()
cache = {}

def learn(cf = 'tensorflowMLPConfig', size = None, classes = None, train_x = None, train_y = None, test_x = None, test_y = None, classifyarray = None, steps = None, zero = True):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : False, 'mllearn' : True }
    cfname, modelInt, thecf = config.get(cf)
    if steps is not None:
        thecf['steps'] = steps
    filename = getfilename(thecf, "ds")
    data = { 'modelInt' : modelInt, 'filename' : filename, 'size' : size, 'classes' : classes, 'trainingarray' : train_x, 'trainingcatarray' : train_y, 'testarray' : test_x, 'testcatarray' : test_y, 'classifyarray' : classifyarray, 'neuralnetcommand' : neuralnetcommand, cfname : thecf, 'zero' : zero, 'classify' : False }
    myjson = json.dumps(data)
    response = cl.do_learntestclassify(queue, myjson)
    result = queue.get()
    print (result)
    return result

def classify(text, path = None, cf = 'tensorflowMLPConfig', take = None, size = 40):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : True, 'mllearn' : False }
    cfname, modelInt, thecf = config.get(cf)
    if take is not None:
        thecf['take'] = take
    myds = getdsname(ds)
    filename = getfilename(thecf, myds)
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'path' : path, 'filename' : filename, 'classifyarray' : [ text ], 'classes' : size, 'neuralnetcommand' : neuralnetcommand, cfname : thecf }
    cachedata = cache.get(cf+myds)
    myjson = json.dumps(data)
    response = cl.do_gpt(queue, myjson, cachedata)
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
