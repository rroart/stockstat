import pdutils as pdu
import myutils as my
import numpy as np
import pandas as pd
import const

class DAY:
  def __init__(self, stockdata, days):
      self.count = 0;
      self.stockdata = stockdata
      self.days = days
        
  def title(self):
      return "DAY";

  def names(self):
      return "day";

  def dfextend(self, df, period, periodtext, sort, interpolate = True, rebase = False, deltadays = 3, reverse = False, interpolation = 'linear'):
    #df2 = stockdata.stocklistperiod[period][j + days]
    daylist = []
    for mydf in df.itertuples():
        el = next(x for x in self.stockdata.listid if x.id.iloc[0] == mydf.id)
        #df3 = df2[mydf.id == df2.id]
        #print("df3", len(df), df3)
        el = el.sort_values(by='date', ascending = 1)
        #el = listid[mydf.id]                                       
        #el = el[order(el.date),]                                   
        myc = pdu.getonedfvalue(el, period)
        dateslen = len(self.stockdata.listdates)
        myclen = len(myc)
        mycoffset = dateslen - myclen
        #mycoffset = 0
        if mycoffset < 0:
            print("neg", el.id, el.name)
        #print("lens", len(myc), len(self.stockdata.listdates), self.stockdata.dates.startindex, mycoffset, mydf.id, mydf.name)
        #mycorig = myc
        myc = myc.iloc[0 : self.stockdata.dates.startindex + 1 - mycoffset]
        if periodtext == "Price" or periodtext == "Index":
            myc = my.fixzero2(myc)
        if interpolate:
            myc = my.fixna(myc, interpolation)
        #print("myc", myc.values)
        alen = len(myc)
        #print("alen",alen)
        if alen - 1 - self.days >= 0:
            keys = myc.keys()
            valnow = myc[keys[alen - 1]]
            valwas = myc[keys[alen - 1 - self.days]]
        else:
            valnow = np.nan
            valwas = np.nan
        if mydf.id == '1151606' or mydf.id == '1155463':
            print("hiddn", valnow, valwas, valnow / valwas, myc.values)
        if not np.isnan(valnow) and not np.isnan(valwas):
            daylist.append(valnow / valwas)
        else:
            daylist.append(None)
    df['days'] = pd.Series(data = daylist, name = 'days', index = df.index)
    if reverse:
        df = df.sort_values(by='days', ascending = 0)
    else:
        df = df.sort_values(by='days', ascending = 1)
    return df

  def titles(self):
    return [ "day" ]

  def values(self, df, i):
    #import sys
    #print(df.columns, file=sys.stderr)
    return [ df.days.iloc[i] ]

  def formats(self):
      return [ "{:.2f}" ]
