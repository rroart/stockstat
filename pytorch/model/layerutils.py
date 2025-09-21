import torch
import numpy as np

def getNormalLayer(shape):
    if isinstance(shape, tuple):
        shapelen = len(shape)
        print("shapelen", shapelen, shape)
        return torch.nn.LayerNorm(shape)
    print("shape not tuple", shape, type(shape))
    return None


def avgstdvar(array):
    r1 = np.average(array)
    #print("\nMean: ", r1)

    r2 = np.sqrt(np.mean((array - np.mean(array)) ** 2))
    #print("\nstd: ", r2)

    r3 = np.mean((array - np.mean(array)) ** 2)
    #print("\nvariance: ", r3)
    return (r1, r2, r3)


def normalize(input, avgstdvar):
    (mean, std, var) = avgstdvar
    x = input / 1  # 255.0
    x = input - mean
    x = x / std
    return x
