import talib as ta

import myutils as my

def getmylses(myma):
                                        #    print(myma)
                                        #    print("bla\n")
    myma = my.fixna(myma)
    #print(myma)
    #print(len(myma))
    if len(myma) < 40:
        return(None)
    
    scalebeginning100 = 0
#    if scalebeginning100 == 0:
                                        #        this does not matter?
                                        #        myma = fixpercent(myma)
    
                                        #    print(myma)
    maType = 'EMA'
    fast = 12
    slow = 26
    sig = 9
    #m = ta.MACD(myma, nFast=fast, nSlow=slow, nSig=sig, maType = maType, percent = False )
    #print((myma)
    real = ta.CCI(high, low, close, timeperiod=14)
    if not myma.isnull().all():
        m = ta.MACD(myma)
    else:
        return None
        #m = (pd.Series([np.NaN]), pd.Series([np.NaN]), pd.Series([np.NaN]))
    #print(type(m))
    #print(m.values)
    #m = ta.MACD(myma, fast, slow, sig)
    #print(type(m))
    #print(m)
    #if True:
    #   import time
    #   time.sleep(15)                                     

                                        #    print(m)
                                        #    print(m)
                                        #    print("\ngrr\n")
    lses = getmacd(m)
    l = len(myma)
    #print(myma)
    #print(m)
    
    return(lses)


