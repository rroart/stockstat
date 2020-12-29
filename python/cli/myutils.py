import pandas as pd
import numpy as np

import myconfig as cf

def fixzero(v):
    print(type(v))
    return [ None if x == 0 else x for x in v ]

def fixzero2(v):
    return v.replace(0, np.nan)

def fixna(v, interpolation):
    #print(type(v))
    if cf.nafix == 1:
        return(v.dropna())
    if interpolation == 'linear':
        return (v.interpolate(method='linear'))
    else:
        return v.ffill()

def fixnaarr(myma, interpolation):
    l = myma[0]
    llow = myma[1]
    lhigh = myma[2]
    l = fixna(l, interpolation)
    llow = fixna(llow, interpolation)
    lhigh = fixna(lhigh, interpolation)
    return [ l, llow, lhigh ]

def base100(myma, periodtext):
    percentize = True
    l = myma[0]
    llow = myma[1]
    lhigh = myma[2]
    if percentize:
      if periodtext == "Price" or periodtext == "Index":
        first = l[0]
        print("t1 ", type(myma))
        l = np.asarray(l) * (100 / first)
        l = pd.Series(data = l)
        llow = np.asarray(llow) * (100 / first)
        llow = pd.Series(data = llow)
        lhigh = np.asarray(lhigh) * (100 / first)
        lhigh = pd.Series(data = lhigh)
        #print("t2 ", type(myma))
    #print("tmyma3 ", type(myma))
    #print(myma)
    return [ l, llow, lhigh ]
