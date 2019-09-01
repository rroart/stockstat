import tensorflow as tf
import numpy as np
#import pandas as pd 

def getdataset(myobj):
    if myobj.dataset == 'mnist':
        return getmnist()
    if myobj.dataset == 'dailymintemperatures':
        return getdailymintemperatures(myobj)

def getmnist():
    #load mnist data
    (x_train, y_train), (x_test, y_test) = tf.keras.datasets.mnist.load_data("/tmp/datasets/mnist.npz")
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
    x_train = x_train.reshape((x_train.shape[0], 784))
    x_test = x_test.reshape((x_test.shape[0], 784))
    y_train = np.int32(y_train)
    y_test = np.int32(y_test)
    print(x_train.shape)
    print(y_train.shape)
    return x_train, y_train, x_test, y_test, 784, 10

def getdailymintemperatures(myobj):
    url = 'https://raw.githubusercontent.com/jbrownlee/Datasets/master/daily-min-temperatures.csv'
    file_path = tf.keras.utils.get_file("/tmp/daily-min-temperatures.csv", url)
    data = np.genfromtxt(file_path, delimiter = ',', skip_header = 1, dtype = {'names': ('date', 'temp'), 'formats': (np.str, np.float)})
    data = data['temp']
    return data, None, None, None, myobj.size, myobj.classes
    
