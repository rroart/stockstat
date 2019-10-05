import torch
import torch.nn as nn
import torch.nn.functional as F
from torch.utils.data import DataLoader, Dataset, TensorDataset
from torch.optim import *
import torchvision
import matplotlib.pyplot as plt

import numpy as np

def getdataset(myobj, config, classifier):
    if myobj.dataset == 'mnist':
        return getmnist(config)
    if myobj.dataset == 'dailymintemperatures':
        return getdailymintemperatures(myobj, config, classifier)

def getmnist(config):
    dl = DataLoader(torchvision.datasets.MNIST('/tmp/datasets/mnist', train=True, download=True))

    tensor = dl.dataset.data
    tensor = tensor.to(dtype=torch.float32)
    print("tt", tensor.shape)
    tr = tensor
    #.reshape(tensor.size(0), -1) 
    print("tt", tr.shape)
    tr = tr/128
    targets = dl.dataset.targets
    targets = targets.to(dtype=torch.float)

    x_train = tr[0:50000]
    y_train = targets[0:50000]
    x_valid = tr[50000:60000]
    y_valid = targets[50000:60000]

    y_valid = y_valid.to(dtype=torch.long)
    
    #bs=64

    #train_ds = TensorDataset(x_train, y_train)
    #train_dl = DataLoader(train_ds, batch_size=bs, drop_last=False, shuffle=True)

    #valid_ds = TensorDataset(x_valid, y_valid)
    #valid_dl = DataLoader(valid_ds, batch_size=bs * 2)

    #loaders={}
    #loaders['train'] = train_dl
    #loaders['valid'] = valid_dl
    print("X", x_train.shape)
    mydim = (x_train.shape[1], x_train.shape[2])
    if not config.name == "cnn":
      if config.name == "rnn" or config.name == "lstm" or config.name == "gru":
        #x_train = x_train.view(-1, 1, 784)
        #x_valid = x_valid.view(-1, 1, 784)
        #y_train = y_train.view(1, y_train.size()[0])
        #y_valid = y_valid.view(1, y_valid.size()[0])
        #x_train = x_train.view(-1, 28, 28)
        #x_valid = x_valid.view(-1, 28, 28)
        mydim = 28
        print("here")
      else:
        x_train = x_train.reshape(x_train.size(0), -1)
        x_valid = x_valid.reshape(x_valid.size(0), -1)
        mydim = 784
    else:
      mydim = 28    
    print(type(x_train), x_train.shape)
    print(type(y_train), y_train.shape)
    print("mydim", mydim)
    return x_train, y_train, x_valid, y_valid, mydim, 10, True

def getdailymintemperatures(myobj, config, classifier):
    url = 'https://raw.githubusercontent.com/jbrownlee/Datasets/master/daily-min-temperatures.csv'
    torchvision.datasets.utils.download_url(url, "/tmp/")
    file_path = "/tmp/daily-min-temperatures.csv"
    data = np.genfromtxt(file_path, delimiter = ',', skip_header = 1, dtype = {'names': ('date', 'temp'), 'formats': (np.str, np.float)})
    print(type(data), data.shape, data)
    data = data['temp']
    #data = data[1:20]
    data = [[data]]
    print(type(data), len(data), data)
    (inputs, labels) = classifier.getSlide(data, None, myobj, config)
    #print("iiiiiiiiii")
    #print(inputs)
    #print(labels)
    return inputs, labels, inputs, labels, myobj.size, myobj.classes, False
