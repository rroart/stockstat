import tensorflow as tf
import numpy as np

def iriscommon(binary = False):
    url_train = 'http://download.tensorflow.org/data/iris_training.csv'
    url_test = 'http://download.tensorflow.org/data/iris_test.csv'
    file_path_train = tf.keras.utils.get_file(origin=url_train, cache_dir="/tmp")
    file_path_test = tf.keras.utils.get_file(origin=url_test, cache_dir="/tmp")
    iris_data_train = np.genfromtxt(file_path_train, delimiter=',', skip_header=1)
    iris_data_test = np.genfromtxt(file_path_test, delimiter=',', skip_header=1)
    print(iris_data_test)
    classes = 3
    if binary:
        rows = np.where( iris_data_train[:,4] < 2 )
        iris_data_train = iris_data_train[rows]
        rows = np.where( iris_data_test[:,4] < 2 )
        iris_data_test = iris_data_test[rows]
        classes = 2

    print(iris_data_test)
    train_x = iris_data_train[:,:-1].tolist()
    train_y = iris_data_train[:,-1].tolist()
    test_x = iris_data_test[:,:-1].tolist()
    test_y = iris_data_test[:,-1].tolist()
    print(test_x, test_y)
    return train_x, train_y, test_x, test_y, classes

def mnistcommon(binary = False, outcomes = 2, take = 100):
    (x_train, y_train), (x_test, y_test) = tf.keras.datasets.mnist.load_data()
    x_train = x_train[:take]
    y_train = y_train[:take]
    x_test = x_test[:take]
    y_test = y_test[:take]
    print(type(x_train), x_train.shape)
    print(type(y_train), y_train.shape)
    classes = 10
    if binary:
        rows = np.where( y_train < outcomes )
        x_train = x_train[rows]
        y_train = y_train[rows]
        rows = np.where( y_test < outcomes )
        x_test = x_test[rows]
        y_test = y_test[rows]
        classes = 2
    x_train = x_train.reshape(x_train.shape[0], 784).tolist()
    x_test = x_test.reshape(x_test.shape[0], 784).tolist()
    y_train = y_train.tolist()
    y_test = y_test.tolist()
    return x_train, y_train, x_test, y_test, classes

