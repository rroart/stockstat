import talib as ta

#import myutils as my

doprint = False

class MACD:

  def title(self):
      return "MACD"

  def names(self):
      return ["macd", "signal", "diff"]
  
  def getmacd(self, m):
    #print("mmm", type(m))
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

  def calculate(self, myma):
                                        #    print(myma)
                                        #    print("bla\n")
    #myma = my.fixna(myma)
    #print("bbbb")
    #print(myma)
    #print(len(myma))
    l = myma[0]
    #llow = myma[1]
    #lhigh = myma[2]
    #print("len",len(l))
    if len(l) < 40:
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
    if doprint:
      print("l1",l.values)
    if not l.isnull().all():
        m = ta.MACD(l)
        if doprint:
          print("hh", m[0].values)
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
    lses = self.getmacd(m)
    l = len(myma)
    #print(myma)
    #print(m)
    
    return(lses)


  def getmomhist(self, myma, deltadays):
    #print(type(myma))
    #print(myma.values)
    lses = self.calculate(myma)
    if lses is None:
        print("null")
        return(None)
    
    retl = [0, 0, 0, 0, 0, 0]
    ls1 = lses[0]
    ls2 = lses[1]
    ls3 = lses[2]
    #print(type(ls1))
    doprint = None
    if doprint:
        print(ls1.values)
        print(ls2.values)
        print(ls3.values)
    #print(type(ls1))
    #rint(ls1.keys())
    keys1 = ls1.keys()
    keys2 = ls2.keys()
    keys3 = ls3.keys()
    last1 = len(ls1) - 1
    last2 = len(ls2) - 1
    last3 = len(ls3) - 1
    r = keys1[last1]
    retl[0] = ls1[keys1[last1]]
    retl[1] = ls2[keys2[last2]]
    retl[2] = ls3[keys3[last3]]
    delta = deltadays - 1
    prevs1 = last1 - delta
    prevs2 = last2 - delta
    prevs3 = last3 - delta
    retl[3] = (ls1[keys1[last1]] - ls1[keys1[prevs1]])/delta
    retl[4] = (ls2[keys2[last2]] - ls2[keys2[prevs2]])/delta
    retl[5] = (ls3[keys3[last3]] - ls3[keys3[prevs3]])/delta
    #print(mydf.id)
     #           if mydf.id == 'VXAZN':
    #print('vxazn')
    #print(histc.values)
    #print(retl)
    #print(retl[0])
    return(retl)

