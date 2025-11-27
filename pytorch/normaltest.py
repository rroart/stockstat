import unittest
import numpy as np
import torch
from torch.utils.data import DataLoader, TensorDataset

import config
from model import layerutils
import model.modelutils

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
        print("shape", my_adapt_data.shape)
        ys = np.array([[0], [0], [0], [0]])
        ys = torch.FloatTensor(np.array([0, 0, 0, 0]))
        my_adapt_data_t = torch.FloatTensor(my_adapt_data)
        train_loader = DataLoader(TensorDataset(my_adapt_data_t, ys), batch_size=16, shuffle=True)
        input_data = np.array([[1., 2., 3.]], dtype='float32')
        input_data = torch.FloatTensor(input_data)
        print("norm", torch.nn.functional.normalize(input_data))
        print("norm", torch.nn.functional.normalize(my_adapt_data_t))
        #print("ad", adapt_data)
        #print("in", input_data)

        #print("mean variance", layer.adapt_mean, layer.adapt_variance)
        #print("mean variance", layer.adapt_mean.value, layer.adapt_variance.value)
        #self.assertAlmostEqual(layer.adapt_mean.value.numpy().item(), 5.5, "Mean")
        self.myout(my_adapt_data)
        print("sh", len(my_adapt_data.shape))
        print("sh", my_adapt_data.shape[2:])
        shape = my_adapt_data.shape[1:]
        #shapel[0] = 0
        #shape=tuple(shapel)
        print("shapegrr", my_adapt_data.shape, shape)
        print("end", layerutils.getNormalLayer(shape)(torch.FloatTensor(my_adapt_data)))
        #print("end", layerutils.getNormalLayer(shape)(torch.FloatTensor(my_adapt_data[2:])))
        (r1, r2, r3)  = self.avgstdvar(my_adapt_data)
        m, s = model.modelutils._compute_mean_std(train_loader)
        print("utils", m, s)
        f = self.normalize(my_adapt_data, r1, r2)
        print("f", f)
        f = self.normalize(my_adapt_data[1:], r1, r2)
        print("f", f)
        gr = model.modelutils._apply_normalize(my_adapt_data_t, m, s)
        print("gr", gr)

    def test_something2(self):
        #return
        print("test2")
        my_adapt_data = np.array([[[0., 1., 2.], [20., 21., 22.]],
                              [[3., 4., 5.], [23., 24., 25.]],
                              [[6., 7., 8.], [26., 27., 28.]],
                              [[9., 10., 11.], [29., 30., 31]]], dtype='float32')
        print("shape", my_adapt_data.shape)
        print("mean", np.mean(my_adapt_data, axis=(0,1)))
        print("var", np.var(my_adapt_data, axis=(0,1)))
        print("mean", my_adapt_data.mean(axis=(0,1)))
        print("var", my_adapt_data.std(axis=(0,1)))
        input_data = np.array([[[1., 2., 3.], [23, 24, 25]]], dtype='float32')
        input_data = torch.FloatTensor(input_data)
        print(torch.nn.functional.normalize(input_data))
        self.myout(my_adapt_data)
        print("end", layerutils.getNormalLayer(my_adapt_data.shape)(torch.FloatTensor(my_adapt_data)))
        #print("end", layerutils.getNormalLayer(my_adapt_data.shape)(torch.FloatTensor(my_adapt_data[2:])))
        shape = my_adapt_data.shape[1:]
        #shapel[0] = 0
        #shape=tuple(shapel)
        print("shapegrr", my_adapt_data.shape, shape)
        print("end", layerutils.getNormalLayer(shape)(torch.FloatTensor(my_adapt_data)))
        #print("end", layerutils.getNormalLayer(shape)(torch.FloatTensor(my_adapt_data[2:])))
        (r1, r2, r3)  = self.avgstdvar(my_adapt_data)
        f = self.normalize(my_adapt_data, r1, r2)
        print("f", f)
        f = self.normalize(my_adapt_data[1:], r1, r2)
        print("f", f)
        print("last")
        print("norm", torch.nn.functional.normalize(torch.Tensor(input_data)))
        print("norm", torch.nn.functional.normalize(torch.Tensor(my_adapt_data)))


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
            datum = torch.FloatTensor(datum)
            for i in [None, -1, 0, 1, 2, 3, 4]:
                print("i", i)
                try:
                    print(torch.nn.functional.normalize(datum, dim=i))
                except Exception as e:
                    print("Exception", e)
        print("again")
        for datum in testdata:
            datum = torch.FloatTensor(datum)
            for i in range(-1, 6):
                print("i", i)
                try:
                    print(layerutils.normalize(datum))
                except Exception as e:
                    print("Exception", e)

    # 2d shape[0:] 3d shape[1:]
    def myout(self, my_adapt_data):
        print("out")
        layernorm = torch.nn.LayerNorm(my_adapt_data.shape[0:])
        print(layernorm(torch.FloatTensor(my_adapt_data)))
        print("out ok")
        layernorm = torch.nn.LayerNorm(my_adapt_data.shape[1:])
        print(layernorm(torch.FloatTensor(my_adapt_data)))
        print("out")
        layernorm = torch.nn.LayerNorm(my_adapt_data.shape[-1:])
        print(layernorm(torch.FloatTensor(my_adapt_data)))
        print("out")
        layernorm = torch.nn.LayerNorm(my_adapt_data.shape[-2:])
        print(layernorm(torch.FloatTensor(my_adapt_data)))
        #layer = keras.layers.Normalization(axis=1)  # , invert=True)
        #layer.adapt(my_adapt_data)
        #print(layer(input_data))
        #input_data2 = np.array([[[0., 1., 2.], [20, 21, 22]]], dtype='float32')
        #input_data3 = np.array([[[9., 10., 11.], [29, 30, 31]]], dtype='float32')
        #print(layer(input_data2))
        #print(layer(input_data3))
        # print("ad", adapt_data)
        # print("in", input_data)
        #print("mean variance", layer.adapt_mean, layer.adapt_variance)
        #print("mean variance", layer.adapt_mean.value, layer.adapt_variance.value)
        #self.assertAlmostEqual(layer.adapt_mean.value.numpy()[0].item(), 5.5, "Mean")
        #self.assertAlmostEqual(layer.adapt_mean.value.numpy()[1].item(), 25.5, "Mean")


    def normalize(self, input, mean, std):
        x = input / 1 #255.0
        x = x - mean
        x = x / std
        return x

    def avgstdvar(self, array):
        r1 = np.average(array)
        print("\nMean: ", r1)

        r2 = np.sqrt(np.mean((array - np.mean(array)) ** 2))
        print("\nstd: ", r2)

        r3 = np.mean((array - np.mean(array)) ** 2)
        print("\nvariance: ", r3)
        return (r1, r2, r3)

if __name__ == '__main__':
    unittest.main()
