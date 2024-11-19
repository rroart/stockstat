import os

from datetime import datetime, timedelta
import config
import multiprocessing as mp
from multiprocessing import Process, Queue
import json

from collections import OrderedDict

import config
import classify

img = classify.Classify()
queue = Queue()
cache = {}

def image(path, path2, data, cf):
    myjson = json.dumps(data)
    filenames = [ path, path2 ]
    img.do_dataset_gen(queue, myjson, filenames)
    response = queue.get()
    print(response)
    myobj = response # .json() #.loads(response.text) #request.get_data(as_text=True)
    print(type(myobj))
    print(myobj)
    for afile in myobj['files']:
        filename = "/tmp/download/" + afile
        print("Filename", filename)
    return response

def dcgan(ds = 'celeba_gan', cf = config.TENSORFLOWDCGAN, take = None, load = False, save = False):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : load, 'mllearn' : save }
    cfname, modelInt, thecf = config.get(cf)
    thecf['steps'] = 1
    if take is not None:
        thecf['take'] = take
    filename = getfilename(thecf, ds)
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'generate' : True, 'files' : 2, 'filename' : filename, 'neuralnetcommand' : neuralnetcommand, cfname : thecf }
    return image(None, None, data, cf)

def conditionalgan(ds = 'mnist', cf = config.TENSORFLOWCONDITIONALGAN, take = None, load = False, save = False):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : load, 'mllearn' : save }
    cfname, modelInt, thecf = config.get(cf)
    thecf['steps'] = 1
    if take is not None:
        thecf['take'] = take
    filename = getfilename(thecf, ds)
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'generate' : True, 'files' : 2, 'filename' : filename, 'neuralnetcommand' : neuralnetcommand, cfname : thecf }
    return image(None, None, data, cf)

def neural_style_transfer(path, path2, ds = 'vgg19', cf = config.TENSORFLOWNEURALSTYLETRANSFER, load = False, save = False):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : load, 'mllearn' : save }
    cfname, modelInt, thecf = config.get(cf)
    thecf['steps'] = 1
    filename = getfilename(thecf, ds)
    # TODO vgg19
    data = { 'modelInt' : modelInt, 'dataset' : 'dcgan', 'generate' : True, 'files' : 2, 'filename' : filename, 'neuralnetcommand' : neuralnetcommand, cfname : thecf }
    return image(path, path2, data, cf)
    
def dataset(ds = 'mnist', cf = config.TENSORFLOWMLP, load = False, save = True):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : load, 'mllearn' : save }
    cfname, modelInt, thecf = config.get(cf)
    thecf['steps'] = 1
    filename = getfilename(thecf, ds)
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'filename' : filename, 'zero' : True, 'neuralnetcommand' : neuralnetcommand, cfname : thecf }
    data[cfname] = thecf
    img.do_dataset()
    response = request.imgrequest3(cf, data)
    print(response.text)
    print(response)
    myobj = response.json() #.loads(response.text) #request.get_data(as_text=True)

def classify(path, ds = 'mnist', cf = config.TENSORFLOWMLP):
    neuralnetcommand = { 'mldynamic' : False, 'mlclassify' : True, 'mllearn' : False }
    cfname, modelInt, thecf = config.get(cf)
    filename = getfilename(thecf, ds)
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'filename' : filename, 'zero' : True, 'neuralnetcommand' : neuralnetcommand, cfname : thecf }
    img_data = None
    if path is not None:
        with open(path, 'rb') as f:
            img_data = f.read()
    files = {
        'json': (None, json.dumps(data), 'application/json'),
        "file": (path, img_data),
    }
    img.do_imgclassify()
    response = queue.get()
    print(response.text)
    print(response)
    myobj = response.json() #.loads(response.text) #request.get_data(as_text=True)
    
def getfilename(cf, ds):
    return cf['name'] + ds
