#!/usr/bin/python3

#exec(open("./lib.py").read())

import os

from datetime import datetime, timedelta
import config
import request
import multiprocessing as mp
import json

from collections import OrderedDict

def midi(path, path2, data, cf):
    midi_data = None
    if path is not None:
        with open(path, 'rb') as f:
            midi_data = f.read()
    files = {
        'json': (None, json.dumps(data), 'application/json'),
        "file": (path, midi_data)
    }
    response = request.gptmidirequest(cf, files)
    print(response.text)
    print(response)
    myobj = response.json() #.loads(response.text) #request.get_data(as_text=True)
    print(type(myobj))
    print(myobj)
    if myobj['files'] is None:
        return
    for afile in myobj['files']:
        response = request.download(cf, afile)

        with open(afile, 'wb') as file:
            file.write(response.content)

def learn(ds = 'maestro', path = None, cf = 'pytorchGPTMIDIConfig', steps = None, take = None, vocab = None):
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
    midi(None, None, data, cf)

def generate(path2 = None, ds = 'maestro', path = None, cf = 'pytorchGPTMIDIConfig', take = None, size = 40):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : True, 'mllearn' : False }
    cfname, modelInt, thecf = config.get(cf)
    myds = getdsname(ds)
    filename = getfilename(thecf, myds)
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'path' : path, 'filename' : filename, 'classes' : size, 'neuralnetcommand' : neuralnetcommand, cfname : thecf }
    if take is not None:
        thecf['take'] = take
    midi(path2, None, data, cf)

def getdsname(ds):
    if isinstance(ds, list):
        myds = str(ds[0]) + str(ds[1])
    else:
        myds = ds
    return myds

def getfilename(cf, ds):
    return cf['name'] + ds

