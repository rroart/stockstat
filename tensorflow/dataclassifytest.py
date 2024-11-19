import unittest
import tensorflow as tf
import pandas as pd

import config
import datacli as cli

class MyTestCase(unittest.TestCase):
    def test_something(self):
        url_train = 'http://download.tensorflow.org/data/iris_training.csv'
        url_test = 'http://download.tensorflow.org/data/iris_test.csv'
        file_path_train = tf.keras.utils.get_file(origin = url_train, cache_dir = "/tmp")
        file_path_test = tf.keras.utils.get_file(origin = url_test, cache_dir = "/tmp")
        iris_data_train = pd.read_csv(file_path_train, header=0)
        iris_data_test = pd.read_csv(file_path_test, header=0)
        iris_data_train.info()
        train_x = iris_data_train.loc[:, iris_data_test.columns != 'virginica']
        train_y = iris_data_train.loc[:, ['virginica']]
        test_x = iris_data_test.loc[:, iris_data_test.columns != 'virginica']
        test_y = iris_data_test.loc[:, ['virginica']]
        train_x = train_x.values.tolist()
        train_y = train_y.values.tolist()
        test_x =  test_x.values.tolist()
        test_y = test_y.values.tolist()

        testlist = [ config.TENSORFLOWDNN, config.TENSORFLOWLIC, config.TENSORFLOWMLP ]
        size = 4
        for test in testlist:
            print("Doing", test)
            result = cli.learn(cf = test, train_x = train_x, train_y = train_y, test_x = test_x, test_y = test_y, steps = 1, size = size, classes = 3)
            print(result)
            self.assertIsNotNone(result['accuracy'], "Accuracy")  # add assertion
        # here


    def test_something2(self):
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
        train_x = train_x.to_numpy().reshape(120, 2, 2).tolist()  # train_x.values.tolist()
        train_y = train_y.values.tolist()
        test_x = test_x.to_numpy().reshape(30, 2, 2).tolist()  # test_x.values.tolist()
        test_y = test_y.values.tolist()

        testlist = [ config.TENSORFLOWRNN, config.TENSORFLOWCNN, config.TENSORFLOWLSTM, config.TENSORFLOWGRU ]
        size = 4
        for test in testlist:
            print("Doing", test)
            result = cli.learn(cf=test, train_x=train_x, train_y=train_y, test_x=test_x, test_y=test_y, steps=1, size=size,
                               classes=3)
            print(result)
            self.assertIsNotNone(result['accuracy'], "Accuracy")  # add assertion
        # here

# todo cnn2

if __name__ == '__main__':
    unittest.main()
