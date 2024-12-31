import random

import numpy as np

def gen(start, end):
    l = []
    for i in range(start, end):
        l.append(i)
    return np.asarray(l)

def add(array, value):
    arr = array + value
    return arr

def genrand(start, count, rang):
    l = []
    l.append(start)
    for i in range(count):
        l.append(l[-1] * (1 + rang/2 - rang * random.random()))
    return np.asarray(l)

def genrandadd(arr, mult, rang):
    arr = arr.tolist()
    l = []
    for i in range(len(arr)):
        l.append(arr[i] + mult * rang * random.random())
    return np.asarray(l)
