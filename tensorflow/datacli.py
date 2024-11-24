#!/usr/bin/python3

import classify
import json
import sys

from multiprocessing import Process, Queue

import config

cl = classify.Classify()
queue = Queue()
cache = {}

def learntestclassify(classify_x, cf = 'tensorflowMLPConfig', size = None, classes = None, train_x = None, train_y = None, test_x = None, test_y = None, steps = None, zero = True, classify = True, take = None):
    neuralnetcommand = { 'mldynamic' : True, 'mlclassify' : True, 'mllearn' : True }
    return learntestclassify_inner(cf = cf, steps = steps, size = size, classes = classes, take = take, zero = zero, train_x = train_x, train_y = train_y, test_x = test_x, test_y = test_y , classify_x = classify_x, classify = classify, neuralnetcommand = neuralnetcommand)

def learntest(cf = 'tensorflowMLPConfig', size = None, classes = None, train_x = None, train_y = None, test_x = None, test_y = None, classifyarray = None, steps = None, zero = True, classify = True, take = None):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : False, 'mllearn' : True }
    return learntestclassify_inner(cf = cf, steps = steps, size = size, classes = classes, take = take, zero = zero, train_x = train_x, train_y = train_y, test_x = test_x, test_y = test_y , classify_x = None, classify = classify, neuralnetcommand = neuralnetcommand)

def classify(classify_x, path = None, cf = 'tensorflowMLPConfig', take = None, size = None, train_x = None, train_y = None, zero = True, classify = True):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : True, 'mllearn' : False }
    return learntestclassify_inner(cf = cf, size = size, classes = None, take = take, zero = zero, train_x = classify_x, train_y = [[0]], test_x = [], test_y = [] , classify_x = classify_x, classify = classify, neuralnetcommand = neuralnetcommand)

def learntestclassify_inner(cf='tensorflowMLPConfig', size=None, classes=None, train_x=[], train_y=[], test_x=[],
    test_y=[], classify_x=None, steps=None, zero=True, classify=True, take = None, neuralnetcommand = None):

    cfname, modelInt, thecf = config.get(cf)
    if steps is not None:
        thecf['steps'] = steps
    if take is not None:
        thecf['take'] = take
    filename = getfilename(thecf, "ds")
    data = { 'modelInt' : modelInt, 'filename' : filename, 'size' : size, 'trainingarray' : train_x, 'classifyarray' : classify_x, 'classes' : classes, 'trainingcatarray' : train_y, 'testarray' : test_x, 'testcatarray' : test_y, 'neuralnetcommand' : neuralnetcommand, cfname : thecf, 'zero' : zero, 'classify' : classify }
    myjson = json.dumps(data)
    response = cl.do_learntestclassify(queue, myjson)
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
