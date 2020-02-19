import talib as ta

import myutils as my

def getmacd(m):
    #print(type(m))
    #print(len(m))
    #print(type(m[0]))
    l = len(m) / 2
                                        #    print("hei\n")
                                        #    print(l)
                                        #    print("\nhei2\n")
                                        #    m
    c = 1
    retlist1 = m[0];
    retlist2 = m[1];
    retlist3 = m[2];
    #for i in range(0, l):
    #    elem = m[i,]
    #    first = elem[1]
    #    second = elem[2]
    #    if isna(first) and isna(second):
    #        retlist1[c] = first
    #        retlist2[c] = second
    #        retlist3[c] = first - second
    #        c = c + 1
    #print(unlist(retlist1))
    #print("")
    #print(unlist(retlist2))
    #print("")
    #print(unlist(retlist3))
    return([retlist1, retlist2, retlist3])

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


def getmomhist(myma, deltadays):
    #print(type(myma))
    #print(myma.values)
    lses = macd.getmylses(myma)
    if lses is None:
        print("null")
        return(None)
    
    retl = [0, 0, 0, 0]
    ls1 = lses[0]
    ls2 = lses[1]
    ls3 = lses[2]
    #print(type(ls1))
    if doprint:
        print(ls1.values)
        print(ls2.values)
        print(ls3.values)
    #print(type(ls1))
    #rint(ls1.keys())
    keys1 = ls1.keys()
    keys3 = ls3.keys()
    last1 = len(ls1) - 1
    last3 = len(ls3) - 1
    r = keys1[last1]
    retl[0] = ls1[keys1[last1]]
    retl[1] = ls3[keys3[last3]]
    delta = deltadays - 1
    prevs1 = last1 - delta
    prevs3 = last3 - delta
    retl[2] = (ls1[keys1[last1]] - ls1[keys1[prevs1]])/delta
    retl[3] = (ls3[keys3[last3]] - ls3[keys3[prevs3]])/delta
    #print(mydf.id)
     #           if mydf.id == 'VXAZN':
    #print('vxazn')
    #print(histc.values)
    #print(retl)
    return(retl)

