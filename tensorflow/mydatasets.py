import tensorflow as tf
import numpy as np
#import pandas as pd 
from keras import backend as K

def getdataset(myobj, config, classifier):
    if myobj.dataset == 'mnist':
        return getmnist(config)
    if myobj.dataset == 'cifar10':
        return getcifar10(config)
    if myobj.dataset == 'dailymintemperatures':
        return getdailymintemperatures(myobj)
    if myobj.dataset == 'nasdaq':
        return getnasdaq(myobj, config)
    if myobj.dataset == 'number':
        return getnumber(myobj, config)

def getmnist(config):
    #load mnist data
    (x_train, y_train), (x_test, y_test) = tf.keras.datasets.mnist.load_data("/tmp/datasets/mnist.npz")
    #print(tf.keras.backend.image_data_format())
    def create_mnist_dataset(data, labels, batch_size):
        def gen():
            for image, label in zip(data, labels):
                yield image, label
        ds = tf.data.Dataset.from_generator(gen, (tf.float32, tf.int32), ((28,28 ), ()))
        return ds.repeat().batch(batch_size)
    #train and validation dataset with different batch size
    train_dataset = create_mnist_dataset(x_train, y_train, 10)
    valid_dataset = create_mnist_dataset(x_test, y_test, 20)
    #print(type(x_train))
    #print(type(y_train))
    #print(type(train_dataset))
    #print(x_train.shape)
    #print(x_train.shape, x_train.shape[2])
    #print(type(y_train.shape))
    mydim = (x_train.shape[1], x_train.shape[2])
    if not config.name == "cnn" and not config.name == "cnn2":
        if config.name == "rnn" or config.name == "lstm" or config.name == "gru":
            #print("here")
            if config.name == "rnnnot":
                #print(x_train.shape)
                x_train = np.transpose(x_train, [1, 0, 2])
                #print(x_train.shape)
                x_test = np.transpose(x_test, [1, 0, 2])
            #x_train = x_train.reshape(1, x_train.shape[0], x_train.shape[1])
            #x_test = x_test.reshape(1, x_test.shape[0], x_test.shape[1])
        else:
            x_train = x_train.reshape((x_train.shape[0], 784))
            x_test = x_test.reshape((x_test.shape[0], 784))
            mydim = 784
        y_train = np.int32(y_train)
        y_test = np.int32(y_test)
    else:
        if config.name == "cnn2":
            mydim = (28, 28, 1)
            x_train = x_train.reshape(x_train.shape[0], 28, 28, 1)
            x_test = x_test.reshape(x_test.shape[0], 28, 28, 1)
    x_test = np.float16(x_test)        
    #print(x_train.shape)
    #print(y_train.shape)
    return x_train, y_train, x_test, y_test, mydim, 10, True

def getcifar10(config):
    #load mnist data
    (x_train, y_train), (x_test, y_test) = tf.keras.datasets.cifar10.load_data()
    #"/tmp/datasets/cifar.npz")
    def create_cifar10_dataset(data, labels, batch_size):
        def gen():
            for image, label in zip(data, labels):
                yield image, label
        ds = tf.data.Dataset.from_generator(gen, (tf.float32, tf.int32), ((32,32 ), ()))
        return ds.repeat().batch(batch_size)
    #train and validation dataset with different batch size
    train_dataset = create_cifar10_dataset(x_train, y_train, 10)
    valid_dataset = create_cifar10_dataset(x_test, y_test, 20)
    #print(type(x_train))
    #print(type(y_train))
    #print(type(train_dataset))
    #print(x_train.shape)
    #print(x_train.shape, x_train.shape[2])
    #print(type(y_train.shape))
    mydim = (x_train.shape[1], x_train.shape[2])
    if not config.name == "cnn" and not config.name == "cnn2":
        if config.name == "rnn" or config.name == "lstm" or config.name == "gru":
            #print("here")
            if config.name == "rnnnot":
                #print(x_train.shape)
                x_train = np.transpose(x_train, [1, 0, 2])
                #print(x_train.shape)
                x_test = np.transpose(x_test, [1, 0, 2])
            #x_train = x_train.reshape(1, x_train.shape[0], x_train.shape[1])
            #x_test = x_test.reshape(1, x_test.shape[0], x_test.shape[1])
        else:
            x_train = x_train.reshape((x_train.shape[0], 1024 * 3))
            x_test = x_test.reshape((x_test.shape[0], 1024 * 3))
            mydim = 1024 * 3
        y_train = np.int32(y_train)
        y_test = np.int32(y_test)
    else:
        if config.name == "cnn2":
            #print("here21")
            #print(x_train.shape)
            mydim = (32, 32, 3)
            x_train = x_train.reshape(x_train.shape[0], 32, 32, 3)
            x_test = np.float16(x_test.reshape(x_test.shape[0], 32, 32, 3))
            #x_train = x_train.reshape(x_train.shape[0], 1, 32, 32)
            #x_test = x_test.reshape(x_test.shape[0], 1, 32, 32)
            #mydim = (1, 32, 32)
    #print(x_train.shape)
    #print(y_train.shape)
    return x_train, y_train, x_test, y_test, mydim, 10, True

def getdailymintemperatures(myobj):
    url = 'https://raw.githubusercontent.com/jbrownlee/Datasets/master/daily-min-temperatures.csv'
    file_path = tf.keras.utils.get_file("/tmp/daily-min-temperatures.csv", url)
    data = np.genfromtxt(file_path, delimiter = ',', skip_header = 1, dtype = {'names': ('date', 'temp'), 'formats': (np.str, np.float)})
    data = data['temp']
    data = [ data ]
    #print(type(data), len(data), data)
    return data, None, data, None, myobj.size, myobj.classes, False
#    return data, None, None, None, myobj.size, myobj.classes
    
def getnasdaq(myobj, config):
    url = 'https://fred.stlouisfed.org/graph/fredgraph.csv?mode=fred&id=NASDAQCOM&cosd=2014-11-15&coed=2019-11-15&vintage_date=2019-11-17&revision_date=2019-11-17&nd=1971-02-05'
    file_path = tf.keras.utils.get_file("/tmp/", url)
    file_path = "/tmp/fredgraph.csv?mode=fred&id=NASDAQCOM&cosd=2014-11-15&coed=2019-11-15&vintage_date=2019-11-17&revision_date=2019-11-17&nd=1971-02-05"
    data = np.genfromtxt(file_path, delimiter = ',', skip_header = 1, dtype = {'names': ('date', 'nasdaqcom'), 'formats': (np.str, np.float)})
    #print(type(data), data.shape, data)
    data = data['nasdaqcom']
    data = data[np.logical_not(np.isnan(data))]
    #data = data[1:20]

    url = 'https://fred.stlouisfed.org/graph/fredgraph.csv?mode=fred&id=DJIA&cosd=2014-11-15&coed=2019-11-15&vintage_date=2019-11-17&revision_date=2019-11-17&nd=1971-02-05'
    file_path2 = tf.keras.utils.get_file("/tmp/", url)
    file_path2 = "/tmp/fredgraph.csv?mode=fred&id=DJIA&cosd=2014-11-15&coed=2019-11-15&vintage_date=2019-11-17&revision_date=2019-11-17&nd=1971-02-05"
    data2 = np.genfromtxt(file_path2, delimiter = ',', skip_header = 1, dtype = {'names': ('date', 'djia'), 'formats': (np.str, np.float)})
    #print(type(data2), data2.shape, data2)
    data2 = data2['djia']
    data2 = data2[np.logical_not(np.isnan(data2))]
    #data = data[1:20]

    #print(type(data), len(data), data)
    #print(type(data2), len(data2), data2)

    data3 = 100 * data / data[0]
    data4 = 100 * data2 / data2[0]
    #print(data3[0:10])
    
    data = [ data ]
    data2 = [ data2 ]

    return data, None, data, None, myobj.size, myobj.classes, False

def getnumber(myobj, config):
    data = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40 ]
    data1 = [ 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140 ]

    data = [ data, data1 ]

    return data, None, data, None, myobj.size, myobj.classes, False
