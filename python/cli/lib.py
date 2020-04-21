#!/usr/bin/python3

#exec(open("./lib.py").read())

import pandas as pd
#import tensorflow as tf
import numpy as np
import psycopg2
import matplotlib.pyplot as plt

import myutils as my

from datetime import datetime, timedelta

import atr
import cci
import macd
import rsi
import stoch
import stochrsi

#from sqlalchemy import create_engine

doprint = False

pricetype = -1
indextype = -2
metaperiods = 9
periods = 11
topbottom = 15

filterweekend = True

VALUE = 1
MACD = 2
RSI = 3

def getmetas(conn):
    return pd.read_sql_query('select * from meta', con=conn)

def getstocks(conn):
    return pd.read_sql_query('select * from stock', con=conn)

def getmarketmeta(metas, market):
    return metas.loc[(metas.marketid == market)]

def getstockmarket(stocks, market):
    return stocks.loc[(stocks.marketid == market)]

def getperiodtexts(market):
    periodtext = [ "Period1", "Period2", "Period3", "Period4", "Period5", "Period6", "Period7", "Period8", "Period9" ]
    mymeta = getmarketmeta(allmetas, market)
    print(len(mymeta))
    print(mymeta)
    if len(mymeta) > 0:
        for i in range(0, metaperiods):
            #print(type(getperiodtext(mymeta, i)))
            #print(getperiodtext(mymeta, i))
            if getperiodtext(mymeta, i) is not None:
                periodtext[i] = getperiodtext(mymeta, i)
    periodtext.append("Price")
    periodtext.append("Index")
    return periodtext

def getperiodtext(meta, period):
    #print(type(meta))
    #print(type(meta.period1))
    #print(len(meta))
    #print(meta.period1)
    #m=meta.period1
    #print(m.index)
    #print(m[0])
    if period == 0:
        return meta.period1.iloc[0]
    if period == 1:
        return meta.period2.iloc[0]
    if period == 2:
        return meta.period3.iloc[0]
    if period == 3:
        return meta.period4.iloc[0]
    if period == 4:
        return meta.period5.iloc[0]
    if period == 5:
        return meta.period6.iloc[0]
    if period == 6:
        return meta.period7.iloc[0]
    if period == 7:
        return meta.period8.iloc[0]
    if period == 8:
        return meta.period9.iloc[0]
    return None

def split(df, group):
    gb = df.groupby(group)
    return [gb.get_group(x) for x in gb.groups]

class MyDates:
    def __init__(self, start, end, startindex, endindex):
        self.start = start
        self.end = end
        self.startindex = startindex
        self.endindex = endindex
        
    def getdates(listdates, start, end):
        print(len(listdates), listdates[0])
        print("start ", start, " end ", end)
        startoffset = None
        endoffset = None
        startdateindex = None
        enddateindex = None
        if start is None:
            startdateindex = 0
        if isinstance(start, float):
            startoffset = round(start)
            start = None
        if isinstance(start, int):
            startoffset = start
            start = None
        if isinstance(start, str):
            pdstart = np.datetime64(start)
            startdateindex = np.where(listdates == pdstart)
            startdateindex = startdateindex[0][0]
        if end is None:
            enddateindex = len(listdates) - 1
        if isinstance(end, float):
            endoffset = round(end)
            end = None
        if isinstance(end, int):
            endoffset = end
            end = None
        if isinstance(end, str):
            pdend = np.datetime64(end)
            enddateindex = np.where(listdates == pdend)
            enddateindex = enddateindex[0][0]
        print(start)
        print(end)
        print(startoffset)
        print(endoffset)
        print(startdateindex)
        print(enddateindex)
        if startdateindex is None:
            #if enddateindex is None:
            #    enddateindex = len(listdates) - 1
            if startoffset is not None:    
                startdateindex = enddateindex - startoffset
        if enddateindex is None:
            #if startdateindex is None:
            #    startdateindex = 0
            if endoffset is not None:    
                enddateindex = startdateindex + endoffset
        # -1 ok?
                                        #index = len(listdate)
        print(startdateindex)
        print(enddateindex)
        return MyDates(start, end, startdateindex, enddateindex)
        
def getdatedstocklists(listdate, listdates, dates, numberdays, tableintervaldays):
    datedstocklists = []
    #print(len(listdate))
    #print(len(datedstocklists))
    #print(type(listdate[index]))
    #print("days0 ", days)
    index = dates.endindex
    datedstocklists.append(listdate[index])
    print("Index %d" %(dates.startindex), dates.endindex, index, len(listdate))
    if numberdays is None:
        for j in range(dates.startindex, dates.endindex):
            index = index - tableintervaldays
            print(index)
            datedstocklists.append(listdate[index])
    else:
        for j in range(numberdays):
            index = index - tableintervaldays
            print(index)
            datedstocklists.append(listdate[index])
    print(len(datedstocklists))
    return datedstocklists

def getdforderperiod(df, period):
    ds = df
    if period == 0:
        ds = df.sort_values(by='period1', ascending = 0)
    if period == 1:
        ds = df.sort_values(by='period2', ascending = 0)
    if period == 2:
        ds = df.sort_values(by='period3', ascending = 0)
    if period == 3:
        ds = df.sort_values(by='period4', ascending = 0)
    if period == 4:
        ds = df.sort_values(by='period5', ascending = 0)
    if period == 5:
        ds = df.sort_values(by='period6', ascending = 0)
    if period == 6:
        ds = df.sort_values(by='period7', ascending = 0)
    if period == 7:
        ds = df.sort_values(by='period8', ascending = 0)
    if period == 8:
        ds = df.sort_values(by='period9', ascending = 0)
    if period == 9:
        ds = df.sort_values(by='price', ascending = 0)
    if period == 10:
        ds = df.sort_values(by='indexvalue', ascending = 0)
    return ds

def getdforderperiodreverse(df, period):
    ds = df
    if period == 0:
        ds = df.sort_values(by='period1', ascending = 0)
    if period == 1:
        ds = df.sort_values(by='period2', ascending = 0)
    if period == 2:
        ds = df.sort_values(by='period3', ascending = 0)
    if period == 3:
        ds = df.sort_values(by='period4', ascending = 0)
    if period == 4:
        ds = df.sort_values(by='period5', ascending = 0)
    if period == 5:
        ds = df.sort_values(by='period6', ascending = 0)
    if period == 6:
        ds = df.sort_values(by='period7', ascending = 0)
    if period == 7:
        ds = df.sort_values(by='period8', ascending = 0)
    if period == 8:
        ds = df.sort_values(by='period9', ascending = 0)
    if period == 9:
        ds = df.sort_values(by='price', ascending = 0)
    if period == 10:
        ds = df.sort_values(by='indexvalue', ascending = 0)
    return ds

def getonedfperiod(df, period):
    if period == 0:
        return df.period1
    if period == 1:
        return df.period2
    if period == 2:
        return df.period3
    if period == 3:
        return df.period4
    if period == 4:
        return df.period5
    if period == 5:
        return df.period6
    if period == 6:
        return df.period7
    if period == 7:
        return df.period8
    if period == 8:
        return df.period9
    if period == 9:
        return df.price
    if period == 10:
        return df.indexvalue
    #print("should not be here")
    return None

def getonedfperiodarr(df, period):
    if period == 0:
        return [ df.period1 ]
    if period == 1:
        return [ df.period2 ]
    if period == 2:
        return [ df.period3 ]
    if period == 3:
        return [ df.period4 ]
    if period == 4:
        return [ df.period5 ]
    if period == 5:
        return [ df.period6 ]
    if period == 6:
        return [ df.period7 ]
    if period == 7:
        return [ df.period8 ]
    if period == 8:
        return [ df.period9 ]
    if period == 9:
        return [ df.price, df.pricelow, df.pricehigh ]
    if period == 10:
        return [ df.indexvalue, df.indexvaluelow, df.indexvaluehigh ]
    #print("should not be here")
    return None

def getlistmove(datedstocklists, listid, listdate, count, tableintervaldays, stocklistperiod):
    #periodmaps = [] #matrix([], nrow = periods, ncol = (count - 1))
    periodmaps = [[None for x in range(count)] for y in range(periods)]
    print("ddd")
    print(count)
    print(periods)
    for j in range(0, count):
        for i in range(0, periods):
            hasperiod = stocklistperiod[i][j] is not None
            #print("hasperiod")
            #print(type(hasperiod))
            #print(hasperiod)
            if hasperiod:
                print("j")
                print(j)
                if j > 0:
                    df1 = stocklistperiod[i][j - 1]
                    df2 = stocklistperiod[i][j]
                    tmplist = getperiodmap(df1, df2)
                    periodmaps[i][j - 1] = tmplist
                
             #else:
                                        #print("no period day ", j, " period ", i)
            
        

    print("periodmaps")
    print(periodmaps)
    return(periodmaps)


def getlistsorted(datedstocklists, listid, listdate, count, tableintervaldays, wantrise = True, reverse = False):
    stocklistperiod = [[0 for x in range(count)] for y in range(periods)]
                  #matrix([], nrow = periods, ncol = count)
    #print(stocklistperiod)
    #print("count %d %d" % (count, periods))
    for j in range(0, count):
        for i in range(0, periods):
            df = datedstocklists[j] # dataframe make?
            hasperiod = False
            #is.infinit
            #print(type(df))
            #print(len(df))
            #print(i)
            d = getonedfperiod(df, i)
            #print(d)
            #print(type(getonedfperiod(df, i)))
            #print(getonedfperiod(df, i))
            hasperiod = max(getonedfperiod(df, i))
            if hasperiod:
                ds = None
                if reverse:
                    ds = getdforderperiodreverse(df, i)
                else:
                    ds = getdforderperiod(df, i)
                tmp = ds
                #print("tmp")
                #print(type(tmp))
                #print(stocklistperiod.shape)
                #print("ij")
                #print(i)
                #print(j)
                stocklistperiod[i][j] = tmp
            else:
                print("no")
                                        #print("no period day ", j, " period ", i)
    return stocklistperiod

def getperiodmap(list1, list2):
    list = {}
    #print("ty")
    #print(type(list1))
    df1 = list1
    df2 = list2
    #print("ty")
    #print(type(df1))
    list1 = df1.id
    list2 = df2.id
    #print(type(list2))
    #print(len(list2))
    #print(type(list2.keys))
    #print(type(list2.keys()))
    #print(list2.keys())
    j = 0
    values = list1.values
    #print(type(values))
    #print(values)
    for key in list2.keys():
        #print(key)
        id = list2[key]
        list[id] = None #np.NaN
        i = np.where(values == id)
        #print(type(i))
        #print(i)
        i = i[0]
        #print(type(i))
        #print(i)
        if not i is None:
            list[id] = j - i
        j = j + 1
    
    return (list)


def listperiod(list, period, index):
    #print(type(list))
    #print(len(list))
    if period == 0:
        return list.period1.iloc[index]
    if period == 1:
        return list.period2.iloc[index]
    if period == 2:
        return list.period3.iloc[index]
    if period == 3:
        return list.period4.iloc[index]
    if period == 4:
        return list.period5.iloc[index]
    if period == 5:
        return list.period6.iloc[index]
    if period == 6:
        return list.period7.iloc[index]
    if period == 7:
        return list.period8.iloc[index]
    if period == 8:
        return list.period9.iloc[index]
    if period == 9:
        return list.price.iloc[index]
    if period == 10:
        return list.indexvalue.iloc[index]
    return None
                                        # out of use
def myperiodtextslist(myperiodtexts, periodtexts):
    retlist = myperiodtexts
    if myperiodtexts is None:
        retlist = periodtexts
#    if !is.list(myperiodtexts):
#        retlist = list(myperiodtexts)
#    
    return retlist

def getvalues(market, id, start, end, myperiodtexts):
    tablemoveintervaldays = 1
    periodtexts = getperiodtexts(market)
    myperiodtexts = myperiodtextslist(myperiodtexts, periodtexts)
    print("here")
    print(len(myperiodtexts))
    for i in range(0, len(myperiodtexts)):
        periodtext = myperiodtexts[i]
        print(periodtexts)
        print(periodtext)
        print(periodtexts.index(periodtext))
        period = periodtexts.index(periodtext)
        stocks = getstockmarket(allstocks, market)
        stocks = stocks.loc[(stocks.id == id)]

        stocks = stocks.sort_values('date', ascending=[0])
        listdate = split(stocks, stocks.date)
        #print(type(listdate))
        listdates = stocks.date.unique()
        listdates.sort()
        listid = split(stocks, stocks.id)
        dates = MyDates.getdates(listdates, start, end)
        datedstocklists = getdatedstocklists(listdate, listdates, dates, None, tablemoveintervaldays)
        days = dates.endindex - dates.startindex
        stocklistperiod = getlistsorted(datedstocklists, listid, listdate, days, tablemoveintervaldays, reverse=False)
        dflist = []
        print("here", days)
        for j in range(0, days):
            df = stocklistperiod[period][j]
            if len(df) == 1:
                name = df.name.iloc[0]
                list11 = df
                #print(df.name.iloc[0])
                print("%3d %-35s %12s % 6.2f %s" % (i, name[:33], df.date.iloc[0], listperiod(df, period, i), df.id.iloc[0]))
            else:
                print("err" ,len(df))
                
def mytopperiod2(dflist, period, max, days, wantrise=False, wantmacd=False, wantrsi=False):
    #print(type(dflist))
    print("days ", days, " ", len(dflist))
    for j in range(0, days):
        print("j ", j)
        df = dflist[j]
        #if df is None:
        #    continue
        #print(df.index)
        print(type(df))
        if max > len(df):
            max = len(df)
        
        for i in range(0, max):
            rsi = 0 #np.NaN
            if wantrsi:
                rsi = df.rsic.iloc[i]
            
            macd = 0 #np.NaN
            hist = 0 #np.NaN
            macdd = 0 #np.NaN
            histd = 0 #np.NaN
            if wantmacd:
                macd = df.momc.iloc[i]
                hist = df.histc.iloc[i]
                macdd = df.momdc.iloc[i]
                histd = df.histdc.iloc[i]
            
            rise = 0 #np.NaN
            if wantrise:
                #print(list(df.columns.values))
                rise = df.risec.iloc[i]
                #print(df.risec)
            
            name = df.name.iloc[i]
	    #Encoding(name) = "UTF-8"
            l = listperiod(df, period, i)
            #print(l, period, i)
            #print(rise)
            if np.isnan(rise):
                rise = 0
            #print(name[:33], df.date.iloc[i], l, rise, hist, histd, macd, macdd, rsi, df.id.iloc[i])
            #print("rsi " , rsi, type(rsi))
            print("%3d %-35s %12s % 6.2f %3d % 3.2f % 3.2f % 3.2f % 3.2f %3.2f %s" %(i, name[:33], df.date.iloc[i], listperiod(df, period, i), rise, hist, histd, macd, macdd, rsi, df.id.iloc[i]))
        
                                        #        print(df$id[[1]])
    


def myperiodtextslist(myperiodtexts, periodtexts):
    retlist = myperiodtexts
    if myperiodtexts is None:
        retlist = periodtexts
    
    if type(myperiodtexts) is not list:
       retlist = [myperiodtexts]
    
    return(retlist)


def getbottomgraph(market, start, end, numberdays, tablemoveintervaldays, topbottom, myperiodtexts, wantrise=False, wantmacd=False, wantrsi=False, sort=VALUE, macddays=180, deltadays=3, percentize=True):
    return(gettopgraph(market, start, end, numberdays, tablemoveintervaldays, topbottom, myperiodtexts, sort, wantmacd=wantmacd, wantrise=wantrise, wantrsi=wantrsi, macddays=macddays, reverse=True, deltadays=deltadays, percentize=percentize))

def getonedfvalue(df, atype):
    if atype > 0:
        return(getonedfperiod(df, atype))
    if atype < 0:
        return(getonedfspecial(df, atype))
    print("should not be here")

def getonedfvaluearr(df, atype):
    if atype > 0:
        return(getonedfperiodarr(df, atype))
    if atype < 0:
        return(getonedfspecialarr(df, atype))
    print("should not be here")

def gettopgraph(market, start, end, numberdays, tablemoveintervaldays, topbottom, myperiodtexts, sort=VALUE, macddays=180, reverse=False, wantrise=False, wantmacd=False, wantrsi=False, deltadays=3, percentize=True, wantchart=True):
    print("0", market)
    periodtexts = getperiodtexts(market)
    myperiodtexts = myperiodtextslist(myperiodtexts, periodtexts)
    print ("00 " , len(myperiodtexts))
    for i in range(0, len(myperiodtexts)):
        periodtext = myperiodtexts[i]
        print("1", myperiodtexts)
        print("2", periodtexts)
        print("3" , periodtext)
        period = periodtexts.index(periodtext)
        stocks = getstockmarket(allstocks, market)
        listdate = split(stocks, stocks.date)
        #print(listdate[0].date)
        #print(listdate[1].date)
        #print(listdate[len(listdate) - 2].date)
        #print(listdate[len(listdate) - 1].date)
        listdates = stocks.date.unique()
        listdates.sort()
        listid = split(stocks, stocks.id)
        dates = MyDates.getdates(listdates, start, end)
        datedstocklists = getdatedstocklists(listdate, listdates, dates, numberdays, tablemoveintervaldays)
        days = dates.endindex - dates.startindex
        if numberdays is not None:
            days = numberdays
        stocklistperiod = getlistsorted(datedstocklists, listid, listdate, days, tablemoveintervaldays, reverse=reverse)
        periodmaps = None
        if wantrise:
            periodmaps = getlistmove(datedstocklists, listid, listdate, days, tablemoveintervaldays, stocklistperiod)
        
        dflist = []
        headskiprsi = 0
        if end is not None:
            headskiprsi = end
        headskipmacd = 0
        if end is not None:
            headskipmacd = end
        #print(len(periodmaps))
        for j in range(0, days):
            df = stocklistperiod[period][j]
            if wantrise:
                list2 = []
                if j < days:
                    list2 = periodmaps[period][j]
                riselist = [ None for x in range(len(df)) ]
                for i in range(0, len(df)):
                    id = df.id.iloc[i]
                    rise = 0
                    if j < days:
                        #print(type(list2))
                        #print(type(id))
                        #print(id)
                        if True:
                            import time
                            #time.sleep(15)
                        if not list2 is None:
                            rise = list2.get(id)
                            #print(list2.keys())
                            #print(rise)
                            if rise is None:
                                rise = 0 #None
                    riselist[i] = rise
                risec = pd.Series(data = riselist, name = 'risec')
                df['risec'] = risec
                print('riselist')
                print(riselist)
            idc = df.id
            namec = df.name
            datec = df.date
            periodc = getonedfperiod(df, period)
            if wantmacd:
                momlist = []
                histlist = []
                momdlist = []
                histdlist = []
                for mydf in df.itertuples():
                    #print(type(listid))
                    el = next(x for x in listid if x.id.iloc[0] == mydf.id)
                    #print(type(el))
                    el = el.sort_values(by='date', ascending = 0)
                    myc = getonedfvalue(el, period)
                    myclen = len(myc)
                    #print(type(myc))
                    myc = myc.head(n=(myclen-headskipmacd))
                    myc = myc.tail(n=macddays)
                                        #print(myc)
                    if percentize:
                        if periodtext == "Price" or periodtext == "Index":

                            #print("myc")
                            #print(type(myc))
                            #print(myc.values[0])
                            first = myc.values[0]
                            #print(first)
                            #print(100/first)
                            #print(myc.values)
                            myc = myc * (100 / first)
                            #print(myc.values)

                            #print("myc2")
                    #print(mydf.id)
                    global doprint
                    doprint = mydf.id == 'VXAZN'
                    if doprint:
                        print(myc.values)
                    momhist = getmomhist(myc, deltadays)
                    #print(type(momhist))
                    #print(len(momhist))
                                        #print(mom)
                    if not momhist is None:
                        momlist.append(momhist[0])
                        histlist.append(momhist[1])
                        momdlist.append(momhist[2])
                        histdlist.append(momhist[3])
                    else:
                        momlist.append(None)
                        histlist.append(None)
                        momdlist.append(None)
                        histdlist.append(None)
                headskipmacd = headskipmacd + tablemoveintervaldays
                momc = momlist
                histc = histlist
                momdc = momdlist
                histdc = histdlist
                #print('momlist')
                #print(momlist)
                #print(type(momlist))
                df['momc'] = pd.Series(data = momlist, name = 'momc', index = df.index)
                df['histc'] = pd.Series(data = histlist, name = 'histc', index = df.index)
                df['momdc'] = pd.Series(data = momdlist, name = 'momdc', index = df.index)
                df['histdc'] = pd.Series(data = histdlist, name = 'histdc', index = df.index)
                #print(df.name)
                #print(df.momc)
                if sort == MACD:
                    if reverse:
                        df = df.sort_values(by='histc', ascending = 0)
                    else:
                        df = df.sort_values(by='histc', ascending = 1)
            if wantrsi:
                rsilist = []
                headskip = 0
                for mydf in df.itertuples():
                    el = next(x for x in listid if x.id.iloc[0] == mydf.id)
                    #print(type(el))
                    el = el.sort_values(by='date', ascending = 0)
                    #el = listid[mydf.id]
                    #el = el[order(el.date),]
                    myc = getonedfvalue(el, period)
                    myclen = len(myc)
                    myc = myc.head(n=(myclen-headskiprsi))
                    myc = myc.tail(n=macddays)
                    if percentize:
                        if periodtext == "Price" or periodtext == "Index":
                            first = myc.values[0]
                            myc = myc * (100 / first)
                    rsi = rsi.getrsi(myc)
                    rsilist.append(rsi)
                    #if rsi is None:
                    #    print("App rsi none")
                        
                headskiprsi = headskiprsi + tablemoveintervaldays
                rsic = rsilist
                df['rsic'] = pd.Series(data = rsic, name = 'rsic', index = df.index)
                if sort == RSI:
                    if reverse:
                        df = df.sort_values(by='rsic', ascending = 0)
                    else:
                        df = df.sort_values(by='rsic', ascending = 1)
            print("typedf ", type(df))
            dflist.append(df)
        #print("dflist",dflist)
        mytopperiod2(dflist, period, topbottom, days, wantrise=wantrise, wantmacd=wantmacd, wantrsi=wantrsi)
        if not wantchart:
            return
        if reverse:
            getbottomchart(market, days, topbottom, stocklistperiod, period)
        else:
            gettopchart(market, days, topbottom, stocklistperiod, period)
              
def gettopchart(market, days, topbottom, stocklistperiod, period):
    mainlist = stocklistperiod[period][0]
    oldlist = stocklistperiod[period][days - 1]
    #print(type(mainlist.date))
    maindate = mainlist.date[mainlist.date.keys()[0]]
    olddate = oldlist.date[oldlist.date.keys()[0]]
    ls = []
    names = []
    c = 0
    #print(type(mainlist.id))
    keys = mainlist.id.keys()
    print("topb ", topbottom)
    for i in range(0, topbottom):
        l = getelem(mainlist.id[keys[i]], days, stocklistperiod, period, topbottom)
        c = c + 1
        #print("grrl ", type(l), len(l), " ", l[0], " ", l[1], " ", type(l[0]))
        ls.append(l)
        names.append(mainlist.name[keys[i]])
    
    periodtext = getmyperiodtext(market, period)
    print("displ", names, " ", ls, " ", maindate, " ", olddate)
    displaychart(ls, names, topbottom, periodtext, maindate, olddate, days)


def getbottomchart(market, days, topbottom, stocklistperiod, period):
    mainlist = stocklistperiod[period][1]
    oldlist = stocklistperiod[period][days]
    maindate = mainlist.date[1]
    olddate = oldlist.date[1]
    ls = []
    names = []
    c = 0
    len = len(mainlist)
    print(len)
    len = len + 1
    for i in range(0, topbottom):
        l = getelem(mainlist.id[len - i], days, stocklistperiod, period, topbottom)
        c = c + 1
        ls[c] = l
        names[c] = mainlist.name[len - i]
    
    periodtext = getmyperiodtext(market, period)
    displaychart(ls, names, topbottom, periodtext, maindate, olddate, days)


def displaychart(ls, mynames, topbottom, periodtext, maindate, olddate, days):
    #dev.new()
    #colours = rainbow(topbottom)
    print("g_range")

def getmyperiodtext(market, period):
    periodtext = period
    if period >= 0:
        mymeta = getmarketmeta(allmetas, market)
        newtext = getperiodtext(mymeta, period)
        if not newtext is None:
            periodtext = newtext
        
    
    return(periodtext)

def getelem(id, days, stocklistperiod, period, size):
    retl = [ None for x in range(days) ]
    c = 0
    for i in reversed(range(0, days)):
        #print("d ", i)
        retl[c] = np.NaN
        l = stocklistperiod[period][i]
        df = l
        el = df.loc[(df.id == id)]
        if len(el) == 1:
            retl[c] = getonedfperiod(el, period).values[0]
        else:
            print("err1")
        c = c + 1
    #print("retl", retl)
    return(retl)

def getcontentgraph(start, end, tableintervaldays, ids, periodtext, wantmacd=False, wantrsi=False, wantatr=False, wantcci=False, wantstoch=False, wantstochrsi=False, interpolate = True):
    scalebeginning100 = 0
    if len(ids) > 1:
        if periodtext == "price":
            scalebeginning100 = 1
        if periodtext == "index":
            scalebeginning100 = 1
    
    markets = set()
    for id in ids:
        markets.add(id[0])
    marketdatamap = {}
    for market in markets:
        stocks = getstockmarket(allstocks, market)
        listdate = split(stocks, stocks.date)
        listdates = stocks.date.unique()
        listdates.sort()
        periodtexts = getperiodtexts(market)
        #print("days ", days, " " , tableintervaldays)
        dates = MyDates.getdates(listdates, start, end)
        datedstocklists = getdatedstocklists(listdate, listdates, dates, None, tableintervaldays)
        days = dates.endindex - dates.startindex
        marketdatamap[market] = [ stocks, periodtexts, datedstocklists ]
    perioddatamap = {}
    for market in markets:
        marketdata = marketdatamap[market]
        periodtexts = marketdata[1]
        for i in range(periods):
            text = periodtexts[i]
            pair = [market, i]
            pairkey = str(1) + market
                                        #            print(text)
            if perioddatamap.get(text) is None:
                                        #                print("new")
                perioddata = {}
                perioddata["text"] = {}
                perioddatamap[text] = perioddata
            perioddata = perioddatamap[text]
            pairs = perioddata["text"]
            pairs[pairkey] = pair
            perioddata["text"] = pairs
            perioddatamap[text] = perioddata
        if False:
            perioddata = []
            pairs[paste(1, market)] = [market, pricetype]
            perioddata["text"] = pairs
            perioddatamap["price"] = perioddata
        if False:
            perioddata = []
            pairs[paste(1, market)] = [market, indextype]
            perioddata["text"] = pairs
            perioddatamap["index"] = perioddata
    retl = []
    olddate = "old"
    newdate = "new"
    dayset = []
    dayset2 = []
    ls = []
    mynames = []
    for text in perioddatamap:
        if text == periodtext:
                                        #        print(text)
            c = 0
            perioddata = perioddatamap[text]
            pairs = perioddata["text"]
            for pairkey in pairs:
                pair = pairs[pairkey]
                market = pair[0]
                period = pair[1]
                marketdata = marketdatamap[market]
                #print(type(marketdata))
                #print(len(marketdata))
                #print(marketdata)
                datedstocklists = marketdata[2]
                for i in range(len(ids)):
                    idpair = ids[i]
                    idmarket = idpair[0]
                    id = idpair[1]
                                        #           print("for")
                    print(market, idmarket, id)
                    print("")
                    if market == idmarket:
                        print("per", text, " ", id, " ", period, " ")
                        print("")
                        bigretl = getelem3(id, days, datedstocklists, period, topbottom)
                        l3 = bigretl[0]
                        l = l3[0]
                        llow = l3[1]
                        lhigh = l3[2]
                        print("gaga")
                        #print(l)
                        print(type(l))
                        print("scale", scalebeginning100)
                        if scalebeginning100 == 1:
                            print("minmax")
                            print(l)
                            mymin = abs(min(l))
                            mymax = abs(max(l))
                            if mymin > mymax:
                                mymax = mymin
                            for j in range(len(l)):
                                l[j] = l[j] * 100 / mymax;
                            print(l)
                        
                        dayset.extend(bigretl[1])
                        dayset2.extend(bigretl[2])
                        if interpolate:
                            print(type(l))
                            l = l.interpolate(method='linear')
                            if not llow is None:
                                llow = llow.interpolate(method='linear')
                            if not lhigh is None:
                                lhigh = lhigh.interpolate(method='linear')
                            
                        ls.append([l, llow, lhigh])
                        listdf = getelem3tup(id, days, datedstocklists, period, topbottom)
                        df = listdf
                        mynames.append(df.name)
                        c = c + 1
    daynames = dayset
    daynames2 = dayset2
    print("type(daynames)")
    print((daynames[0]))
    print((daynames2[0]))
    #print(daynames[0])
    print(type(daynames[0]))
    print(type(daynames2[0]))
    #print(type(daynames))
    #print(len(daynames))
    olddate = min(daynames)
    newdate = max(daynames)
    olddate = min(daynames2)
    newdate = max(daynames2)
    #print(type(olddate))
    #print(len(olddate))
    #print("")
    #print("ls ", ls)
    #print("")
    plt.rc('axes', grid=True)
    plt.rc('grid', color='0.75', linestyle='-', linewidth=0.5)

    indicators = []
    if wantatr:
        indicators.append(atr.ATR())
    if wantcci:
        indicators.append(cci.CCI())
    if wantmacd:
        indicators.append(macd.MACD())
    if wantrsi:
        indicators.append(rsi.RSI())
    if wantstoch:
        indicators.append(stoch.STOCH())
    if wantstochrsi:
        indicators.append(stochrsi.STOCHRSI())
    
    textsize = 9
    left, width = 0.1, 0.8
    numindicators = len(indicators)
    rect =  [ None for x in range(numindicators + 1) ]
    others = 0
    for i in range(numindicators):
        others = others + 0.4 / numindicators
        rect[i + 1] = [left, 0.5 - others, width, 0.4 / numindicators ]
        #rect2 = [left, 0.3, width, 0.2]
        #rect3 = [left, 0.1, width, 0.2]
    rect[0] = [left, 0.1 + others, width, 0.8 - others ]
    plt.ion()
    title = mynames[0].values + " " + str(olddate) + " - " + str(newdate)
    fig = plt.figure(facecolor='white')
    axescolor = '#f6f6f6'  # the axes background color

    ax =  [ None for x in range(numindicators + 1) ]
    ax[0] = fig.add_axes(rect[0], facecolor=axescolor)  # left, bottom, width, height
    for i in range(numindicators):
        ax[i + 1] = fig.add_axes(rect[i + 1], facecolor=axescolor, sharex=ax[0])
        #ax2t = ax2.twinx()
        #ax3 = fig.add_axes(rect3, facecolor=axescolor, sharex=ax1)

    print("ll ", len(ls), len(ls[0]))
    #print("mynames", type(mynames), len(mynames), mynames, " ", type(mynames[0]), len(mynames[0]))
    print("daes", olddate, newdate)
    displayax(ax[0], ls[0], daynames2, mynames[0].values, 5, periodtext, newdate, olddate, days, title, periodtext)
    myma = ls[0]
    #print("tmyma ", type(myma))
    #print(myma)
    print(myma)
    print("mym", type(myma), len(myma))
    myma = my.fixnaarr(myma)
    myma = my.base100(myma, periodtext)
    #print(myma)
    for i in range(numindicators):
        indicator = indicators[i]
        lses = indicator.calculate(myma)
        print("l0", type(lses), type(lses[0]))
        lsesl = lses[0].tolist()
        lsesr = [ round(num, 1) for num in lsesl ]
        print(lsesr)
        days2 = len(lses[0])
        olddate2 = daynames[days - days2]
        mynames2 = indicator.names()
        text = ''.join(mynames2)
        title = indicator.title()
        displayax(ax[i + 1], lses, daynames2, mynames2, 3, text, newdate, olddate2, days2, title)
                                        #    displaymacd(lses, mynames[1], 1, periodtext, maindate, olddate, days)
    wantrsi2 = None
    if wantrsi2:
        rsis = []
        #for i in range(len(ls)):
        myma = ls[i]
        arsi = rsi.getmyrsi(myma)
        rsis.append(arsi)
        mynames = [ "rsi" ]
        #displayax(ax3, rsis, daynames2, mynames, 5, periodtext, newdate, olddate, days, "RSI")
#    displaychart(lses, mynames2, 3, periodtext, newdate, olddate2, days2)
    plt.show()
                                        #    displaymacd(lses, mynames[1], 1, periodtext, maindate, olddate, days)

def getcomparegraph(start, end, tableintervaldays, ids, interpolate = True):
    scalebeginning100 = 1
    
    markets = set()
    for id in ids:
        markets.add(id[0])
    marketdatamap = {}
    for market in markets:
        stocks = getstockmarket(allstocks, market)
        listdate = split(stocks, stocks.date)
        listdates = stocks.date.unique()
        listdates.sort()
        periodtexts = getperiodtexts(market)
        print("days ", tableintervaldays)
        dates = MyDates.getdates(listdates, start, end)
        print("dates", dates.start, dates.end, dates.startindex, dates.endindex)
        datedstocklists = getdatedstocklists(listdate, listdates, dates, None, tableintervaldays)
        days = dates.endindex - dates.startindex
        print("numdays", days)
        marketdatamap[market] = [ stocks, periodtexts, datedstocklists, listdates ]
    perioddatamap = {}
    for market in markets:
        marketdata = marketdatamap[market]
        periodtexts = marketdata[1]
        listdates = marketdata[3]
        for i in range(periods):
            text = periodtexts[i]
            tuple = [market, i, listdates]
            tuplekey = str(1) + market
                                        #            print(text)
            if perioddatamap.get(text) is None:
                                        #                print("new")
                perioddata = {}
                perioddata["text"] = {}
                perioddatamap[text] = perioddata
            perioddata = perioddatamap[text]
            tuples = perioddata["text"]
            tuples[tuplekey] = tuple
            perioddata["text"] = tuples
            perioddatamap[text] = perioddata
        if False:
            perioddata = []
            tuples[paste(1, market)] = [market, pricetype, listdates]
            perioddata["text"] = tuples
            perioddatamap["price"] = perioddata
        if False:
            perioddata = []
            tuples[paste(1, market)] = [market, indextype, listdates]
            perioddata["text"] = tuples
            perioddatamap["index"] = perioddata
    retl = []
    olddate = "old"
    newdate = "new"
    dayset = []
    dayset2 = []
    ls = []
    dayls = []
    mynames = []
    if True:
        for intuple in ids:
        #for text in perioddatamap:
            print("intuple", intuple)
            #if text == periodtext:
            #        print(text)
            c = 0
            market = intuple[0]
            id = intuple[1]
            periodtext = intuple[2]
            perioddata = perioddatamap[periodtext]
            periodtuples = perioddata["text"]
            if True:
                tuplekey = str(1) + market
                periodtuple = periodtuples[tuplekey]
                market = periodtuple[0]
                indexid = periodtuple[1]
                marketdata = marketdatamap[market]
                #print(type(marketdata))
                #print(len(marketdata))
                #print(marketdata)
                datedstocklists = marketdata[2]
                #for i in range(len(ids)):
                if True:
                                        #           print("for")
                    #print(market, idmarket, id)
                    print("")
                    if True:
                        #print("per", text, " ", id, " ", period, " ")
                        print("Id", id)
                        bigretl = getelem3(id, days, datedstocklists, indexid, topbottom)
                        l3 = bigretl[0]
                        l = l3[0]
                        llow = l3[1]
                        lhigh = l3[2]
                        print("gaga")
                        #print(l)
                        print(type(l))
                        if scalebeginning100 == 1:
                            #print("minmax")
                            #print(l)
                            first = l[0]
                            l = l * 100 / first;
                            #print(l)
                        
                        dayset.extend(bigretl[1])
                        dayset2.extend(bigretl[2])
                        #print(str(bigretl[2]))
                        dayls.append(bigretl[2])
                        if interpolate:
                            print(type(l))
                            l = l.interpolate(method='linear')
                        ls.append(l)
                        listdf = getelem3tup(id, days, datedstocklists, indexid, topbottom)
                        df = listdf
                        #print("Id " + id + " " + str((df.name.values[0])))
                        #print(df)
                        mynames.append(df.name.values[0])
                        c = c + 1
    commondays = dayls[0]
    print("dayls",0,dayls[0])
    for i in range(1, len(dayls)):
        print("dayls",i,dayls[i])
        commondays = intersection(commondays, dayls[i])
    #print(type(commondays), commondays)
    commondays.sort()
    print(commondays)
    commonls = []
    for i in range(len(dayls)):
        tmpdayls = dayls[i]
        tmpls = ls[i]
        #print("tmpls", tmpls)
        newtmpls = []
        for j in range(len(tmpls)):
            if commondays.index(tmpdayls[j]) >= 0:
                newtmpls.append(tmpls[j])
                #print("has"+str(j) + str(tmpdayls[j]) + " " + str(commondays.index(tmpdayls[j])))
            else:
                print("hasnot")
        commonls.append(newtmpls)    
    daynames = dayset
    daynames2 = dayset2
    print("type(daynames)")
    print((daynames[0]))
    print((daynames2[0]))
    #print(daynames[0])
    print(type(daynames[0]))
    print(type(daynames2[0]))
    #print(type(daynames))
    #print(len(daynames))
    olddate = min(daynames)
    newdate = max(daynames)
    olddate = min(daynames2)
    newdate = max(daynames2)
    #print(type(olddate))
    #print(len(olddate))
    #print("")
    #print("ls ", ls)
    #print("")
    plt.rc('axes', grid=True)
    plt.rc('grid', color='0.75', linestyle='-', linewidth=0.5)

    textsize = 9
    left, width = 0.1, 0.8
    rect1 = [left, 0.5, width, 0.4]
    rect2 = [left, 0.3, width, 0.2]
    rect3 = [left, 0.1, width, 0.2]
    plt.ion()
    #print("TT" + str(type(mynames[0])))
    title = str(mynames) + " " + str(olddate) + " - " + str(newdate)
    fig = plt.figure(facecolor='white')
    axescolor = '#f6f6f6'  # the axes background color

    ax1 = fig.add_axes(rect1, facecolor=axescolor)  # left, bottom, width, height
    print("ll ", len(ls), len(ls[0]))
    print("ll ", len(ls), len(ls[1]))
    print("ll ", len(dayls), len(dayls[0]))
    print("ll ", len(dayls), len(dayls[1]))
    print("ll ", len(commonls), len(commonls[0]))
    print("ll ", len(commonls), len(commonls[1]))
    print("ll ", len(dayset2))
    print("ll ", len(commondays))
    print("ll ", mynames)
    #print("mynames", type(mynames), len(mynames), mynames, " ", type(mynames[0]), len(mynames[0]))
    print("daes", olddate, newdate)
    displayax(ax1, commonls, commondays, mynames, 5, periodtext, newdate, olddate, days, title, periodtext)
    percentize = True      
    if percentize:
      if periodtext == "Price" or periodtext == "Index":
          first = []
        #first = myma[0]
        #print("t1 ", type(myma))
        #myma = np.asarray(myma) * (100 / first)
        #myma = pd.Series(data = myma)
        #print("t2 ", type(myma))
    #print("tmyma3 ", type(myma))
    #print(myma)
    plt.show()

def displaychart(ls, mynames, topbottom, periodtext, maindate, olddate, days):
    ####dev.new()
    ####colours = rainbow(topbottom)
    g_range = 0 ####range(0, ls, na.rm=True)
    print("g_range")
    print(g_range)
    plt.ion()
    f, ax = plt.subplots(figsize = (8,4))
    #ax.legend(loc='upper right')

    for i in range(topbottom):
        if i == 0:
                                        #print(l$id[[1]])
                                        #print(l$name[[2]])
            c = ls[0]
            #print("c ", type(c[0][0]))
            #print("c ", type(c[0]))
            #print(c[0])
            #print(c[0][0].values)
            #print(c[0][0])
            #print(len(c[0]))
            #print(len(c[0][0]))
            print("plot0 ", range(len(c)), " : ", c)
            ax.plot(range(len(c)), c, label = mynames[0])
            ####axis(1, at=1:days, lab=c(-(days-1):0))
            #axis(2, las=2)
            #grid(NULL,NULL)
            #box()
                                        #l2 = getc(l, period)
                                        #print(l[[1]]$period1)
                                        #print(l2)
        else:
                                        #print("count", i)
            c = ls[i]
            #print("ploti ", range(len(c)), " : ", c)
            print("ploti ", range(len(c)), " : ", c)
            ax.plot(range(len(c)), c, label = mynames[i])
                                        #print(c)
            #lines(c, type="o", lty = i, col = colours[i], pch = i)

        #title(main=sprintf("Period %s", periodtext))
        #title(xlab=sprintf("Time %s - %s", olddate, maindate))
        #title(ylab="Value")
        #n = mynames[1]
    ax.legend()
    plt.show()
        ####legend(1, g_range[2], mynames, cex=0.8, lty=1:6, pch=1:25, col=colours)

def displayax(ax, ls, daynames, mynames, topbottom, periodtext, maindate, olddate, days, title, ylabel="Value"):
    ####dev.new()
    ####colours = rainbow(topbottom)
    topbottom = len(ls)
    #g_range = 0 ####range(0, ls, na.rm=True)
    #print("g_range")
    #print(g_range)
    #f1, ax1 = plt.subplots(figsize = (8,4))
    #f2, ax2 = plt.subplots(figsize = (8,4))
    #print(olddate)
    #print(type(olddate))
    #print(type(maindate))
    mytitle = "Compare " + str(mynames) + " " + str(olddate) + " - " + str(maindate)
    ax.set(title = title, ylabel = ylabel)
    #ax2.legend(loc = 'upper right')
    #ax2.grid(False)
    print("l3", len(ls), len(mynames))
    for i in range(len(mynames)):
        print("intc ", i, mynames[i])
        if i == 0:
                                        #print(l$id[[1]])
                                        #print(l$name[[2]])
            c = ls
            #ax.plot(range(len(c[i])), c[i], label = mynames[i])
            #print(c[0])
            #print("lens"+str(len(daynames))+" " +str(len(c[i]))+ " " + str(len(mynames[i])))
            #print(mynames[i], c[i])
            ax.plot(daynames, c[i], label = mynames[i])
            #print("c ", type(c[0][0]))
            #print("c ", type(c[0]))
            #print(c[0])
            ####axis(1, at=1:days, lab=c(-(days-1):0))
            #axis(2, las=2)
            #grid(NULL,NULL)
            #box()
                                        #l2 = getc(l, period)
                                        #print(l[[1]]$period1)
                                        #print(l2)
        else:
                                        #print("count", i)
            #print("lens3"+str(len(daynames))+" " +str(len(c[i]))+ " " + str(len(mynames[i])))
            c = ls
            #print(mynames[i], c[i])
            ax.plot(daynames, c[i], label = mynames[i])
            #ax.plot(range(len(c[i])), c[i], label = mynames[i])
            #lines(c, type="o", lty = i, col = colours[i], pch = i)

        #title(main=sprintf("Period %s", periodtext))
        #title(xlab=sprintf("Time %s - %s", olddate, maindate))
        #title(ylab="Value")
        #n = mynames[1]
        ####legend(1, g_range[2], mynames, cex=0.8, lty=1:6, pch=1:25, col=colours)
    ax.legend()

def intersection(a, b):
    return list(set(a) & set(b))
    
def getelem3(id, days, datedstocklist, period, size):
    dayset = []
    dayset2 = []
    retl1 = [ None for x in range(days) ]
    retl2 = [ None for x in range(days) ]
    retl3 = [ None for x in range(days) ]
    c = 0
    print(reversed(range(days)))
    for i in reversed(range(days)):
        retl1[c] = np.NaN
        retl2[c] = np.NaN
        retl3[c] = np.NaN
        #print("dsl")
        #print(len(datedstocklist))
        #print(len(datedstocklist[0]))
        #print(datedstocklist[0])
        #print(datedstocklist[43])
        l = datedstocklist[i]
        #print(type(l))
        #print(len(l))
        #print(l)
        df = l
        el = df.loc[(df.id == id)]
        if len(el) == 1:
            dfarr = getonedfvaluearr(el, period)
            if len(dfarr) == 1:
                retl1[c] = dfarr[0].values[0]
                retl2[c] = None
                retl3[c] = None
            else:
                retl1[c] = dfarr[0].values[0]
                retl2[c] = dfarr[1].values[0]
                retl3[c] = dfarr[2].values[0]
            #print(type(retl[c].values[0]))
            str2 = str(el.date.values[0])
            #print(type(el.date.values[0]))
            #print("lenel", len(el.date), type(el.date), el.date.values[0])
            #print("str2", type(str2))
            dayset.append(str2)
            dayset2.append(el.date.values[0])
        else:
            #prev = dayset[len(dayset) - 1]
            #prev2 = dayset2[len(dayset2) - 1]
            str2 = str(df.date.values[0])
            dayset.append(str2)
            dayset2.append(df.date.values[0])
            print("err2", len(el), c)
            print(el)
        c = c + 1
    retls1 = pd.Series(data = retl1)
    retls2 = pd.Series(data = retl2)
    retls3 = pd.Series(data = retl3)
    #print("dayset ", type(dayset), type(dayset[0]))
    #print("d1", str(dayset))
    #print("d2", str(dayset2))
    print("l ", len(retls1), len(dayset), len(dayset2))
    return([ [ retls1, retls2, retls3 ], dayset,dayset2])

def getelem3tup(id, days, datedstocklist, period, size):
    retl = []
    c = 0
    for i in reversed(range(days)):
        retl.append(np.NaN)
        l = datedstocklist[i]
        df = l
        el = df.loc[(df.id == id)]
        if len(el) == 1:
            return(el)
        else:
            print("err3")
        c = c + 1
    return(retl)

def gettopmonth(id, numberdays = 5, tablemoveintervaldays = 20, topbottom = 10):
    start = (numberdays + 1) * tablemoveintervaldays
    gettopgraph(id, start, None, numberdays, tablemoveintervaldays, topbottom, "1m", wantchart=False)

def gettopweek(id, numberdays = 5, tablemoveintervaldays = 5, topbottom = 10):
    start = (numberdays + 1) * tablemoveintervaldays
    gettopgraph(id, start, None, numberdays, tablemoveintervaldays, topbottom, "1w", wantchart=False)

def gettopcy(id, numberdays = 5, tablemoveintervaldays = 20, topbottom = 10):
    start = (numberdays + 1) * tablemoveintervaldays
    gettopgraph(id, start, None, numberdays, tablemoveintervaldays, topbottom, "cy", wantchart=False)

#engine = create_engine('postgresql://stockread@localhost:5432/stockstat')
conn = psycopg2.connect("host=localhost dbname=stockstat user=stockread password=password")

allstocks = getstocks(conn)
if filterweekend:
    allstocks = allstocks.loc[(allstocks.date.dt.weekday < 5)]
allmetas = getmetas(conn)

plt.close('all')

today = datetime.today().strftime('%Y-%m-%d')
yesterday = datetime.strftime(datetime.now() - timedelta(1), '%Y-%m-%d')

#print(len(stock))
#print(meta)
#print(type(meta))
