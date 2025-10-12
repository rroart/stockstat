#!/usr/bin/python3

import classify
import json
import sys

from multiprocessing import Process, Queue

import config

gpt = classify.Classify()
queue = Queue()
cache = {}

def learn(ds = None, path = None, cf = config.PYTORCHGPTMIDIRPRCONFIG, steps = None, take = None, vocab = None, submodel = None):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : False, 'mllearn' : True }
    cfname, modelInt, thecf = config.get(cf)
    if steps is not None:
        thecf['steps'] = steps
    if take is not None:
        thecf['take'] = take
    if vocab is not None:
        thecf['vocab'] = vocab
    print("submodel", submodel)
    if submodel is not None:
        thecf['submodel'] = submodel
    myds = getdsname(ds)
    filename = getfilename(thecf, myds)
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'path' : path, 'filename' : filename, 'classifyarray' : None, 'neuralnetcommand' : neuralnetcommand, cfname : thecf }
    cachedata = cache.get(cf+myds)
    cachedata = None
    myjson = json.dumps(data)
    response = gpt.do_gptmidi(queue, myjson, [ None ], cachedata)
    cache[cf+myds] = response
    result = queue.get()
    print (result)
    return result

def generate(text, ds = None, path = None, cf = config.PYTORCHGPTMIDIRPRCONFIG, take = None, size = 40, submodel = None):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : True, 'mllearn' : False }
    cfname, modelInt, thecf = config.get(cf)
    if take is not None:
        thecf['take'] = take
    print("submodel", submodel)
    if submodel is not None:
        thecf['submodel'] = submodel
    myds = getdsname(ds)
    filename = getfilename(thecf, myds)
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'path' : path, 'filename' : filename, 'classifyarray' : [ text ], 'classes' : size, 'neuralnetcommand' : neuralnetcommand, cfname : thecf }
    #if cf == config.PYTORCHGPTMIDIFIGARO:
    #    data['flavour'] = None
    cachedata = cache.get(cf+myds)
    cachedata = None
    myjson = json.dumps(data)
    response = gpt.do_gptmidi(queue, myjson, [ None ], cachedata)
    cache[cf+myds] = response
    result = queue.get()
    print (result)

def getfilename(cf, ds):
    submodel = ''
    if 'submodel' in cf:
        submodel = cf['submodel']
    return cf['name'] + submodel + ds

def getdsname(ds):
    if isinstance(ds, list):
        myds = str(ds[0]) + str(ds[1])
    else:
        myds = ds
    return myds


def chatloop(ds = None, path = None, cf = 'tensorflowMiniatureGPTConfig', take = None, size = 40):
    rdall = ''
    while True:
        rd = input("Input")
        do_rd = rd != ''
        if do_rd:
            rdall = rdall + " " + rd
            generate(rdall, ds, path, cf, take, size)
        else:
            break
