import unittest
import numpy as np
import keras
import tensorflow as tf

import config
import datacli as cli
import mydatasetscustom
from model import layerutils

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
        layer = layerutils.getNormalLayer(my_adapt_data.shape)
        layer.adapt(my_adapt_data)
        print("end", layer(my_adapt_data))
        print("end", layer(my_adapt_data[1:]))
        print("\nbn", layer(my_adapt_data))


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
        #layer = keras.layers.Normalization(axis=1, mean = meanarr, variance = vararr)  # , invert=True)
        print(layer(input_data))
        print(layer(input_data2))
        print(layer(input_data3))
        self.myout(my_adapt_data)
        layer = layerutils.getNormalLayer(my_adapt_data.shape)
        layer.adapt(my_adapt_data)
        print("end", layer(my_adapt_data))
        print("end", layer(my_adapt_data[1:]))
        layer = keras.layers.BatchNormalization()
        print("\nbn", layer(my_adapt_data))
        layer = layerutils.getNormalLayer((1,2,3))
        print("\nlay", layer)
        layer.adapt(input_data)
        print("\nlast", layer(input_data))
        layer.adapt(my_adapt_data)
        print("\nlast", layer(my_adapt_data))
        layer = keras.layers.LayerNormalization(axis=-1)
        print("\nlast", layer(input_data))
        print("\nlast", layer(my_adapt_data))

        print("\nlast", tf.keras.utils.normalize(input_data, axis=-1))
        print("\nlast", tf.keras.utils.normalize(my_adapt_data, axis=-1))


    def test_something3(self):
        print("test3")
        irislist = [[6.4,2.8,5.6,2.2], [5.0,2.3,3.3,1.0], [4.9,2.5,4.5,1.7], [4.9,3.1,1.5,0.1], [5.7,3.8,1.7,0.3], [4.4,3.2,1.3,0.2], [5.4,3.4,1.5,0.4], [6.9,3.1,5.1,2.3], [6.7,3.1,4.4,1.4], [5.1,3.7,1.5,0.4], [5.2,2.7,3.9,1.4], [6.9,3.1,4.9,1.5], [5.8,4.0,1.2,0.2], [5.4,3.9,1.7,0.4], [7.7,3.8,6.7,2.2], [6.3,3.3,4.7,1.6], [6.8,3.2,5.9,2.3], [7.6,3.0,6.6,2.1], [6.4,3.2,5.3,2.3], [5.7,4.4,1.5,0.4]]
        iris = np.array(irislist)
        timeserieslist = [[ 100.0, 100.1, 100.2, 100.3, 100.4 ], [ 100.1, 100.2, 100.5, 100.4, 100.3 ], [ 100.6, 100.3, 100.4, 100.5, 100.2 ]]
        timeseries = np.array(timeserieslist)
        difftimeserieslist = [[ 20, 22, 21, 24, 23], [ 1.0, 1.5, 1.4, 1.2, 1.3], [0.10, 0.15, 0.14, 0.12, 0.13 ]]
        difftimeseries = np.array(difftimeserieslist)

        testdata = [ iris, timeseries, difftimeseries ]
        for datum in testdata:
            for i in [None, -1, 0, 1, 2, 3, 4]:
                print("i", i)
                try:
                    print(tf.keras.utils.normalize(datum, axis=i))
                    #print(tf.keras.utils.normalize(datum, axis=i, order=1))
                except Exception:
                    pass
        print("again")
        for datum in testdata:
            for i in [None, -1, 0, 1, 2, 3, 4]:
                print("i", i)
                try:
                    #layer = keras.layers.LayerNormalization(axis=i)
                    #print(layer(datum))
                    layer = keras.layers.Normalization(axis=i)
                    layer.adapt(datum)
                    print(layer(datum))
                except Exception as e:
                    print("Exception", e)

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
