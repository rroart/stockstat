import talib as ta

import myutils as my

def getrsi(myma):
    lses = getmyrsi(myma)
    if lses is None:
        return(None)
        #return pd.Series()
    #print(type(lses), len(lses))
    ls = lses
    keys = ls.keys()
    return ls[keys[len(keys) - 1]]


def getmyrsi(myma):
                                        #    print(myma)
                                        #    print("bla\n")
    myma = my.fixna(myma)
    if len(myma) < 40:
        return(None)
    
    scalebeginning100 = 0
#    if scalebeginning100 == 0:
                                        #        this does not matter?
                                        #        myma = fixpercent(myma)
    
                                        #    print(myma)
    num = 14
    if not myma.isnull().all():
        m = ta.RSI(myma)
    else:
        return None
                                        #    print(m)
                                        #    print(m)
                                        #    print("\ngrr\n")
    l = len(myma)
#    print(myma[l])
    #print(myma)
    #print(m)
    
    return(m)


