import unittest
import numpy as np
import keras
import tensorflow as tf

import config
import datacli as cli
import mydatasetscustom

steps = 300

class MyTestCase(unittest.TestCase):
    def test_something(self):
        print("test")
        adapt_data = np.array([[0., 7., 4.],
                       [2., 9., 6.],
                       [0., 7., 4.],
                       [2., 9., 6.]], dtype='float32')
        my_adapt_data = np.array([[0., 1., 2.],
                       [3., 4., 5.],
                       [6., 7., 8.],
                       [9., 10., 11.]], dtype='float32')
        input_data = np.array([[1., 2., 3.]], dtype='float32')
        layer = keras.layers.Normalization(axis=None) #, invert=True)
        layer.adapt(my_adapt_data)
        print(layer(input_data))
        #print("ad", adapt_data)
        #print("in", input_data)
        print("mean variance", layer.adapt_mean, layer.adapt_variance)
        print("mean variance", layer.adapt_mean.value, layer.adapt_variance.value)
        self.assertAlmostEqual(layer.adapt_mean.value.numpy().item(), 5.5, "Mean")
        self.myout(my_adapt_data)


    def test_something2(self):
        print("test2")
        my_adapt_data = np.array([[[0., 1., 2.], [20., 21., 22.]],
                              [[3., 4., 5.], [23., 24., 25.]],
                              [[6., 7., 8.], [26., 27., 28.]],
                              [[9., 10., 11.], [29., 30., 31]]], dtype='float32')
        print("shape", my_adapt_data.shape)
        print("shape", len(my_adapt_data.shape))
        print("mean", np.mean(my_adapt_data, axis=(0,1)))
        print("var", np.var(my_adapt_data, axis=(0,1)))
        print("mean", my_adapt_data.mean(axis=(-1)))
        print("var", my_adapt_data.std(axis=-1))
        input_data = np.array([[[1., 2., 3.], [23, 24, 25]]], dtype='float32')
        print(tf.keras.utils.normalize(input_data, axis=1))
        layer = keras.layers.Normalization(axis=1)  # , invert=True)
        layer.adapt(my_adapt_data)
        print(layer(input_data))
        input_data2 = np.array([[[0., 1., 2.], [20, 21, 22]]], dtype='float32')
        input_data3 = np.array([[[9., 10., 11.], [29, 30, 31]]], dtype='float32')
        print(layer(input_data2))
        print(layer(input_data3))
        # print("ad", adapt_data)
        # print("in", input_data)
        print("mean variance", layer.adapt_mean, layer.adapt_variance)
        print("mean variance", layer.adapt_mean.value, layer.adapt_variance.value)
        self.assertAlmostEqual(layer.adapt_mean.value.numpy()[0].item(), 5.5, "Mean")
        self.assertAlmostEqual(layer.adapt_mean.value.numpy()[1].item(), 25.5, "Mean")
        print(type(layer.adapt_mean.value.numpy()));
        print(type(layer.adapt_mean.value.numpy()[0]));
        print(type(layer.adapt_mean.value.numpy()[0].item()));
        meanarr = layer.adapt_mean.value.numpy()
        vararr = layer.adapt_variance.value.numpy()
        layer = keras.layers.Normalization(axis=1, mean = meanarr, variance = vararr)  # , invert=True)
        print(layer(input_data))
        print(layer(input_data2))
        print(layer(input_data3))
        self.myout(my_adapt_data)

    # 2d axis None 3d axis 1
    def myout(self, my_adapt_data):
        print("out")
        layer = keras.layers.Normalization()  # , invert=True)
        layer.adapt(my_adapt_data)
        print(layer(my_adapt_data))
        print("out")
        layer = keras.layers.Normalization(axis=-1)  # , invert=True)
        layer.adapt(my_adapt_data)
        print(layer(my_adapt_data))
        print("out ok")
        layer = keras.layers.Normalization(axis=0)  # , invert=True)
        layer.adapt(my_adapt_data)
        print(layer(my_adapt_data))
        print("out")
        layer = keras.layers.Normalization(axis=1)  # , invert=True)
        layer.adapt(my_adapt_data)
        print(layer(my_adapt_data))
        print("out")
        layer = keras.layers.Normalization(axis=-2)  # , invert=True)
        layer.adapt(my_adapt_data)
        print(layer(my_adapt_data))
        print("out")
        layer = keras.layers.Normalization(axis=None)  # , invert=True)
        layer.adapt(my_adapt_data)
        print(layer(my_adapt_data))

if __name__ == '__main__':
    unittest.main()
