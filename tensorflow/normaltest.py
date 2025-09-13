import unittest
import numpy as np

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
        import keras
        layer = keras.layers.Normalization(axis=None) #, invert=True)
        layer.adapt(my_adapt_data)
        print(layer(input_data))
        #print("ad", adapt_data)
        #print("in", input_data)
        print("mean variance", layer.adapt_mean, layer.adapt_variance)
        print("mean variance", layer.adapt_mean.value, layer.adapt_variance.value)
        self.assertAlmostEqual(layer.adapt_mean.value.numpy().item(), 5.5, "Mean")


    def test_something2(self):
        print("test2")
        my_adapt_data = np.array([[[0., 1., 2.], [20., 21., 22.]],
                              [[3., 4., 5.], [23., 24., 25.]],
                              [[6., 7., 8.], [26., 27., 28.]],
                              [[9., 10., 11.], [29., 30., 31]]], dtype='float32')
        print("shape", my_adapt_data.shape)
        input_data = np.array([[[1., 2., 3.], [23, 24, 25]]], dtype='float32')
        import keras
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


if __name__ == '__main__':
    unittest.main()
