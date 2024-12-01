import tensorflow as tf
import pandas as pd

def iriscommon():
    url_train = 'http://download.tensorflow.org/data/iris_training.csv'
    url_test = 'http://download.tensorflow.org/data/iris_test.csv'
    file_path_train = tf.keras.utils.get_file(origin=url_train, cache_dir="/tmp")
    file_path_test = tf.keras.utils.get_file(origin=url_test, cache_dir="/tmp")
    iris_data_train = pd.read_csv(file_path_train, header=0)
    iris_data_test = pd.read_csv(file_path_test, header=0)
    iris_data_train.info()
    train_x = iris_data_train.loc[:, iris_data_test.columns != 'virginica']
    train_y = iris_data_train.loc[:, ['virginica']]
    test_x = iris_data_test.loc[:, iris_data_test.columns != 'virginica']
    test_y = iris_data_test.loc[:, ['virginica']]
    train_x = train_x.values.tolist()
    train_y = train_y.values.tolist()
    test_x = test_x.values.tolist()
    test_y = test_y.values.tolist()
    return train_x, train_y, test_x, test_y
