import talib as ta
import pandas as pd
import numpy as np
import pdutils as pdu
import myutils as my
import const

#import myutils as my

doprint = False

class MACD:

  def __init__(self, stockdata):
    self.count = 0;
    self.stockdata = stockdata
        
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

  def dfextend(self, df, period, periodtext, sort, interpolate = True, rebase = False, deltadays = 3, reverse = False, interpolation = 'linear'):
    dateset = set(self.stockdata.listdates)
    momlist = []
    signlist = []
    histlist = []
    momdlist = []
    histdlist = []
    sigdlist = []
    macd2list = []
    sign2list = []
    hist2list = []
    for mydf in df.itertuples():
        #print(type(listid))
        el = next(x for x in self.stockdata.listid if x.id.iloc[0] == mydf.id)
        #print(type(el))
        eldateset = set(el.date.values)
        aset = dateset - eldateset
        emptydf = pd.DataFrame(data = None, columns = [ 'date' ])
        #print("l", len(emptydf))
        #print("aset", aset)
        for x in aset:
            emptydf = emptydf.append({ 'date' : x }, ignore_index=True)
        #print("empty0", len(el), len(emptydf), len(aset))
        el = el.append(emptydf)
        #print("empty", len(el), len(emptydf), len(aset))
        #print("eld", len(eld), eld.values)
        
        #for x in stockdata.listdates:
        #    #print("lll", len(el.date == x))
        #    if len(el.date == x) == 0:
        #        print("xxx", x)
        #el2 = stockdata.stocks[stockdata.stocks.id == mydf.id]
        #print("x", type(el), len(el))
        #print(type(el2), len(el2))
        el = el.sort_values(by='date', ascending = 1)
        myc = pdu.getonedfvalue(el, period)
        dateslen = len(self.stockdata.listdates)
        myclen = len(myc)
        mycoffset = dateslen - myclen
        mycorig = myc
        #print(type(myc))
        #print(myclen, headskipmacd)
        #print(type(headskipmacd))
        #myc = myc.head(n=(myclen-headskipmacd))
        #myc = myc.tail(n=macddays)
        myc = myc.iloc[0 : self.stockdata.dates.startindex + 1 - mycoffset]
        #print(type(myc))
        if rebase:
            if periodtext == "Price" or periodtext == "Index":

                #print("myc")
                #print(type(myc))
                #print(myc.values[0])
                #first = myc.values[0]
                #print(first)
                #print(type(myc))
                first = myc.values[np.isfinite(myc.values)][0]
                #print(first)
                #print(100/first)
                #print(myc.values)
                myc = myc * (100 / first)
                #print(myc.values)

                #print("myc2")
        #print(mydf.id)
        global doprint
        doprint = mydf.id == '1301162' or mydf.id == '3SUR'
        #doprint = doprint
        #rsi.doprint = doprint
        if doprint:
            print(type(myc))
            print(myc.values)
            print(mycorig.values)
            print(len(myc.values))
            print(len(self.stockdata.listdates))
            print(myclen)
            print("iloc",self.stockdata.dates.startindex,self.stockdata.dates.endindex)
            print("iloc",self.stockdata.listdates[self.stockdata.dates.startindex],self.stockdata.listdates[self.stockdata.dates.endindex])
        #if myclen != len(stockdata.listdates):
        #    print("error", len(stockdata.listdates),myclen)
        #else:
        #    print("ok")
        if periodtext == "Price" or periodtext == "Index":
            myc = my.fixzero2(myc)
        if interpolate:
            myc = my.fixna(myc, interpolation)
            #myc = myc.interpolate(method='linear')
        momhist = self.getmomhist([myc, None, None], deltadays)
        #print(type(momhist))
        #print(len(momhist))
        #print(momhist.keys())

        #print(type(momhist))
        #print(len(momhist))
                            #print(mom)
        if doprint:
            print("monh", momhist)
            l = pdu.listperiod2(mydf, period)
            print(l)
        if not momhist is None:
            l = pdu.listperiod2(mydf, period)
            momlist.append(momhist[0])
            signlist.append(momhist[1])
            histlist.append(momhist[2])
            momdlist.append(momhist[3])
            sigdlist.append(momhist[4])
            histdlist.append(momhist[5])
            macd2list.append(momhist[0]/l)
            sign2list.append(momhist[1]/l)
            hist2list.append(momhist[2]/l)
        else:
            momlist.append(None)
            signlist.append(None)
            histlist.append(None)
            momdlist.append(None)
            sigdlist.append(None)
            histdlist.append(None)
            macd2list.append(None)
            sign2list.append(None)
            hist2list.append(None)
    #headskipmacd = headskipmacd + tablemoveintervaldays
    momc = momlist
    signc = signlist
    histc = histlist
    #momdc = momdlist
    #histdc = histdlist
    #print('momlist')
    #print(momlist)
    #print(type(momlist))
    df['momc'] = pd.Series(data = momlist, name = 'momc', index = df.index)
    df['histc'] = pd.Series(data = histlist, name = 'histc', index = df.index)
    df['signc'] = pd.Series(data = signlist, name = 'signc', index = df.index)
    df['momdc'] = pd.Series(data = momdlist, name = 'momdc', index = df.index)
    df['sigdc'] = pd.Series(data = sigdlist, name = 'sigdc', index = df.index)
    df['histdc'] = pd.Series(data = histdlist, name = 'histdc', index = df.index)
    df['macd2'] = pd.Series(data = macd2list, name = 'macd2', index = df.index)
    df['sign2'] = pd.Series(data = sign2list, name = 'sign2', index = df.index)
    df['hist2'] = pd.Series(data = hist2list, name = 'hist2', index = df.index)
    #print(df.name)
    #print(df.momc)
    if sort == const.HIST:
        if reverse:
            df = df.sort_values(by='histc', ascending = 0)
        else:
            df = df.sort_values(by='histc', ascending = 1)
    if sort == const.MACD:
        if reverse:
            df = df.sort_values(by='momc', ascending = 0)
        else:
            df = df.sort_values(by='momc', ascending = 1)
    if sort == const.SIGN:
        if reverse:
            df = df.sort_values(by='signc', ascending = 0)
        else:
            df = df.sort_values(by='signc', ascending = 1)
    if sort == const.MACD2:
        if reverse:
            df = df.sort_values(by='macd2', ascending = 0)
        else:
            df = df.sort_values(by='macd2', ascending = 1)
    if sort == const.SIGN2:
        if reverse:
            df = df.sort_values(by='sign2', ascending = 0)
        else:
            df = df.sort_values(by='sign2', ascending = 1)
    if sort == const.HIST2:
        if reverse:
            df = df.sort_values(by='hist2', ascending = 0)
        else:
            df = df.sort_values(by='hist2', ascending = 1)
    return df

  def titles(self):
    return [ "macd", "sign", "hist", "macdd", "signd", "histd", "macdb", "signb", "histb" ]

  def values(self, df, i):
    return [ df.momc.iloc[i],
             df.signc.iloc[i],
             df.histc.iloc[i],
             df.momdc.iloc[i],
             df.sigdc.iloc[i],
             df.histdc.iloc[i],
             df.macd2.iloc[i],
             df.sign2.iloc[i],
             df.hist2.iloc[i] ]

  def formats(self):
    return [ "{:.2f}", "{:.2f}", "{:.2f}", "{:.4f}", "{:.4f}", "{:.4f}", "{:.4f}", "{:.4f}", "{:.4f}" ]
