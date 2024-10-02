#!/usr/bin/python3

#exec(open("./lib.py").read())

import os

from datetime import datetime, timedelta
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

def dcgan(load = False, save = False):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : load, 'mllearn' : save }
    data = { 'modelInt' : 13, 'dataset' : 'celeba_gan', 'generate' : True, 'filename' : 'dcgan', 'neuralnetcommand' : neuralnetcommand, 'tensorflowDCGANConfig' : { 'lr' : 0.0001, 'steps' : 10 } }
    image(None, None, data)

def conditionalgan(load = False, save = False):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : load, 'mllearn' : save }
    data = { 'modelInt' : 12, 'dataset' : 'mnist', 'generate' : True, 'filename' : 'conditionalgan', 'neuralnetcommand' : neuralnetcommand, 'tensorflowConditionalGANConfig' : { 'lr' : 0.0003, 'steps' : 1 } }
    image(None, None, data)

def neural_style_transfer(path, path2, load = False, save = False):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : load, 'mllearn' : save }
    data = { 'modelInt' : 14, 'generate' : True, 'neuralnetcommand' : neuralnetcommand, 'tensorflowNeuralStyleTransferConfig' : { 'steps' : 1 } }
    image(path, path2, data)
    
