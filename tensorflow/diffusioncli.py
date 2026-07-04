import os

from datetime import datetime, timedelta
import config
import multiprocessing as mp
from multiprocessing import Process, Queue
import json

from collections import OrderedDict

import config
import classify

diffusion = classify.Classify()
queue = Queue()
cache = {}


def learn(ds = None, path = None, cf = config.TENSORFLOWDIFFUSION, steps = None, take = None, vocab = None, submodel = None):
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
    response = diffusion.do_diffusion(queue, myjson, cachedata)
    cache[cf+myds] = response
    result = queue.get()
    print (result)
    return result

def image(path, path2, data, cf):
    myjson = json.dumps(data)
    filenames = [ path, path2 ]
    diffusion.do_dataset_gen(queue, myjson, filenames)
    response = queue.get()
    print(response)
    myobj = response # .json() #.loads(response.text) #request.get_data(as_text=True)
    print(type(myobj))
    print(myobj)
    for afile in myobj['files']:
        filename = "/tmp/download/" + afile
        print("Filename", filename)
    return response

def dcgan(ds = 'celeba_gan', cf = config.TENSORFLOWDCGAN, take = None):
    neuralnetcommand = { 'mldynamic' : None, 'mlclassify' : None, 'mllearn' : None }
    cfname, modelInt, thecf = config.get(cf)
    thecf['steps'] = 1
    if take is not None:
        thecf['take'] = take
    filename = getfilename(thecf, ds)
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'generate' : True, 'files' : 2, 'filename' : filename, 'neuralnetcommand' : neuralnetcommand, cfname : thecf }
    return image(None, None, data, cf)

def conditionalgan(ds = 'mnist', cf = config.TENSORFLOWCONDITIONALGAN, take = None):
    neuralnetcommand = { 'mldynamic' : None, 'mlclassify' : None, 'mllearn' : None }
    cfname, modelInt, thecf = config.get(cf)
    thecf['steps'] = 1
    if take is not None:
        thecf['take'] = take
    filename = getfilename(thecf, ds)
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'generate' : True, 'files' : 2, 'filename' : filename, 'neuralnetcommand' : neuralnetcommand, cfname : thecf }
    return image(None, None, data, cf)

def neural_style_transfer(path, path2, ds = 'vgg19', cf = config.TENSORFLOWNEURALSTYLETRANSFER):
    neuralnetcommand = { 'mldynamic' : None, 'mlclassify' : None, 'mllearn' : None }
    cfname, modelInt, thecf = config.get(cf)
    thecf['steps'] = 1
    filename = getfilename(thecf, ds)
    # TODO vgg19
    data = { 'modelInt' : modelInt, 'dataset' : 'dcgan', 'generate' : True, 'files' : 2, 'filename' : filename, 'neuralnetcommand' : neuralnetcommand, cfname : thecf }
    return image(path, path2, data, cf)
    
def vae(ds = 'mnist', cf = config.TENSORFLOWVAE, take = None):
    neuralnetcommand = { 'mldynamic' : None, 'mlclassify' : None, 'mllearn' : None }
    cfname, modelInt, thecf = config.get(cf)
    thecf['steps'] = 1
    if take is not None:
        thecf['take'] = take
    filename = getfilename(thecf, ds)
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'generate' : True, 'files' : 2, 'filename' : filename, 'neuralnetcommand' : neuralnetcommand, cfname : thecf }
    return image(None, None, data, cf)

def dataset(ds = 'mnist', cf = config.TENSORFLOWMLP):
    neuralnetcommand = { 'mldynamic' : None, 'mlclassify' : None, 'mllearn' : None }
    cfname, modelInt, thecf = config.get(cf)
    thecf['steps'] = 1
    filename = getfilename(thecf, ds)
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'filename' : filename, 'zero' : True, 'neuralnetcommand' : neuralnetcommand, cfname : thecf }
    data[cfname] = thecf
    myjson = json.dumps(data)
    response = diffusion.do_dataset(queue, myjson)
    result = queue.get()
    print(result)
    return result

def classify(path, ds = 'mnist', cf = config.TENSORFLOWMLP):
    neuralnetcommand = { 'mldynamic' : None, 'mlclassify' : None, 'mllearn' : None }
    cfname, modelInt, thecf = config.get(cf)
    thecf['binary'] = False
    filename = getfilename(thecf, ds)
    data = { 'modelInt' : modelInt, 'dataset' : ds, 'filename' : filename, 'zero' : True, 'neuralnetcommand' : neuralnetcommand, cfname : thecf }
    filenames = [ path ]
    myjson = json.dumps(data)
    diffusion.do_imgclassify(queue, myjson, filenames)
    result = queue.get()
    print(result)
    return result
    
def getfilename(cf, ds):
    return cf['name'] + ds

def getdsname(ds):
    if isinstance(ds, list):
        myds = str(ds[0]) + str(ds[1])
    else:
        myds = ds
    return myds
