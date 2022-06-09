import talib as ta
import pandas as pd
import numpy as np
import pdutils as pdu
import myutils as my
import const

#import myutils as my

doprint = False

class RSI:

  def __init__(self, stockdata):
    self.count = 0;
    self.stockdata = stockdata
        
  def title(self):
      return "RSI"

  def names(self):
      return [ "rsi" ]

  def getrsi(self, myma):
    lses = self.calculate(myma)
    if lses is None:
        return(None)
        #return pd.Series()
    #print(type(lses), len(lses))
    ls = lses
    #print(len(ls))
    #print("kkk",ls[0].keys())
    keys = ls[0].keys()
    #return ls[0]
    v = ls[0][keys[len(keys) - 1]]
    #print("v ", v)
    return [ v ]


  def calculate(self, myma):
                                        #    print(myma)
                                        #    print("bla\n")
    #myma = my.fixna(myma)
    l = myma[0]
    #llow = myma[1]
    #lhigh = myma[2]
    if len(l) < 40:
        return(None)
    
    scalebeginning100 = 0
#    if scalebeginning100 == 0:
                                        #        this does not matter?
                                        #        myma = fixpercent(myma)
    
                                        #    print(myma)
    num = 14
    if not l.isnull().all():
        m = ta.RSI(l)
        if doprint:
          print("dop",l.values)
          print(m.values)
    else:
        return None
                                        #    print(m)
                                        #    print(m)
                                        #    print("\ngrr\n")
    l = len(myma)
#    print(myma[l])
    #print(myma)
    #print(m)
    
    return [m]

  def dfextend(self, df, period, periodtext, sort, interpolate = True, rebase = False, deltadays = 3, reverse = False, interpolation = 'linear'):
    dateset = set(self.stockdata.listdates)
    rsilist = []
    headskip = 0
    for mydf in df.itertuples():
        el = next(x for x in self.stockdata.listid if x.id.iloc[0] == mydf.id)
        #print(type(el))
        eldateset = set(el.date.values)
        aset = dateset - eldateset
        emptydf = pd.DataFrame(data = None, columns = [ 'date' ])
        for x in aset:
            emptydf = emptydf.append({ 'date' : x }, ignore_index=True)
        el = el.append(emptydf)
        el = el.sort_values(by='date', ascending = 1)
        #el = listid[mydf.id]
        #el = el[order(el.date),]
        myc = pdu.getonedfvalue(el, period)
        dateslen = len(self.stockdata.listdates)
        myclen = len(myc)
        mycoffset = dateslen - myclen
        mycorig = myc
        #myc = myc.head(n=(myclen-headskiprsi))
        #myc = myc.tail(n=macddays)
        myc = myc.iloc[0 : self.stockdata.dates.startindex + 1 - mycoffset]
        if rebase:
            if periodtext == "Price" or periodtext == "Index":
                first = myc.values[np.isfinite(myc.values)][0]
                myc = myc * (100 / first)
        if periodtext == "Price" or periodtext == "Index":
            myc = my.fixzero2(myc)
        if interpolate:
            myc = my.fixna(myc, interpolation)
            #myc = myc.interpolate(method='linear')
        doprint = mydf.id == '1301162'
        #macd.doprint = doprint
        #rsi.doprint = doprint
        rsis = self.getrsi([myc, None, None])
        #print(type(rsis))
        #print(len(rsis))
        #print(rsis.keys())
        if not rsis is None:
            rsilist.append(rsis[0])
        else:
            rsilist.append(None)
        #if rsi is None:
        #    print("App rsi none")
            
    #headskiprsi = headskiprsi + tablemoveintervaldays
    rsic = rsilist
    df['rsic'] = pd.Series(data = rsic, name = 'rsic', index = df.index)
    if sort == const.RSI:
        if reverse:
            df = df.sort_values(by='rsic', ascending = 0)
        else:
            df = df.sort_values(by='rsic', ascending = 1)
    return df
        
  def titles(self):
    return [ "rsi" ]

  def values(self, df, i):
    return [ df.rsic.iloc[i] ]

  def formats(self):
    return [ "{:.2f}" ]
