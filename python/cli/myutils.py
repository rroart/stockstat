import pandas as pd
import numpy as np
import math

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
    lopen = myma[3]
    l = fixna(l, interpolation)
    llow = fixna(llow, interpolation)
    lhigh = fixna(lhigh, interpolation)
    lopen = fixna(lopen, interpolation)
    return [ l, llow, lhigh, lopen ]

def base100(myma, periodtext):
    percentize = True
    l = myma[0]
    llow = myma[1]
    lhigh = myma[2]
    lopen = myma[3]
    #print("per", percentize, periodtext)
    if percentize:
      if periodtext == "Price" or periodtext == "Index":
        #print("ty", type(l),l)
        first = next(item for item in l if not math.isnan(item))
        #print("t1 ", type(myma), type(first), first, math.isnan(first), math.isnan(l[0]))
        l = np.asarray(l) * (100 / first)
        l = pd.Series(data = l)
        print(llow)
        # check for none
        if not None in llow.tolist():
            llow = np.asarray(llow) * (100 / first)
            llow = pd.Series(data = llow)
        if not None in lhigh.tolist():
            lhigh = np.asarray(lhigh) * (100 / first)
            lhigh = pd.Series(data = lhigh)
        if not None in lopen.tolist():
            lopen = np.asarray(lopen) * (100 / first)
            lopen = pd.Series(data = lopen)
        #print("t2 ", type(myma))
    #print("tmyma3 ", type(myma))
    #print(myma)
    return [ l, llow, lhigh, lopen ]
