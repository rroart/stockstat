import talib as ta
import pandas as pd
import pdutils as pdu
import myutils as my
import const

#import myutils as my

doprint = False

class MA:

  def __init__(self, matype, timeperiod):
    self.count = 0
    self.matype = matype
    self.timeperiod = timeperiod
        
  def title(self):
      adict = {'0':'SMA', '1':'EMA', '2':'WMA', '3':'DEMA', '4':'TEMA', '5':'TRIMA', '6':'KAMA', '7':'MAMA', '8':'T3'}
      return [ adict[str(self.matype)] ]

  def names(self):
      return ["ma"]
  
  def getma(self, m1, m2, m3, m4, m5):
    #print("mmm", type(m))
    #print(len(m))
    #print(type(m[0]))
    l = len(m) / 2
                                        #    print("hei\n")
                                        #    print(l)
                                        #    print("\nhei2\n")
                                        #    m
    c = 1
    #retlist1 = m[0];
    #retlist2 = m[1];
    #retlist3 = m[2];
    #retlist3 = m[2];
    #retlist3 = m[2];
    return([retlist1, retlist2, retlist3])

  def calculate(self, myma):
    l = myma[0]
    if len(l) < 40:
        return(None)
    if not l.isnull().all():
        m = ta.MA(l, matype=self.matype, timeperiod=self.timeperiod) #dup
        #m1 = ta.SMA(l, timeperiod=5)
        #print("m0", m0)
        #print("m1", m1)
        #m2 = ta.SMA(l, timeperiod=8)
        #m3 = ta.SMA(l, timeperiod=13)
        #m4 = ta.SMA(l, timeperiod=30) #dup
        #m5 = ta.SMA(l) #dup
        #if doprint:
        #  print("hh", m[0].values)
    else:
        return None
    lses = [ m ]
    #self.getma(m)
    l = len(myma)
    return(lses)


  def getmomhist(self, myma, deltadays):
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
