#!/usr/bin/python3

#exec(open("./lib.py").read())

import os

from datetime import datetime, timedelta
import config
import request
import multiprocessing as mp
import json

from collections import OrderedDict

def image(path, path2, data):
    img_data = None
    if path is not None:
        with open(path, 'rb') as f:
            img_data = f.read()
    img_data2 = None
    if path2 is not None:
        with open(path2, 'rb') as f:
            img_data2 = f.read()
    files = {
        'json': (None, json.dumps(data), 'application/json'),
        "file": (path, img_data),
        "file2": (path2, img_data2)
    }
    response = request.imgrequest1(None, files)
    print(response.text)
    print(response)
    myobj = response.json() #.loads(response.text) #request.get_data(as_text=True)
    print(type(myobj))
    print(myobj)
    for afile in myobj['files']:
        response = request.imgrequest2(None, afile)

        with open(afile, 'wb') as file:
            file.write(response.content)

def dcgan(ds = 'celeba_gan', cf = 'tensorflowDCGANConfig', load = False, save = False):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : load, 'mllearn' : save }
    cfname, modelInt, thecf = config.get(cf)
    thecf['steps'] = 1
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'generate' : True, 'files' : 2, 'filename' : 'dcgan', 'neuralnetcommand' : neuralnetcommand }
    data[cfname] = thecf
    image(None, None, data)

def conditionalgan(ds = 'mnist', cf = 'tensorflowConditionalGANConfig', load = False, save = False):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : load, 'mllearn' : save }
    cfname, modelInt, thecf = config.get(cf)
    thecf['steps'] = 1
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'generate' : True, 'files' : 2, 'filename' : 'conditionalgan', 'neuralnetcommand' : neuralnetcommand }
    data[cfname] = thecf
    image(None, None, data)

def neural_style_transfer(path, path2, ds = 'vgg19', cf = 'tensorflowNeuralStyleTransferConfig', load = False, save = False):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : load, 'mllearn' : save }
    cfname, modelInt, thecf = config.get(cf)
    thecf['steps'] = 1
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'generate' : True, 'files' : 2, 'filename' : 'dcgan', 'neuralnetcommand' : neuralnetcommand }
    data[cfname] = thecf
    image(path, path2, data)
    
def dataset(ds = 'mnist', cf = 'tensorflowMLPConfig', load = False, save = True):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : load, 'mllearn' : save }
    cfname, modelInt, thecf = config.get(cf)
    thecf['steps'] = 1
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'filename' : ds, 'zero' : True, 'neuralnetcommand' : neuralnetcommand }
    data[cfname] = thecf
    response = request.imgrequest3(None, data)
    print(response.text)
    print(response)
    myobj = response.json() #.loads(response.text) #request.get_data(as_text=True)
    
def classify(path, ds = 'mnist', cf = 'tensorflowMLPConfig'):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : True, 'mllearn' : False }
    cfname, modelInt, thecf = config.get(cf)
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'filename' : ds, 'zero' : True, 'neuralnetcommand' : neuralnetcommand }
    data[cfname] = thecf
    img_data = None
    if path is not None:
        with open(path, 'rb') as f:
            img_data = f.read()
    files = {
        'json': (None, json.dumps(data), 'application/json'),
        "file": (path, img_data),
    }
    response = request.imgrequest4(None, files)
    print(response.text)
    print(response)
    myobj = response.json() #.loads(response.text) #request.get_data(as_text=True)
    
