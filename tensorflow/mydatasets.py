import tensorflow as tf
import numpy as np
#import pandas as pd 
from keras import backend as K

def getdataset(myobj, config):
    if myobj.dataset == 'mnist':
        return getmnist(config)
    if myobj.dataset == 'cifar10':
        return getcifar10(config)
    if myobj.dataset == 'dailymintemperatures':
        return getdailymintemperatures(myobj)

def getmnist(config):
    #load mnist data
    (x_train, y_train), (x_test, y_test) = tf.keras.datasets.mnist.load_data("/tmp/datasets/mnist.npz")
    print(tf.keras.backend.image_data_format())
    def create_mnist_dataset(data, labels, batch_size):
        def gen():
            for image, label in zip(data, labels):
                yield image, label
        ds = tf.data.Dataset.from_generator(gen, (tf.float32, tf.int32), ((28,28 ), ()))
        return ds.repeat().batch(batch_size)
    #train and validation dataset with different batch size
    train_dataset = create_mnist_dataset(x_train, y_train, 10)
    valid_dataset = create_mnist_dataset(x_test, y_test, 20)
    print(type(x_train))
    print(type(y_train))
    print(type(train_dataset))
    print(x_train.shape)
    print(x_train.shape, x_train.shape[2])
    print(type(y_train.shape))
    mydim = (x_train.shape[1], x_train.shape[2])
    if not config.name == "cnn" and not config.name == "cnn2":
        if config.name == "rnn" or config.name == "lstm" or config.name == "gru":
            print("here")
            if config.name == "rnnnot":
                print(x_train.shape)
                x_train = np.transpose(x_train, [1, 0, 2])
                print(x_train.shape)
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
            x_test = np.float16(x_test.reshape(x_test.shape[0], 28, 28, 1))
    print(x_train.shape)
    print(y_train.shape)
    return x_train, y_train, x_test, y_test, mydim, 10

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
    print(type(x_train))
    print(type(y_train))
    print(type(train_dataset))
    print(x_train.shape)
    print(x_train.shape, x_train.shape[2])
    print(type(y_train.shape))
    mydim = (x_train.shape[1], x_train.shape[2])
    if not config.name == "cnn" and not config.name == "cnn2":
        if config.name == "rnn" or config.name == "lstm" or config.name == "gru":
            print("here")
            if config.name == "rnnnot":
                print(x_train.shape)
                x_train = np.transpose(x_train, [1, 0, 2])
                print(x_train.shape)
                x_test = np.transpose(x_test, [1, 0, 2])
            #x_train = x_train.reshape(1, x_train.shape[0], x_train.shape[1])
            #x_test = x_test.reshape(1, x_test.shape[0], x_test.shape[1])
        else:
            x_train = x_train.reshape((x_train.shape[0], 1024))
            x_test = x_test.reshape((x_test.shape[0], 1024))
            mydim = 1024
        y_train = np.int32(y_train)
        y_test = np.int32(y_test)
    else:
        if config.name == "cnn2":
            print("here21")
            print(x_train.shape)
            mydim = (32, 32, 3)
            x_train = x_train.reshape(x_train.shape[0], 32, 32, 3)
            x_test = np.float16(x_test.reshape(x_test.shape[0], 32, 32, 3))
            #x_train = x_train.reshape(x_train.shape[0], 1, 32, 32)
            #x_test = x_test.reshape(x_test.shape[0], 1, 32, 32)
            #mydim = (1, 32, 32)
    print(x_train.shape)
    print(y_train.shape)
    return x_train, y_train, x_test, y_test, mydim, 10

def getdailymintemperatures(myobj):
    url = 'https://raw.githubusercontent.com/jbrownlee/Datasets/master/daily-min-temperatures.csv'
    file_path = tf.keras.utils.get_file("/tmp/daily-min-temperatures.csv", url)
    data = np.genfromtxt(file_path, delimiter = ',', skip_header = 1, dtype = {'names': ('date', 'temp'), 'formats': (np.str, np.float)})
    data = data['temp']
    return data, None, None, None, myobj.size, myobj.classes
    
