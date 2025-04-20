#!/usr/bin/python3

#exec(open("./lib.py").read())

import os
import pandas as pd
#import tensorflow as tf
import numpy as np
import scipy.stats
#import psycopg2
import matplotlib.pyplot as plt

from datetime import datetime, timedelta
import time
import mypost
import request
import multiprocessing as mp

import rise
import day
import ma
import atr
import cci
import macd
import rsi
import stoch
import stochrsi
import obv
import adl
import bbands
import etl

import guiutils as gui
import myutils as my
import pdutils as pdu
import const

from collections import OrderedDict

from sqlalchemy import create_engine
from sqlalchemy.orm import Session

doprint = False

pricetype = -1
indextype = -2
metaperiods = 9
periods = 11
topbottom = 15

filterweekend = True

dovalidate = False

def getmetas(session):
    return pd.read_sql_query('select * from meta', session.bind)

def getstocks(session):
    return pd.read_sql_query('select * from stock', session.bind)

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
        for i in range(metaperiods):
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
            pdstart = pd.Timestamp(start) #np.datetime64(start)
            startdateindex = np.where(np.asarray(listdates) == pdstart)
            #print(startdateindex, pdstart, listdates)
            #print("d", start, pdstart, type(pdstart), type(start), type(listdates), type(listdates[0]), type(startdateindex), startdateindex)
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
            pdend = pd.Timestamp(end) #np.datetime64(end)
            enddateindex = np.where(np.asarray(listdates) == pdend)
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

class StockData:
    def __init__(self, source, market, ids, periods, allstocks, allmetas, mystart, start, end, numberdays=None,
                 tableintervaldays=1, tablemoveintervaldays=1, reverse=False):
        # other sources...
        if source == 'db':
            self.stocks = getstockmarket(allstocks, market)
            self.meta = getmarketmeta(allmetas, market)
            self.meta['datename'] = ['date']
            self.meta['indexname'] = ['indexvalue']
            self.meta['pricename'] = ['price']
        elif source == 'pd':
            print("iii", ids, periods, start, end)
            import pandas_datareader.data as web
            import pandas_datareader.data as data
            import pandas_datareader.wb as wb
            import pandas_datareader.fred as fred
            print("ids0", ids[0])
            if ids[0] == 'GDP':
                data = {'datename' : ['DATE'],
                        'pricename': ['GDP'],
                        'period1': None,
                        'period2': None,
                        'period3': None,
                        'period4': None,
                        'period5': None,
                        'period6': None,
                        'period7': None,
                        'period8': None,
                        'period9': None,
                        'priority': None}
                df = pd.DataFrame(data)
                self.meta = df
                print("mygdp")
            from datetime import datetime
            print("startend", start, end)
            s1 = start.split('-')
            e1 = end.split('-')
            start1 = datetime.strptime(start, '%Y-%m-%d')
            end1 = datetime.strptime(end, '%Y-%m-%d')
            print(end1)
            #fred.
            print("ids", ids[0], market)
            df = web.DataReader(ids[0], market, start, end)
            print("cols", df.columns)
            print(df)
            print(df.GDP)
            #df = df.rename(columns={"GDP" , "indexvalue"}) 
            #aaa = df['Close']
            #series = aaa.values
            #print(df)
            #key = 'GDP'
            #print(df.GDP)
            #print("cols", df.columns)
            #df = df.rename(columns={"GDP" , "indexvalue"})
            #df = df.rename(columns={'GDP' , 'indexvalue'})
            df = df.reset_index()
            df['id'] = ids[0]
            periodtexts = [ "period1", "period2", "period3", "period4", "period5", "period6", "period7", "period8", "period9", "price", "indexvalue", "pricelow", "pricehigh", "priceopen", "indexvaluelow", "indexvaluehigh", "indexvalueopen", "volume", "name" ]
            print("pppp",periods[0])
            #if periods[0] in periodtexts:
            #    periodtexts.remove(periods[0])
            for periodtext in periodtexts:
                df[periodtext] = None
            #print("rename", ids[0], periods[0])
            #df = df.rename(columns={'DATE' : 'date', ids[0] : periods[0]})
            #print("col2s", df.columns)
            #df = df.rename(columns={'DATE' : 'date', ids[0] : "Indexvalue"})
            #print("df0",df)
            self.stocks = df
            #self.meta = None
        elif source == 'file':
            pass
        print("meta", self.meta.datename)
        print("metatype", type(self.meta.datename))
        print("meta", self.meta.datename.values[0])
        self.listdate = split(self.stocks, getstocksdate(self.meta, self.stocks))
        #print("ttt", type(self.listdate), type(self.listdate[0]), self.listdate[0])
        mysum = 0
        for frame in self.listdate:
            mysum += len(frame)
        avg = mysum / len(self.listdate)
        limit = avg / 2
        removed = [frame.date.unique() for frame in self.listdate if len(frame) < limit]
        removedsizes = [len(frame) for frame in self.listdate if len(frame) < limit]
        print("removed " + str(len(removed)) + " " + str(avg) + " " + str(limit) + " " + str(removedsizes))
        self.listdate = [frame for frame in self.listdate if len(frame) >= limit];
        self.listdates = getstocksdate(self.meta, self.stocks).unique()
        self.listdates = [date for date in self.listdates if date not in removed]
        self.listdates.sort()
        self.listid = split(self.stocks, self.stocks.id)
        self.periodtexts = getperiodtexts(market)
        print("hihi", mystart, end, self.listdates)
        self.dates = MyDates.getdates(self.listdates, mystart, end)
        print("d", self.dates)
        self.datedstocklists = getdatedstocklists(self.listdate, self.listdates, self.dates, numberdays, tableintervaldays)
        self.days = self.dates.endindex - self.dates.startindex + 1
        if numberdays is not None:
            self.days = numberdays
        self.stocklistperiod = getlistsorted(self.datedstocklists, self.listid, self.listdate, self.days, tablemoveintervaldays, reverse = reverse)
        self.marketdatamap = {}
        self.marketdatamap[market] = [ self.stocks, self.periodtexts, self.datedstocklists, self.listdates, self.meta ]
        self.tablemoveintervaldays = tablemoveintervaldays
        self.cat = getwantedcategory(self.stocks, self.marketdatamap[market][4])
        print("cat", self.cat)


def getstocksdate(meta, stocks):
    return stocks[meta.datename.values[0]]

def getstockdate(meta, stock):
    return stock[meta.datename.values[0]]

class DataReader:
    def __init__(self, cat):
        self.cat = cat
        self.listmap = None
        self.filllistmap = None
        self.listmap100 = None
        self.filllistmap100 = None
        self.datelist = None
        self.volumelistmap = None
        
    def readData(self, marketdatamap, category, market):
        marketdata = marketdatamap[market]

    def seriesCopy(self, serieslist: list):
        newlist = []
        for i in range(len(serieslist)):
            newlist.append(serieslist[i].copy())
        return newlist
        
    def calculateotherlistmaps(self, interpolate, interpolation, scalebeginning100):
            #print("llllll")
            l = {}
            m = {}
            n = {}
            for k in self.listmap:
                l0 = self.listmap[k]
                #print("l00", l0, type(l0), len(l0))
                #print("l0k", l0.keys())
                #l2 = []
                #for ll in range(len(l0.iloc[0])):
                #    lll = l0.iloc[0][ll]
                #    if lll is None:
                #        l2.append(None)
                #    else:
                #        l2.append(lll)
                #print("l0", l0[0][0:20].tolist())
                #l2 = pd.Series(data = l0, dtype = np.float64)
                #self.listmap[k] = l2
                l2 = self.seriesCopy(l0)
                #print(type(l0[0]), type(l2[0]))
                for i in range(len(l2)):
                    l2[i] = my.fixzero2(l2[i])
                #l = l.interpolate(method='linear')
                m2 = self.seriesCopy(l2)
                if interpolate:
                    #print("interpolating")
                    l2 = my.fixnaarr(l2, interpolation)
                #print("l2", l2[0][0:20].tolist())
                n2 = self.seriesCopy(l2)
                #print("cccat", self.cat, scalebeginning100, len(l2))
                if scalebeginning100 == 1 and len(l2) > 0 and (self.cat > 8 or self.cat == -3):
                    # print("minmax")
                    # print(l)
                    #print(type(l2),l2[0].tolist())
                    first = l2[0][0]
                    #print(firstlist)
                    #first = firstlist[0]
                    #print(first, type(first))
                    #l2 = l2 * 100 / first;
                    m2 = my.base100(m2, "Price")
                    n2 = my.base100(n2, "Price")
                    #n2 = n2 * 100 / first;
                    # print(l)
                #print("l2",type(l),type(m),type(n))
                #print("l2",type(l2),type(m2),type(n2))
                l[k] = l2
                m[k] = m2
                n[k] = n2
                #print("l2", l2[0][0:300].tolist())
                #print("m2", m2[0][0:300].tolist())
                #print("n2", n2[0][0:300].tolist())
            self.filllistmap = l
            self.listmap100 = m
            self.filllistmap100 = n


def adls(start, end, market, period):
    start0 = time.time()
    stockdata = StockData('db', market, None, None, allstocks, allmetas, start, start, end)
    #if days == 0:
    #    days = stockdata.dates.endindex - stockdata.dates.startindex + 1
    #print("days", days)
    marketdatamap = stockdata.marketdatamap
    datareader = DataReader(stockdata.cat)
    periodint = stockdata.cat
    count = 0
    mytableintervaldays = 0
    currentyear = False
    marketids = stockdata.stocks.id.unique()
    datareader.listmap = getseries(
        getarrsparse(market, periodint, count, mytableintervaldays, marketdatamap, currentyear, marketids))
    interpolate = False
    interpolation = None
    scalebeginning100 = 0
    datareader.calculateotherlistmaps(interpolate, interpolation, scalebeginning100)
    datareader.datelist = getdatelist(market, stockdata.marketdatamap)
    #datareadermap[market] = [datareader]
    print("time", time.time() - start0)
    adl1 = adl.getadl(stockdata.datedstocklists, period)
    #adl2 = adl.getadl2(stockdata.stocks, period, stockdata.listdates)
    adl3 = adl.getadl3(stockdata, datareader)
    print(adl1)
    #print(adl2)
    print(adl3)
    amap = {}
    amap["ADL"] = [ [0] + adl3, [ None for x in range(len(adl3) + 1) ], [ None for x in range(len(adl3) + 1) ]]
    return getseries(amap)
    #return datareader
    
def getdatedstocklists(listdate, listdates, dates, numberdays, tableintervaldays):
    datedstocklists = []
    #print(len(listdate))
    #print(len(datedstocklists))
    #print(type(listdate[index]))
    #print("days0 ", days)
    index = dates.startindex
    #datedstocklists.append(listdate[index])
    #print("Index %d" %(dates.startindex), dates.endindex, index, len(listdate))
    #print(listdates[index])
    #print(listdate[index])
    #print(listdate[dates.endindex])
    if numberdays is None:
        for j in range(dates.startindex, dates.endindex + 1):
            #print(index)
            datedstocklists.append(listdate[index])
            index = index + tableintervaldays
    else:
        for j in range(numberdays):
            #print(index)
            datedstocklists.append(listdate[index])
            index = index + tableintervaldays
    #print(len(datedstocklists))
    return datedstocklists

def getlistsorted(datedstocklists, listid, listdate, count, tableintervaldays, wantrise = True, reverse = False):
    stocklistperiod = [[0 for x in range(count)] for y in range(periods)]
                  #matrix([], nrow = periods, ncol = count)
    #print(stocklistperiod)
    #print("count %d %d" % (count, periods))
    #print(datedstocklists[0])
    #print(datedstocklists[1])
    for j in range(count):
        for i in range(periods):
            df = datedstocklists[j] # dataframe make?
            if not i in df:
                continue
            hasperiod = False
            #is.infinit
            #print(type(df))
            #print(len(df))
            #print(i)
            d = pdu.getonedfperiod(df, i)
            #print(d)
            #print(type(pdu.getonedfperiod(df, i)))
            #print(pdu.getonedfperiod(df, i))
            hasperiod = max(pdu.getonedfperiod(df, i))
            if hasperiod:
                ds = None
                if reverse:
                    ds = pdu.getdforderperiodreverse(df, i)
                else:
                    ds = pdu.getdforderperiod(df, i)
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
    retlist = {}
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
        anid = list2[key]
        retlist[anid] = None #np.NaN
        i = np.where(values == anid)
        #print(type(i))
        #print(i)
        i = i[0]
        #print(type(i))
        #print(i)
        if not i is None:
            retlist[anid] = j - i
        j = j + 1
    
    return (retlist)


def listperiod(list, period, index):
    #print("ttt", type(list))
    #print(list)
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

def filterperiod(list, period):
    #print("ttt", type(list))
    #print(list)
    #print(len(list))
    if period == 0:
        return list[list.period1.notnull()]
    if period == 1:
        return list[list.period2.notnull()]
    if period == 2:
        return list[list.period3.notnull()]
    if period == 3:
        return list[list.period4.notnull()]
    if period == 4:
        return list[list.period5.notnull()]
    if period == 5:
        return list[list.period6.notnull()]
    if period == 6:
        return list[list.period7.notnull()]
    if period == 7:
        return list[list.period8.notnull()]
    if period == 8:
        return list[list.period9.notnull()]
    if period == 9:
        return list[list.price.notnull()]
    if period == 10:
        return list[list.indexvalue.notnull()]
    return None

def myperiodtextslist(myperiodtexts, periodtexts):
    retlist = myperiodtexts
    if myperiodtexts is None:
        retlist = periodtexts
#    if !is.list(myperiodtexts):
#        retlist = list(myperiodtexts)
#    
    return retlist

def getvalues(myid, start, end):
#def getvalues(market, id, start, end, myperiodtexts):
    market = myid[0]
    anid = myid[1]
    periodtext = myid[2]
    stockdata = StockData('db', market, None, None, allstocks, allmetas, start, start, end)
    #stocks = stockdata.stocks
    #stocks = stocks.loc[(stocks.id == anid)]
    myperiodtexts = myperiodtextslist( [ periodtext ], stockdata.periodtexts)
    print("here")
    print(len(myperiodtexts))
    for i in range(len(myperiodtexts)):
        periodtext = myperiodtexts[i]
        print(stockdata.periodtexts)
        print(periodtext)
        print(stockdata.periodtexts.index(periodtext))
        period = stockdata.periodtexts.index(periodtext)
        #stocks = stocks.sort_values('date', ascending=[0])
        dflist = []
        print("here", stockdata.days)
        for j in range(stockdata.days):
            df = stockdata.stocklistperiod[period][j]
            df = df[df.id == anid]
            if len(df) == 1:
                name = df.name.iloc[0]
                list11 = df
                #print(df.name.iloc[0])
                print("%3d %-35s %12s % 6.2f %s" % (i, name[:33], df.date.iloc[0], listperiod(df, period, i), df.id.iloc[0]))
            else:
                print("err" ,len(df))
                
def mytopperiod2(indicators, dflist, period, max, days, reverse=False, wantgrid = False):
    #print(wantmacd, wantrsi, wantdays)
    #print(type(dflist))
    print("days ", days, " ", len(dflist))
    for j in range(days):
        print("j ", j)
        df = dflist[j]
        if reverse:
            df = df.iloc[::-1]
        #if df is None:
        #    continue
        #print(df.index)
        print(type(df))
        if max > len(df):
            max = len(df)

        lists = []
        
        titles = [ "num", "name", "date", "value"]
        formats = [ "{:3d}", "{:35.35}", "{:.10}", "{:.2f}" ];
        numindicators = len(indicators)
        for k in range(numindicators):
            indicator = indicators[k]
            titles = titles + indicator.titles()
            formats = formats + indicator.formats()
        titles = titles + [ "id" ]
        formats = formats + [ "{}" ]
        for j in range(len(titles)):
            title = titles[j]
            print(title, end=" ")
        print();
        lists.append(titles)
        df = filterperiod(df, period)
        for i in range(max):
            l = listperiod(df, period, i)
            name = df.name.iloc[i]
            values = [ i, name[:33], df.date.iloc[i].to_datetime64(), l ]
            for k in range(numindicators):
                indicator = indicators[k]
                values = values + indicator.values(df, i)
                #formats = formats + indicator.formats()
            #formats = formats + [ "{}" ]
            values = values + [ df.id.iloc[i] ]
            for j in range(len(titles)):
                value = values[j]
                aformat = formats[j]
                try: 
                    value = aformat.format(value)
                except:
                    import sys
                    print("err", value, aformat, file=sys.stderr)
                if wantgrid:
                    values[j] = value
                print(value, end=" ")
            print();
            lists.append(values)
        if wantgrid:
            import multiprocessing as mp
            mp.Process(target=gui.grid, args=( [ lists ] )).start()

	    #Encoding(name) = "UTF-8"
            #print(l, period, i)
            #print(rise)
            #print(name[:33], df.date.iloc[i], l, rise, hist, histd, macd, macdd, rsi, df.id.iloc[i])
            #print("rsi " , rsi, type(rsi))
            #print(i, name[:33], df.date.iloc[i], listperiod(df, period, i), rise, hist, macd, sign, histd, macdd, sigdd, hist2, macd2, sign2, rsi, dayvalue, df.id.iloc[i])
            #print(" %s" %(i, name[:33], df.date.iloc[i], listperiod(df, period, i), rise, hist, macd, sign, histd, macdd, sigdd, hist2, macd2, sign2, rsi, dayvalue, df.id.iloc[i]))
        
                                        #        print(df$id[[1]])
    


def myperiodtextslist(myperiodtexts, periodtexts):
    retlist = myperiodtexts
    if myperiodtexts is None:
        retlist = periodtexts
    
    if type(myperiodtexts) is not list:
       retlist = [myperiodtexts]
    
    return(retlist)


def getbottomgraph(market, start, end, numberdays, tablemoveintervaldays, topbottom, myperiodtexts, wantrise=False, wantmacd=False, wantrsi=False, sort=const.VALUE, macddays=180, deltadays=3, rebase=False, percentize=True, wantchart=True):
    return(gettopgraph(market, start, end, numberdays, tablemoveintervaldays, topbottom, myperiodtexts, sort, wantmacd=wantmacd, wantrise=wantrise, wantrsi=wantrsi, macddays=macddays, reverse=True, deltadays=deltadays, rebase=rebase, wantchart=wantchart))

def gettopgraph(market, start, end, numberdays, tablemoveintervaldays, topbottom, myperiodtexts, sort=const.VALUE, macddays=180, reverse=False, wantrise=False, wantmacd=False, wantrsi=False, deltadays=3, rebase=False, wantchart=True, interpolate=True, wantdays=False, days=1, wantgrid=False, interpolation = 'linear'):
    print("0", market)
    print(wantmacd, wantrsi, wantdays, rebase, interpolate)
    stockdata = StockData('db', market, None, None, allstocks, allmetas, start, start, end, numberdays=numberdays,
                          tableintervaldays=tablemoveintervaldays, tablemoveintervaldays=tablemoveintervaldays,
                          reverse=reverse)
    periodtexts = stockdata.periodtexts
    myperiodtexts = myperiodtextslist(myperiodtexts, periodtexts)
    print ("00 " , len(myperiodtexts))
    indicators = []
    if wantrise:
        indicators.append(rise.RISE(stockdata))
    if wantmacd:
        indicators.append(macd.MACD(stockdata))
    if wantrsi:
        indicators.append(rsi.RSI(stockdata))
    if wantdays:
        indicators.append(day.DAY(stockdata, days))
    dateset = set(stockdata.listdates)
    for i in range(len(myperiodtexts)):
        periodtext = myperiodtexts[i]
        print("1", myperiodtexts)
        print("2", periodtexts)
        print("3" , periodtext)
        period = stockdata.periodtexts.index(periodtext)
        stocklistperiod = stockdata.stocklistperiod
        
        dflist = []
        headskiprsi = 0
        if end is not None:
            headskiprsi = end
        headskipmacd = 0
        if end is not None:
            headskipmacd = len(stockdata.listdates) - stockdata.dates.endindex
        #print(len(periodmaps))
        for j in range(stockdata.days):
            df = stockdata.stocklistperiod[period][j]
            numindicators = len(indicators)
            for k in range(numindicators):
                indicator = indicators[k]
                df = indicator.dfextend(df, period, periodtext, sort, interpolate = interpolate, rebase = rebase, deltadays = deltadays, reverse = reverse, interpolation = interpolation)
            #idc = df.id
            #namec = df.name
            #datec = df.date
            #periodc = pdu.getonedfperiod(df, period)
            if sort == const.NAME:
                if reverse:
                    df = df.sort_values(by='name', ascending = 0)
                else:
                    df = df.sort_values(by='name', ascending = 1)
            print("typedf ", type(df))
            dflist.append(df)
        #print("dflist",dflist)
        mytopperiod2(indicators, dflist, period, topbottom, stockdata.days, reverse=reverse, wantgrid = wantgrid)
        if not wantchart:
            return
        if reverse:
            getbottomchart(market, stockdata.days, topbottom, stocklistperiod, period)
        else:
            gettopchart(market, stockdata.days, topbottom, stocklistperiod, period)
              
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
    for i in range(topbottom):
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
    for i in range(topbottom):
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
    for i in range(days):
        #print("d ", i)
        retl[c] = np.NaN
        l = stocklistperiod[period][i]
        df = l
        el = df.loc[(df.id == id)]
        if len(el) == 1:
            retl[c] = pdu.getonedfperiod(el, period).values[0]
        else:
            print("err1")
        c = c + 1
    #print("retl", retl)
    return(retl)

def getcontentgraph(start, end, tableintervaldays, ids, wantmacd=False, wantrsi=False, wantatr=False, wantcci=False, wantstoch=False, wantstochrsi=False, wantbbands=False, interpolate = True, expressions = [], interpolation = 'linear'):
    periodtext = ids[0][2]
    scalebeginning100 = 0
    if len(ids) > 1:
        if periodtext == "price":
            scalebeginning100 = 1
        if periodtext == "index":
            scalebeginning100 = 1
    
    markets = set()
    for id in ids:
        markets.add(id[0])
    stockdatamap = {}
    for market in markets:
        stockdatamap[market] = StockData('db', market, None, None, allstocks, allmetas, start, start, end,
                                         tableintervaldays=tableintervaldays)
    perioddatamap = {}
    for market in markets:
        stockdata = stockdatamap[market]
        periodtexts = stockdata.periodtexts
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
                stockdata = stockdatamap[market]
                #print(type(marketdata))
                #print(len(marketdata))
                #print(marketdata)
                datedstocklists = stockdata.datedstocklists
                for i in range(len(ids)):
                    idpair = ids[i]
                    idmarket = idpair[0]
                    anid = idpair[1]
                                        #           print("for")
                    print(market, idmarket, anid)
                    print("")
                    if market == idmarket:
                        print("per", text, " ", anid, " ", period, " ")
                        print("")
                        bigretl = getelem3(anid, stockdata.days, datedstocklists, period, topbottom, text == 'cy')
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
                            l = my.fixzero2(l)
                            #l = l.interpolate(method='linear')
                            l = my.fixna(l, interpolation)
                            if not llow is None:
                                llow = my.fixzero2(llow)
                                #llow = llow.interpolate(method='linear')
                                llow = my.fixna(llow, interpolation)
                            if not lhigh is None:
                                lhigh = my.fixzero2(lhigh)
                                #lhigh = lhigh.interpolate(method='linear')
                                lhigh = my.fixna(lhigh, interpolation)
                            
                        ls.append([l, llow, lhigh])
                        listdf = getelem3tup(anid, stockdata.days, datedstocklists, period, topbottom)
                        df = listdf
                        mynames.append(df.name)
                        c = c + 1
    daynames = dayset
    daynames2 = dayset2
    print("type(daynames)")
    print(type(daynames))
    print(len(daynames))
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
        indicators.append(macd.MACD(stockdata))
    if wantrsi:
        indicators.append(rsi.RSI(stockdata))
    if wantstoch:
        indicators.append(stoch.STOCH())
    if wantstochrsi:
        indicators.append(stochrsi.STOCHRSI())
    if wantbbands:
        indicators.append(bbands.BBANDS())

    rsi.doprint=True
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
    myma0 = ls[0]
    myma0 = [ my.fixzero2(myma0[0]), my.fixzero2(myma0[1]), my.fixzero2(myma0[2]) ]
    myma0 = my.fixnaarr(myma0, interpolation)
    displayax(ax[0], myma0, daynames2, mynames[0].values, 5, periodtext, newdate, olddate, stockdata.days, title, periodtext)
    myma = ls[0]
    #print("tmyma ", type(myma))
    #print(myma)
    print(myma)
    print("mym", type(myma), len(myma))
    myma = [ my.fixzero2(myma[0]), my.fixzero2(myma[1]), my.fixzero2(myma[2]) ]
    print("mem0", myma[0].values)
    myma = my.fixnaarr(myma, interpolation)
    print(myma[0].values)
    print("p",periodtext)
    #myma = my.base100(myma, periodtext)
    #print(myma)
    for i in range(numindicators):
        indicator = indicators[i]
        lses = indicator.calculate(myma)
        print(type(indicator))
        print("l0", type(lses), len(lses))
        #type(lses[0]))
        lsesl = lses[0].tolist()
        lsesr = [ round(num, 1) for num in lsesl ]
        print(lsesr)
        days2 = len(lses[0])
        olddate2 = daynames[stockdata.days - days2]
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

def getcomparegraph(start, end, tableintervaldays, ids, interpolate = True, interpolation = 'linear'):
    scalebeginning100 = 1
    
    markets = set()
    for anid in ids:
        markets.add(anid[0])
    marketdatamap = {}
    stockdatamap = {}
    for market in markets:
        stockdatamap[market] = StockData('db', market, None, None, allstocks, allmetas, start, start, end,
                                         tableintervaldays=tableintervaldays)
    perioddatamap = {}
    for market in markets:
        stockdata = stockdatamap[market]
        periodtexts = stockdata.periodtexts
        listdates = stockdata.listdates
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
            anid = intuple[1]
            periodtext = intuple[2]
            perioddata = perioddatamap[periodtext]
            periodtuples = perioddata["text"]
            if True:
                tuplekey = str(1) + market
                periodtuple = periodtuples[tuplekey]
                market = periodtuple[0]
                indexid = periodtuple[1]
                stockdata = stockdatamap[market]
                #print(type(marketdata))
                #print(len(marketdata))
                #print(marketdata)
                datedstocklists = stockdata.datedstocklists
                #for i in range(len(ids)):
                if True:
                                        #           print("for")
                    #print(market, idmarket, anid)
                    print("")
                    if True:
                        #print("per", text, " ", anid, " ", period, " ")
                        print("Id", anid)
                        bigretl = getelem3(anid, stockdata.days, datedstocklists, indexid, topbottom, text == 'cy')
                        l3 = bigretl[0]
                        l = l3[0]
                        llow = l3[1]
                        lhigh = l3[2]
                        print("gaga")
                        #print(l)
                        print(type(l))
                        if scalebeginning100 == 1:
                            #print("minmax")
                            print(l.tolist())
                            first = l[0]
                            l = l * 100 / first;
                            #print(l)
                        
                        dayset.extend(bigretl[1])
                        dayset2.extend(bigretl[2])
                        #print(str(bigretl[2]))
                        dayls.append(bigretl[2])
                        if interpolate:
                            print(type(l))
                            #l = l.interpolate(method='linear')
                            l = my.fixna(l, interpolation)
                        ls.append(l)
                        listdf = getelem3tup(anid, stockdata.days, datedstocklists, indexid, topbottom)
                        df = listdf
                        #print("Id " + anid + " " + str((df.name.values[0])))
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
    print(type(commondays[0]))
    displayax(ax1, commonls, commondays, mynames, 5, periodtext, newdate, olddate, stockdata.days, title, periodtext)
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

def getcontentgraphnew(start, end, tableintervaldays, ids, wantmacd=False, wantrsi=False, wantatr=False, wantcci=False, wantstoch=False, wantstochrsi=False, wantobv=False, wantbbands=False, interpolate = True, expressions = [], interpolation = 'linear', wantohlc=False, wantma=False, matype = 0, matimeperiod = 30):
    scalebeginning100 = 0
    if not end is None:
        mystart = None
    else:
        mystart = start
    markets = set()
    marketstocks = set()
    allmarketstocks = OrderedDict()
    for alist in ids:
        for aalist in alist:
            # do not add the expression itself
            if not type(aalist) is str:
                markets.add((aalist[0], aalist[1]))
                marketstocks.add(aalist)
        # if simple
        if len(alist) == 1:
            #if "ADL" == alist[1]:
            #    jj = 1
            allmarketstocks[alist[0]] = None
    stockdatamap = {}
    marketids = {}
    periodids = {}
    for marketstock in marketstocks:
        source = marketstock[0]
        market = marketstock[1]
        anid = marketstock[2]
        sourcemarket = (source, market)
        if not market in marketids:
            marketids[sourcemarket] = [ anid ]
            periodids[sourcemarket] = [ marketstock[3] ]
        else:
            l = marketids[sourcemarket].append(anid)
    datareadermap = getDatareaderMap(start, end, interpolate, interpolation, marketids, periodids, markets, scalebeginning100, mystart,
                                     stockdatamap, tableintervaldays)

    for alist in ids:
        # skip simple
        if len(alist) == 1:
            continue
        print(alist)
        mymarkets = set()
        mymarketstocks = set()
        newmap = {}
        for aalist in alist:
            # do not add the expression itself
            print(type(aalist), aalist)
            if not type(aalist) is str:
                mymarkets.add(aalist[0])
                mymarketstocks.add(aalist)
        commondates = getCommonDates(mymarkets, mymarketstocks, stockdatamap)
        method(alist, commondates, stockdatamap, datareadermap, newmap)
    
        addDatareaderComplex(allmarketstocks, commondates, datareadermap, interpolate, interpolation, newmap,
                         scalebeginning100)

    perioddatamap = {}
    retl = []
    olddate = "old"
    newdate = "new"
    dayset = []
    dayset2 = []
    ls = []
    mynames = []
    commonls = []
    for intuple in allmarketstocks:
    #for text in perioddatamap:
        mynames = []
        print("intuple", intuple)
        #if text == periodtext:
        #        print(text)
        c = 0
        source = intuple[0]
        market = intuple[1]
        sourcemarket = (source, market)
        anid = intuple[2]
        if "ADL" == anid:
            jj = 1

        datareaders = datareadermap[sourcemarket]
        pipelinemap = getpipelinemap(datareaders)
        if sourcemarket in stockdatamap:
            stockdata = stockdatamap[sourcemarket] 
            datareader = pipelinemap[stockdata.cat]
            datelist = datareader.datelist
            datelist.sort()
            validate(datelist)
            df = stockdata.stocks[(stockdata.stocks.id == anid)]
            if df.empty:
                name = anid
            else:
                name = df.name.values[0]
            if name is None or len(name) == 0:
                name = anid
            mynames.append(name)
        else:
            datelist = datareaders[0].datelist
            mynames.append(market)
        smalldates = getdates(datelist, start, end)
        #print("smalln", datelist)
        #print("small", smalldates)
        datareader = datareaders[0]
        filllist = datareader.filllistmap[anid]
        volumelist = None
        if anid in datareader.volumelistmap:
            volumelist = datareader.volumelistmap[anid]
        values = [[], [], [], []]
        volume = []
        #print("cds", commondates, datelist)
        #print("cmd",len(datelist),len(filllist[0]),len(smalldates),len(volumelist[0]))
        print(type(volumelist),volumelist)
        for commondate in smalldates:
            dateindex = datelist.index(commondate)
            #print(datareader.listmap.keys())
            #print(datareader.filllistmap.keys())
            #print(datareaders.keys())
            #print(filllist)
            #print(dateindex,len(filllist.iloc[0]))
            #dateindex = len(filllist) - dateindex
            print(commondate, dateindex)
            print(len(filllist[0]), len(filllist[1]))
            value = filllist[0][dateindex]
            values[0].append(value)
            values[1].append(filllist[1][dateindex])
            values[2].append(filllist[2][dateindex])
            values[3].append(filllist[3][dateindex])
            if volumelist is not None:
                volume.append(volumelist[0][dateindex])
        print("vals", values)
        commonls = []
        commonls.append(pd.Series(values[0]))
    
        daynames = smalldates
        daynames2 = smalldates
        print("type(daynames)")
        print(type(daynames))
        print(len(daynames))
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
        vallen = len(values[0])
        #olddate = datelist[len(datelist) - vallen]
        for i in range(len(values[0])):
            if not values[0][i] is None:
                olddate = datelist[i]
                break;
        olddate = smalldates[0]
        print("lgrr", len(datelist), vallen)
        print(mynames, type(mynames))
        title = mynames[0] + " " + str(olddate) + " - " + str(newdate)
        #print(type(olddate))
        #print(len(olddate))
        #print("")
        #print("ls ", ls)
        #print("")
        plt.rc('axes', grid=True)
        plt.rc('grid', color='0.75', linestyle='-', linewidth=0.5)
        plt.ion()

        indicators2 = []
        if wantma:
            indicators2.append(ma.MA(matype, matimeperiod))

        indicators = []
        if wantatr:
            indicators.append(atr.ATR())
        if wantcci:
            indicators.append(cci.CCI())
        if wantmacd:
            indicators.append(macd.MACD(stockdata))
        if wantrsi:
            indicators.append(rsi.RSI(stockdata))
        if wantstoch:
            indicators.append(stoch.STOCH())
        if wantstochrsi:
            indicators.append(stochrsi.STOCHRSI())
        if wantobv:
            indicators.append(obv.OBV(volumelist[0]))
        if wantbbands:
            indicators.append(bbands.BBANDS())

        rsi.doprint=True
        ax = getContentGraphAx(indicators)

        ls = commonls
        print("ll ", len(ls))
        print("ll ", len(ls), len(ls[0]))
        #print("mynames", type(mynames), len(mynames), mynames, " ", type(mynames[0]), len(mynames[0]))
        print("daes", olddate, newdate)
        myma0 = ls #[0]
        print(mynames, type(myma0), len(myma0))
        #myma0 = [ my.fixzero(myma0[0]), my.fixzero(myma0[0]), my.fixzero(myma0[0]) ]
        #myma0 = my.fixnaarr(myma0, interpolation)

        myma0, mynames = getOverlapIndicatorData(datelist, filllist, indicators2, myma0, mynames, smalldates)

        periodtext = 'bla'
        print("mn", mynames)
        #print(len(myma0), myma0[1])
        displayax(ax[0], myma0, daynames2, mynames, 5, periodtext, newdate, olddate, stockdata.days, title, periodtext)
        if wantohlc:
            import mplfinance as mpf
            frame = {}
            frame['Date']=daynames
            frame['Close']=values[0]
            print(type(values[0]),type(values[0][:-1]))
            if None in values[3] or np.isnan(values[3]).any():
                print("not ready")
                frame['Open']=pd.Series([ values[0][0] ] + values[0][:-1])
            else:
                print("ready")
                frame['Open']=pd.Series(values[3])
            frame['Low']=pd.Series(values[1])
            frame['High']=pd.Series(values[2])
            frame['Volume'] = pd.Series(volume)
            df = pd.DataFrame(frame)
            df = df.set_index('Date')
            print(df)
            mpf.plot(df, type='candle', style='charles', title=title, volume=True)
        myma = ls[0]
        #print("tmyma ", type(myma))
        #print(myma)
        print(myma)
        print("mym", type(myma), len(myma))
        #myma = [ my.fixzero2(myma[0]), my.fixzero2(myma[1]), my.fixzero2(myma[2]) ]
        #print("mem0", myma[0].values)
        #myma = my.fixnaarr(myma, interpolation)
        #print(myma[0].values)
        print("p",periodtext)
        #myma = my.base100(myma, periodtext)
        #print(myma)
        myma = filllist
        print("myma0", myma[0])
        #myma[0] = pd.Series(myma[0])
        #myma[1] = pd.Series(myma[1])
        #myma[2] = pd.Series(myma[2])
        numindicators = len(indicators)
        for i in range(numindicators):
            indicator = indicators[i]
            lses = indicator.calculate(myma)
            print(type(indicator))
            print("l0", type(lses), len(lses))
            print(len(lses))
            print(len(lses[0]))
            idxstart = datelist.index(smalldates[0])
            idxend = datelist.index(smalldates[-1])
            for j in range(len(lses)):
                lses[j] = lses[j][idxstart:idxend + 1]
            #type(lses[0]))
            lsesl = lses[0].tolist()
            lsesr = [ round(num, 1) for num in lsesl ]
            print(lsesr)
            days2 = len(lses[0])
            #olddate2 = daynames[stockdata.days - days2]
            mynames2 = indicator.names()
            text = ''.join(mynames2)
            title = indicator.title()
            displayax(ax[i + 1], lses, daynames2, mynames2, 3, text, newdate, olddate, days2, title)
                                        #    displaymacd(lses, mynames[1], 1, periodtext, maindate, olddate, days)
#    displaychart(lses, mynames2, 3, periodtext, newdate, olddate2, days2)
    plt.show()
                                        #    displaymacd(lses, mynames[1], 1, periodtext, maindate, olddate, days)


def validate(datelist):
    if not dovalidate:
        return
    for i in range(len(datelist) - 1):
        if datelist[i] > datelist[i + 1]:
            print("error")
            
def getOverlapIndicatorData(datelist, filllist, overlapIndicators, mydata, mynames, smalldates):
    numindicators = len(overlapIndicators)
    for i in range(numindicators):
        indicator = overlapIndicators[i]
        indicatorData = indicator.calculate(filllist)
        idxstart = datelist.index(smalldates[0])
        idxend = datelist.index(smalldates[-1])
        for j in range(len(indicatorData)):
            indicatorData[j] = indicatorData[j][idxstart:idxend + 1]
        atitle = indicator.title()
        mynames = mynames + atitle
        mydata = mydata + indicatorData
    return mydata, mynames


def getContentGraphAx(indicators):
    textsize = 9
    left, width = 0.1, 0.8
    numindicators = len(indicators)
    rect = [None for x in range(numindicators + 1)]
    others = 0
    for i in range(numindicators):
        others = others + 0.4 / numindicators
        rect[i + 1] = [left, 0.5 - others, width, 0.4 / numindicators]
        # rect2 = [left, 0.3, width, 0.2]
        # rect3 = [left, 0.1, width, 0.2]
    rect[0] = [left, 0.1 + others, width, 0.8 - others]
    fig = plt.figure(facecolor='white')
    axescolor = '#f6f6f6'  # the axes background color
    ax = [None for x in range(numindicators + 1)]
    ax[0] = fig.add_axes(rect[0], facecolor=axescolor)  # left, bottom, width, height
    for i in range(numindicators):
        ax[i + 1] = fig.add_axes(rect[i + 1], facecolor=axescolor, sharex=ax[0])
        # ax2t = ax2.twinx()
        # ax3 = fig.add_axes(rect3, facecolor=axescolor, sharex=ax1)
    return ax


def getCommonDates(markets, marketstocks, stockdatamap):
    print(markets)
    commondates = set(getdatelist(list(markets)[0][1], stockdatamap[list(markets)[0]].marketdatamap))
    for marketstock in marketstocks:
        print(marketstock)
        source = marketstock[0]
        market = marketstock[1]
        stockdata = stockdatamap[(source, market)]
        adateset = set(getdatelist(market, stockdata.marketdatamap))
        commondates = commondates.intersection(adateset)
    commondates = list(commondates)
    commondates.sort()
    return commondates


# expressions: list of
# expression: list of list(pair)
# items and formula
# items: list of item
# item: list(pair)
# formula: string

# [ [ ( "tradcomm", "ADL", "Price" ) ] ]
# [ [ ( "tradcomm", "XAUUSD:CUR", "Price" ) ] ]
# [ [ ( "tradcomm", "XAUUSD:CUR" ), ( "tradcomm", "XAUUSD:CUR", "Price" ) , "\1 / \2" ] ]
# [ [ ( "tradcomm", "XAUUSD:CUR", "Price" ) ] , [ ( "tradcomm", "XAUUSD:CUR", "Price" ), ( "tradcomm", "HG1:COM", "Price" ), "1 2 /"  ] ]

def getdates(datelist, start, end):
    if not start is None:
        start = datetime.strptime(start, "%Y-%m-%d")
        datelist = list(filter(lambda adate: adate >= start, datelist))
        #print("d1", start, datelist)
    if not end is None:
        end = datetime.strptime(end, "%Y-%m-%d")
        datelist = list(filter(lambda adate: adate <= end, datelist))
        #print("d2", end, datelist)
    return datelist

def getcomparegraphnew(start, end, tableintervaldays, ids, wantmacd=False, wantrsi=False, wantatr=False, wantcci=False, wantstoch=False, wantstochrsi=False, wantbbands=False, interpolate = True, interpolation = 'linear'):
    scalebeginning100 = 1
    markets = set()
    marketstocks = set()
    allmarketstocks = OrderedDict()
    for alist in ids:
        for aalist in alist:
            # do not add the expression itself
            if not type(aalist) is str:
                markets.add((aalist[0], aalist[1]))
                marketstocks.add(aalist)
        # if simple
        if len(alist) == 1:
            allmarketstocks[alist[0]] = None
    stockdatamap = {}
    marketids = {}
    periodids = {}
    for marketstock in marketstocks:
        source = marketstock[0]
        market = marketstock[1]
        anid = marketstock[2]
        sourcemarket = (source, market)
        if not sourcemarket in marketids:
            marketids[sourcemarket] = [ anid ]
            periodids[sourcemarket] = [ marketstock[3] ]
        else:
            l = marketids[sourcemarket].append(anid)
            print("llll", l)
    datareadermap = getDatareaderMap(end, interpolate, interpolation, marketids, periodids, markets, scalebeginning100, start,
                                     stockdatamap, tableintervaldays)
    commondates = getCommonDates(markets, marketstocks, stockdatamap)
    newmap = {}
    for alist in ids:
        # skip simple
        if len(alist) == 1:
            continue
        print(alist)
        method(alist, commondates, stockdatamap, datareadermap, newmap)
    addDatareaderComplex(allmarketstocks, commondates, datareadermap, interpolate, interpolation, newmap,
                         scalebeginning100)

    perioddatamap = {}
    retl = []
    olddate = "old"
    newdate = "new"
    dayset = []
    dayset2 = []
    ls = []
    dayls = []
    mynames = []
    commonls = []
    for intuple in allmarketstocks:
    #for text in perioddatamap:
        print("intuple", intuple)
        #if text == periodtext:
        #        print(text)
        c = 0
        source = intuple[0]
        market = intuple[1]
        anid = intuple[2]
        sourcemarket = (source, market)
        if not source == market:
            datareaders = datareadermap[sourcemarket]
        else:
            datareaders = datareadermap[market]
        pipelinemap = getpipelinemap(datareaders)
        if sourcemarket in stockdatamap:
            stockdata = stockdatamap[sourcemarket] 
            datareader = pipelinemap[stockdata.cat]
            datelist = datareader.datelist
            datelist.sort()
            df = stockdata.stocks[(stockdata.stocks.id == anid)]
            if df.empty:
                name = anid
            else:
                name = df.name.values[0]
            if name is None or len(name) == 0:
                name = anid
            mynames.append(name)
        else:
            datelist = commondates
            mynames.append(market)
        datareader = datareaders[0]
        values = []
        #print("cds", commondates, datelist)
        for commondate in commondates:
            dateindex = len(datelist) - datelist.index(commondate)
            #print(datareader.listmap.keys())
            #print(datareader.filllistmap.keys())
            #print(datareaders.keys())
            filllist = datareader.filllistmap100[anid]
            #print(filllist)
            #print(dateindex,len(filllist.iloc[0]))
            dateindex = len(filllist[0]) - dateindex
            #print(commondate, dateindex)
            value = filllist[0][dateindex]
            values.append(value)
        print("vals", values)
        commonls.append(values)
        periodtext = intuple[3]
    print(dayls)
    commondays = commondates
    #print("dayls",0,dayls[0])
    #for i in range(1, len(dayls)):
    #    print("dayls",i,dayls[i])
    #    commondays = intersection(commondays, dayls[i])
    #print(type(commondays), commondays)
    commondays.sort()
    print(commondays)
    #commonls = []
    daynames = commondates
    daynames2 = commondates
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
    title = str(mynames) + " " + str(olddate) + " - " + str(newdate)
    #print(type(olddate))
    #print(len(olddate))
    #print("")
    #print("ls ", ls)
    #print("")

    plt.rc('axes', grid=True)
    plt.rc('grid', color='0.75', linestyle='-', linewidth=0.5)
    plt.ion()

    ax1 = getCompareGraphAx()
    ls = commonls
    dayls = ls
    #print("ll", ls)
    #print("ll ", len(ls), len(ls[0]))
    #print("ll ", len(ls), len(ls[1]))
    print("ll ", len(dayls), len(dayls[0]))
    #print("ll ", len(dayls), len(dayls[1]))
    #print("ll ", len(commonls), len(commonls[0]))
    #print("ll ", len(commonls), len(commonls[1]))
    print("ll ", len(dayset2))
    print("ll ", len(commondays))
    print("llm ", mynames, periodtext, newdate, olddate)
    #print("mynames", type(mynames), len(mynames), mynames, " ", type(mynames[0]), len(mynames[0]))
    print("daes", olddate, newdate)
    print(type(commondays[0]))
    displayax(ax1, commonls, commondays, mynames, 5, periodtext, newdate, olddate, stockdata.days, title, periodtext)
    plt.show()


def getCompareGraphAx():
    textsize = 9
    left, width = 0.1, 0.8
    rect1 = [left, 0.5, width, 0.4]
    rect2 = [left, 0.3, width, 0.2]
    rect3 = [left, 0.1, width, 0.2]
    # print("TT" + str(type(mynames[0])))
    fig = plt.figure(facecolor='white')
    axescolor = '#f6f6f6'  # the axes background color
    ax1 = fig.add_axes(rect1, facecolor=axescolor)  # left, bottom, width, height
    return ax1


def addDatareaderComplex(allmarketstocks, commondates, datareadermap, interpolate, interpolation, newmap,
                         scalebeginning100):
    for anid in newmap.keys():
        alist = newmap[anid]
        listmap = {}
        listmap[anid] = [alist, [], [], []]
        newdatareader = DataReader(-3)
        newdatareader.listmap = getseries(listmap)
        newdatareader.calculateotherlistmaps(interpolate, interpolation, scalebeginning100)
        newdatareader.datelist = commondates
        print("anid", anid)
        datareadermap[anid] = [newdatareader]
        allmarketstocks[(anid, anid, anid, None)] = None


def getDatareaderMap(start, end, interpolate, interpolation, marketids, periodids, markets, scalebeginning100, mystart, stockdatamap,
                     tableintervaldays):
    datareadermap = {}
    for sourcemarket in markets:
        source = sourcemarket[0]
        market = sourcemarket[1]
        # market[0], market[1] is source, market
        stockdatamap[sourcemarket] = StockData(source, market, marketids[sourcemarket], periodids[sourcemarket],
                                               allstocks, allmetas, mystart, start, end,
                                               tableintervaldays=tableintervaldays)
        marketdatamap = stockdatamap[sourcemarket].marketdatamap
        datareader = DataReader(stockdatamap[sourcemarket].cat)
        periodint = stockdatamap[sourcemarket].cat
        count = 0
        mytableintervaldays = 0
        currentyear = False
        datareader.listmap = getseries(
            getarrsparse(market, periodint, count, mytableintervaldays, marketdatamap, currentyear, marketids[sourcemarket]))
        datareader.calculateotherlistmaps(interpolate, interpolation, scalebeginning100)
        datareader.datelist = getdatelist(market, stockdatamap[sourcemarket].marketdatamap)
        datareader.volumelistmap = getseries(getvolumes(market, periodint, count, mytableintervaldays, marketdatamap, currentyear, marketids[sourcemarket]))
        if "ADL" in marketids[sourcemarket]:
            jj = 1
            datareader.listmap = adls(mystart, end, market, periodint)
            datareader.calculateotherlistmaps(interpolate, interpolation, scalebeginning100)
        datareadermap[sourcemarket] = [datareader]
            
    return datareadermap


def getseries(amap):
    for key in amap:
        #print("ama", amap.keys())
        #print("ama", type(amap[key]), amap[key])
        alist = amap[key]
        for i in range(len(alist)):
            #print("al1", alist[i])
            alist[i] = pd.Series(alist[i])
            #print("al2", alist[i].tolist())
        amap[key] = alist
        #print("ama", type(amap[key]), amap[key])
    return amap

def getarrsparse(market, periodint, count, mytableintervaldays, marketdatamap, currentyear, ids):
    # TODO filter only wanted ids...
    retmap = {}
    print("mkeys", marketdatamap.keys(), ids)
    datedstocklists = marketdatamap[market][2]
    meta = marketdatamap[market][4]
    index = 0
    #print("ids", ids, len(datedstocklists), currentyear)
    if not currentyear:
        if index >= 0:
            for i in range(len(datedstocklists)):
                #print("i",i)
                for anid in ids:
                        stocklist = datedstocklists[i]
                        stock = stocklist.loc[(stocklist.id == anid)]
                        if stock.empty:
                            continue
                        #for stock in stocklist:
                        #anid = stock.id
                        df = stock
                        el = df.loc[(df.id == anid)]
                        if not len(el) == 1:
                            continue
                        #print("da", el.date)
                        dfarr = pdu.getonedfvaluearr(el, periodint, meta)
                        #print("dfarr",type(dfarr), len(dfarr), dfarr)
                        #value = dfarr[2].values
                        #print(type(value))
                        #print("el", el, len(el))
                        #print(dfarr, el.date)
                        #print(dfarr)
                        #print(len(dfarr))
                        #print(len(dfarr[0]))
                        #print(len(dfarr[0].values), len(dfarr[1].values), len(dfarr[2].values))
                        if len(dfarr) == 1:
                            values = [ dfarr[0].values[0], None, None, None ]
                        else:
                            values = [ dfarr[0].values[0], dfarr[1].values[0], dfarr[2].values[0], dfarr[3].values[0] ]
                        #print("vs ", values)
                        mapadd(retmap, anid, i, values , len(datedstocklists))
                        #len(datedstocklists) - 1 - 
    else:
        basenumbermap = {}
        lastnumbermap = {}
        yearmap = {}
        for i in range(len(datedstocklists)):
            #print("i",i)
            stocklist = datedstocklists[i]
            #print(type(stocklist), stocklist)
            for anid in ids:
                    stock = stocklist.loc[(stocklist.id == anid)]
                    if stock.empty:
                        continue
                    #for stock in stocklist:
                    #print(type(stock), stock)
                    #anid = stock.id
                    #print(stock.date, type(stock.date))
                    #print(anid, stock, stocklist)
                    curyear = stock.date.iloc[0].year
                    if not anid in yearmap.keys():
                        thisyear = curyear;
                        yearmap[anid] = thisyear
                    if curyear != thisyear:
                        if not anid in basenumbermap.keys():
                            basenumber = 1.0
                        else:
                            basenumber = basenumbermap[anid]
                        if not anid in lastnumbermap.keys():
                            basenumbermap[anid] = basenumber
                        else:
                            lastnumber = lastnumbermap[anid]
                        basenumbermap[anid] = lastnumber
                        yearmap[anid] = curyear
                    df = stock
                    el = df.loc[(df.id == anid)]
                    #print("da", el.date, el.price, i)
                    dfarr = pdu.getonedfvaluearr(el, periodint, meta)
                    value = dfarr[0].values
                    #print(value, type(value))
                    for ii in range(len(value)):
                        if not value[ii] is None:
                            value[ii] = 0.01 * value[ii] + 1
                            if not anid in basenumbermap.keys():
                                basenumber = 1.0
                            else:
                                basenumber = basenumbermap[anid]
                            value[ii] = value[ii] * basenumber
                        # TODO
                        if not value[ii] is None and ii == 0:
                            lastnumbermap[anid] = value[ii]
                    mapadd(retmap, anid, i, value, len(datedstocklists))
                    #len(datedstocklists) - 1 - 
    #print("rrr", retmap)
    return retmap

def getvolumes(market, periodint, count, mytableintervaldays, marketdatamap, currentyear, ids):
    # TODO filter only wanted ids...
    retmap = {}
    print("mkeys", marketdatamap.keys(), ids)
    datedstocklists = marketdatamap[market][2]
    index = 0
    print("ids", ids, len(datedstocklists), currentyear)
    if not currentyear:
        if index >= 0:
            for i in range(len(datedstocklists)):
                #print("i",i)
                for anid in ids:
                        stocklist = datedstocklists[i]
                        stock = stocklist.loc[(stocklist.id == anid)]
                        if stock.empty:
                            continue
                        #for stock in stocklist:
                        #anid = stock.id
                        df = stock
                        el = df.loc[(df.id == anid)]
                        if not len(el) == 1:
                            continue
                        dfarr = [ el.volume ]
                        values = [ dfarr[0].values[0] ]
                        mapadd(retmap, anid, i, values , len(datedstocklists))
    else:
        basenumbermap = {}
    #print("rrr", retmap)
    return retmap

def mapadd(amap, anid, index, value, length):
    found = anid in amap.keys()
    #print("lens",length,len(value))
    if not found:
        w, h = length, len(value)
        array = [[None for x in range(w)] for y in range(h)]
        #print("aaaa", type(array), w, h)
        amap[anid] = array
    else:
        array = amap[anid]
    for i in range(len(value)):
        array[i][index] = value[i]

def getpipelinemap(datareaders):
    map = {}
    for datareader in datareaders:
        map[datareader.cat] = datareader
    return map
    
def getdatareaders(market, marketdatamap):
    i = None

def getdatelist(market, marketdatamap):
    retlist = []
    print(marketdatamap.keys())
    datedstocklists = marketdatamap[market][2]
    meta = marketdatamap[market][4]
    for i in range(len(datedstocklists)):
        alist = datedstocklists[i]
        #print(type(alist), len(alist))
        if (len(alist) > 0):
            #print(type(list.iloc[0]))
            retlist.append(getstocksdate(meta, alist.iloc[0]))
    #print("retl", retlist)
    return retlist


def method(alist: list, commondates: set, stockdatamap, datareadermap, newmap):
    marketstocks = alist[:-1]
    expression = alist[-1]
    dates = list(commondates)
    dates.sort();
    print("dates", dates)
    newvalues = []
    for date in dates:
        values = []
        #print("mses", marketstocks)
        for marketstock in marketstocks:
            source = marketstock[0]
            market = marketstock[1]
            anid = marketstock[2]
            catname = marketstock[3]
            sourcemarket = (source, market)
            stockdata = stockdatamap[sourcemarket]
            if (catname is None):
                catname = stockdata.catname;
            cat = stockdata.cat
            datareaders = datareadermap[sourcemarket]
            datareader = datareaders[0]
            datelist = datareader.datelist
            print(datareadermap.keys())
            print(datareader.filllistmap.keys())
            filllist = datareader.filllistmap[anid]
            dateindex = len(datelist) - datelist.index(date)
            print("dv", date, dateindex)
            #print(anid, filllist)
            #[0]
            dateindex = len(filllist[0]) - dateindex
            dateindex = datelist.index(date)
            print("dvx", date, dateindex, len(datelist), len(filllist[0]))
            value = filllist[0][dateindex]
            print("dv", dateindex, value)
            values.append(value)
        #print("tis", type(values), values)
        if None in values:
            newvalues.append(None)
            continue
        parts = expression.split()
        #print("fl", filllist[0])
        print(values,parts)
        for i in range(len(parts)):
            if parts[i].isdigit():
                parts[i] = str(values[int(parts[i]) - 1])
        import postfix
        print("p", parts)
        print("p", ' '.join(parts))
        calc = postfix.doPostfix(' '.join(parts))
        newvalues.append(calc[0])
    newid = "";
    for ms in marketstocks:
        source = ms[0]
        market = ms[1]
        anid = ms[2]
        cat = ms[3]
        newid = newid + market + "." + anid + "." + cat + " "
    newid = newid + expression
    newmap[(newid, newid)] = newvalues
    return newmap

def getwantedcategory(stocks: list, meta):
    defaultpris = [ pricetype, indextype ]
    defaultpris = [ 9, 10 ]
    defaultpriorities = [ "Price", "Index" ]
    priorities = []
    priority = None
    if meta is not None:
        priority = meta.priority
    if priority is not None and len(priority) > 0 and priority.iloc[0] is not None:
        priorities = priority.tolist()
    else:
        priorities = defaultpriorities
    print(priority, priorities)
    for apriority in priorities:
        for i in range(len(defaultpriorities)):
            print("a", apriority, defaultpriorities[i])
            if defaultpriorities[i] == apriority:
                #if defaultpris[i] in stocks and hasstockvalue(stocks, defaultpris[i]):
                print("c", defaultpris[i])
                if hasstockvalue(meta, stocks, defaultpris[i]):
                    print("i",i,defaultpris[i])
                    return defaultpris[i]
        periods = []
        if meta is not None:
            periods = [ meta.period1, meta.period2, meta.period3, meta.period4, meta.period5, meta.period6, meta.period7, meta.period8 ]
        for i in range(len(periods)):
            if len(periods[i]) == 0:
                continue
            print("b", apriority, i, len(periods[i]), periods[i])
            print("b", apriority, periods[i].iloc[0])
            if apriority == periods[i].iloc[0]:
                print("cc3")
                if hasstockvalue(meta, stocks, i):
                    return i
    return None

def hasstockvalue(meta, stocks, pri):
    print("ttt", type(stocks), pri)
    periodtext = [ "period1", "period2", "period3", "period4", "period5", "period6", "period7", "period8", "period9", "price", "indexvalue" ]
    text = periodtext[pri]
    if pri == 9:
        text = meta.pricename.values[0]
    if pri == 10:
        text = meta.indexvaluename.values[0]
    print("text", text)
    x = stocks[text]
    #x = stocks["GDP"]
    #print("yyy", stocks)
    #print("xxx", stocks.columns)
    #print("xxx", x)
    return not np.isnan(x.max())

def displaychart(ls, mynames, topbottom, periodtext, maindate, olddate, days):
    ####dev.new()
    ####colours = rainbow(topbottom)
    g_range = 0 ####range(ls, na.rm=True)
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
    display = os.environ.get('DISPLAY')
    if display is None:
        return

    ####dev.new()
    ####colours = rainbow(topbottom)
    topbottom = len(ls)
    #g_range = 0 ####range(ls, na.rm=True)
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
    
def getelem3(anid, days, datedstocklist, period, size, handlecy):
    print("Is this unused?")
    meta = None
    dayset = []
    dayset2 = []
    retl1 = [ None for x in range(days) ]
    retl2 = [ None for x in range(days) ]
    retl3 = [ None for x in range(days) ]
    c = 0

    base = None
    year = None

    print(range(days))
    for i in range(days):
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
        el = df.loc[(df.id == anid)]
        if len(el) == 1:
            dfarr = pdu.getonedfvaluearr(el, period, meta)
            if len(dfarr) == 1:
                retl1[c] = dfarr[0].values[0]
                #dfarr[0].values[0] = dfarr[0].values[0] * 0.01 + 1
                #print("pppp")
                if handlecy == True:
                    retl1[c] = 0.01 * retl1[c] + 1
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
            if handlecy == True:
                mydate = pd.DatetimeIndex([el.date.values[0]])
                myyear = mydate.year
                if not myyear == year:
                    if base is None:
                        base = 1.0
                    else:
                        print("rr", prevNonNan(retl1, c-1))
                        base = prevNonNan(retl1, c - 1)
                    year = myyear
                print(retl1[c], base)
                retl1[c] = retl1[c] * base
                print(2, retl1[c])
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

def getelem3tup(anid, days, datedstocklist, period, size):
    retl = []
    c = 0
    for i in range(days):
        retl.append(np.NaN)
        l = datedstocklist[i]
        df = l
        el = df.loc[(df.id == anid)]
        if len(el) == 1:
            return(el)
        else:
            print("err3",len(el),anid)
        c = c + 1
    return(retl)

def gettop10y(anid, numberdays = 5, tablemoveintervaldays = 5, toptop = 10):
    start = (numberdays - 1) * tablemoveintervaldays
    gettopgraph(anid, start, None, numberdays, tablemoveintervaldays, topbottom, "10y", wantchart=False)

def gettop5y(anid, numberdays = 5, tablemoveintervaldays = 5, toptop = 10):
    start = (numberdays - 1) * tablemoveintervaldays
    gettopgraph(anid, start, None, numberdays, tablemoveintervaldays, topbottom, "5y", wantchart=False)

def gettop3y(anid, numberdays = 5, tablemoveintervaldays = 5, toptop = 10):
    start = (numberdays - 1) * tablemoveintervaldays
    gettopgraph(anid, start, None, numberdays, tablemoveintervaldays, topbottom, "3y", wantchart=False)

def gettopyear(anid, numberdays = 5, tablemoveintervaldays = 20, topbottom = 10):
    start = (numberdays - 1) * tablemoveintervaldays
    gettopgraph(anid, start, None, numberdays, tablemoveintervaldays, topbottom, "1y", wantchart=False)

def gettopcy(anid, numberdays = 5, tablemoveintervaldays = 20, topbottom = 10):
    start = (numberdays - 1) * tablemoveintervaldays
    gettopgraph(anid, start, None, numberdays, tablemoveintervaldays, topbottom, "cy", wantchart=False)

def gettop3m(anid, numberdays = 5, tablemoveintervaldays = 20, topbottom = 10):
    start = (numberdays - 1) * tablemoveintervaldays
    gettopgraph(anid, start, None, numberdays, tablemoveintervaldays, topbottom, "3m", wantchart=False)

def gettopmonth(anid, numberdays = 5, tablemoveintervaldays = 20, topbottom = 10):
    start = (numberdays - 1) * tablemoveintervaldays
    gettopgraph(anid, start, None, numberdays, tablemoveintervaldays, topbottom, "1m", wantchart=False)

def gettopweek(anid, numberdays = 5, tablemoveintervaldays = 5, topbottom = 10):
    start = (numberdays - 1) * tablemoveintervaldays
    gettopgraph(anid, start, None, numberdays, tablemoveintervaldays, topbottom, "1w", wantchart=False)

def gettopday(anid, numberdays = 5, tablemoveintervaldays = 5, topbottom = 10):
    start = (numberdays - 1) * tablemoveintervaldays
    gettopgraph(anid, start, None, numberdays, tablemoveintervaldays, topbottom, "1d", wantchart=False)

def getbottom10y(anid, numberdays = 5, tablemoveintervaldays = 5, topbottom = 10):
    start = (numberdays - 1) * tablemoveintervaldays
    getbottomgraph(anid, start, None, numberdays, tablemoveintervaldays, topbottom, "10y", wantchart=False)

def getbottom5y(anid, numberdays = 5, tablemoveintervaldays = 5, topbottom = 10):
    start = (numberdays - 1) * tablemoveintervaldays
    getbottomgraph(anid, start, None, numberdays, tablemoveintervaldays, topbottom, "5y", wantchart=False)

def getbottom3y(anid, numberdays = 5, tablemoveintervaldays = 5, topbottom = 10):
    start = (numberdays - 1) * tablemoveintervaldays
    getbottomgraph(anid, start, None, numberdays, tablemoveintervaldays, topbottom, "3y", wantchart=False)

def getbottomyear(anid, numberdays = 5, tablemoveintervaldays = 20, topbottom = 10):
    start = (numberdays - 1) * tablemoveintervaldays
    getbottomgraph(anid, start, None, numberdays, tablemoveintervaldays, topbottom, "1y", wantchart=False)

def getbottomcy(anid, numberdays = 5, tablemoveintervaldays = 20, topbottom = 10):
    start = (numberdays - 1) * tablemoveintervaldays
    getbottomgraph(anid, start, None, numberdays, tablemoveintervaldays, topbottom, "cy", wantchart=False)

def getbottom3m(anid, numberdays = 5, tablemoveintervaldays = 20, topbottom = 10):
    start = (numberdays - 1) * tablemoveintervaldays
    getbottomgraph(anid, start, None, numberdays, tablemoveintervaldays, topbottom, "3m", wantchart=False)

def getbottommonth(anid, numberdays = 5, tablemoveintervaldays = 20, topbottom = 10):
    start = (numberdays - 1) * tablemoveintervaldays
    getbottomgraph(anid, start, None, numberdays, tablemoveintervaldays, topbottom, "1m", wantchart=False)

def getbottomweek(anid, numberdays = 5, tablemoveintervaldays = 5, topbottom = 10):
    start = (numberdays - 1) * tablemoveintervaldays
    getbottomgraph(anid, start, None, numberdays, tablemoveintervaldays, topbottom, "1w", wantchart=False)

def getbottomday(anid, numberdays = 5, tablemoveintervaldays = 5, topbottom = 10):
    start = (numberdays - 1) * tablemoveintervaldays
    getbottomgraph(anid, start, None, numberdays, tablemoveintervaldays, topbottom, "1d", wantchart=False)

def prevNonNan(alist, pos):
  l = pos
  for i in range(pos):
      #print("i", i)
      if not alist[l - i] is None and not np.isnan(alist[l - i]):
          return alist[l - i]
  return 0

def rangei(stop):
    return range(stop + 1)


def simulateinvest2(market, startdate = None, enddate = None, confidence = False, confidencevalue = 0.7, confidencefindtimes = 4, stoploss = True, stoplossvalue = 0.9, indicatorpure = False, indicatorrebase = False, indicatorreverse = False, mldate = False, stocks = 5, buyweight = False, interval = 7, adviser = 0, period = 0, interpolate = False, intervalstoploss = True, intervalstoplossvalue = 0.9, day = 1, delay = None, intervalwhole = False, confidenceholdincrease = False, noconfidenceholdincrease = True, noconfidencetrenddecrease = False, noconfidencetrenddecreasetimes = 1, confidencetrendincrease = False, confidencetrendincreasetimes = 1, indicatordirection = False, indicatordirectionup = True, volumelimits = None, abovebelow = False):
    simulateinvest(market, startdate, enddate, confidence, confidencevalue, confidencefindtimes, stoploss, stoplossvalue, indicatorpure, indicatorrebase, indicatorreverse, mldate, stocks, buyweight, interval, adviser, period, interpolate, intervalstoploss, intervalstoplossvalue, day, delay, intervalwhole, confidenceholdincrease, noconfidenceholdincrease, noconfidencetrenddecrease, noconfidencetrenddecreasetimes, confidencetrendincrease, confidencetrendincreasetimes, indicatordirection, indicatordirectionup, volumelimits, abovebelow)
    
def simulateinvest(market, startdate = None, enddate = None, confidence = False, confidenceValue = 0.7, confidenceFindTimes = 4, stoploss = True, stoplossValue = 0.9, indicatorPure = False, indicatorRebase = False, indicatorReverse = False, mldate = False, stocks = 5, buyweight = False, interval = 7, adviser = 0, period = 0, interpolate = False, intervalStoploss = True, intervalStoplossValue = 0.9, day = 1, delay = None, intervalwhole = False, confidenceholdincrease = False, noconfidenceholdincrease = True, noconfidencetrenddecrease = False, noconfidencetrenddecreasetimes = 1, confidencetrendincrease = False, confidencetrendincreasetimes = 1, indicatordirection = False, indicatordirectionup = True, volumelimits = None, abovebelow = False):
    data = { 'startdate' : startdate, 'enddate' : enddate, 'confidence' : confidence, 'confidenceValue' : confidenceValue, 'confidenceFindTimes' : confidenceFindTimes, 'stoploss' : stoploss, 'stoplossValue' : stoplossValue, 'indicatorPure' : indicatorPure, 'indicatorRebase' : indicatorRebase, 'indicatorReverse' : indicatorReverse, 'mldate' : mldate, 'stocks' : stocks, 'buyweight' : buyweight, 'interval' : interval, 'adviser' : adviser, 'period' : period, 'interpolate' : interpolate, 'intervalStoploss' : intervalStoploss, 'intervalStoplossValue' : intervalStoplossValue, 'day' : day, 'delay' : delay, 'intervalwhole' : intervalwhole, 'confidenceholdincrease' : confidenceholdincrease, 'noconfidenceholdincrease': noconfidenceholdincrease, 'noconfidencetrenddecrease' : noconfidencetrenddecrease, 'noconfidencetrenddecreaseTimes' : noconfidencetrenddecreasetimes, 'confidencetrendincrease' : confidencetrendincrease, 'confidencetrendincreaseTimes' : confidencetrendincreasetimes, 'indicatorDirection' : indicatordirection, 'indicatorDirectionUp' : indicatordirectionup, 'volumelimits' : volumelimits, 'abovebelow' : abovebelow }
    print(market, data)
    response = request.request1(market, data)
    #print(type(response))
    #print(response)
    #print(response.text)
    #print(response.json())
    resp = response.json()
    #print(resp)
    webdata = resp['webdatajson']
    #print(webdata)
    #print(type(webdata))
    #print(webdata.keys())
    updatemap = webdata['updateMap']
    #print(updatemap)
    #print(updatemap.keys())
    if 'empty' in updatemap:
        return
    dates = updatemap['plotdates']
    commondays = dates
    #print(type(dates))
    default = updatemap['plotdefault']
    capital = updatemap['plotcapital']
    if len(capital) == 0:
        return
    else:
        print("Capital: ", capital[-1])
        print("Last stocks: ", updatemap['laststocks'])
    geom = np.geomspace(capital[0], capital[-1],num=len(capital),endpoint=True, dtype=None, axis=0)
    if len(capital) >= 2:
        pearson = scipy.stats.pearsonr(capital, geom)
        spearman = scipy.stats.spearmanr(capital, geom)
        kendalltau = scipy.stats.kendalltau(capital, geom)
        pearson = round(pearson[0], 2)
        spearman = round(spearman[0], 2)
        kendalltau = round(kendalltau[0], 2)
    else:
        pearson = 0
        spearman = 0
        kendalltau = 0
    commonls = [ default, capital, geom ]
    mynames = [ "default", "capital", "geom" ]
    plt.rc('axes', grid=True)
    plt.rc('grid', color='0.75', linestyle='-', linewidth=0.5)

    mynames=["default","my" + " " + str(pearson) + " " + str(spearman) + " " + str(kendalltau), "geom"]
    olddate = dates[0]
    newdate = dates[len(dates) - 1]
    
    textsize = 9
    left, width = 0.1, 0.8
    rect1 = [left, 0.5, width, 0.4]
    #rect2 = [left, 0.3, width, 0.2]
    #rect3 = [left, 0.1, width, 0.2]
    plt.ion()
    #print("TT" + str(type(mynames[0])))
    title = market + " " + str(mynames) + " " + str(olddate) + " - " + str(newdate)
    fig = plt.figure(facecolor='white')
    axescolor = '#f6f6f6'  # the axes background color

    ax1 = fig.add_axes(rect1, facecolor=axescolor)  # left, bottom, width, height
    print(type(commondays[0]))
    #print(commondays)
    commondays = [ w.replace('.', '-') for w in commondays ]
    commondays = [ np.datetime64(x) for x in commondays ]
    #print(type(commondays[0]))
    displayax(ax1, commonls, commondays, mynames, None, None, newdate, olddate, None, title, "Value")
    plt.show()
    for x in updatemap['stockhistory']:
        print(x)
    for x in updatemap['sumhistory']:
        print(x)
    for x in updatemap['tradestocks'][:10]:
        print(x)
    #print(webdata.keys())
    print(webdata['timingMap'])
    print(updatemap['startdate'])
    print(updatemap['enddate'])
    if intervalwhole:
      print(updatemap['scores'])
      print(updatemap['stats'])
      print(updatemap['minmax'])
    print(updatemap['lastbuysell'])
    mypost.post(updatemap['lastbuysell'])
    return

def improvesimulateinvest(market = None, startdate = None, enddate = None, ga = 0, adviser = None, indicatorpure = None, delay = 1, intervalwhole = True, stocks = None, indicatorreverse = None, interval = None, buyweight = None, volumelimits = None, filters = None, futurecount = 0, futuretime = 0, improvefilters = False):
    data = { 'startdate' : startdate, 'enddate' : enddate, 'ga' : ga, 'adviser' : adviser, 'indicatorPure' : indicatorpure, 'delay' : delay, 'intervalwhole' : intervalwhole, 'stocks' : stocks, 'indicatorReverse' : indicatorreverse, 'interval' : interval, 'buyweight' : buyweight, 'volumelimits' : volumelimits, 'filters' : filters, 'futurecount' : futurecount, 'futuretime' : futuretime, 'improveFilters' : improvefilters }
    from datetime import datetime
    tsstart = datetime.now().timestamp()
    response = request.request2(market, data)
    tsend = datetime.now().timestamp()
    time = tsend - tsstart
    resp = response.json()
    webdata = resp['webdatajson']
    updatemap = webdata['updateMap']
    #timingmap = webdata['timingMap']
    #key = list(timingmap.keys())[0]
    #timing = timingmap[key]
    #print(timing)
    #print(type(timing))
    #print(timing.keys)
    #print(updatemap.keys())
    #print(updatemap['scores'])
    #print(updatemap['stats'])
    #print(updatemap['minmax'])
    print("improve complete", market, startdate, enddate, time)
    #print(response.text)

def myprint4(arg):
    print("aa", arg)
    
def myprint3(queue, arg):
    import io
    from contextlib import redirect_stdout
    file = io.StringIO()
    with redirect_stdout(file):                                                
        myprint4(arg)
        print("more")
    output = file.getvalue()
    #print("after",output)
    queue.put(output)
    import tkinter as tk
    root = tk.Tk()
    txt = tk.Text(master = root, fg='green', bg='black')
    txt.pack(side=tk.RIGHT)
    txt.insert(tk.END, output)
    tk.mainloop()
    #bop = tk.Frame()
    #bop.pack(side=tk.LEFT)
    
    
def myprint2(arg):
    import multiprocessing as mp
    import io
    from contextlib import redirect_stdout
    import time
    p = mp.Pool(5)
    file = io.StringIO()
    #if True: 
    with redirect_stdout(file):
        p.map(myprint3, [ "aaa" ])
    output = file.getvalue()
    #print("after",output)
    
def myprint(arg):
    import multiprocessing as mp
    import io
    from contextlib import redirect_stdout
    import time
    queue = mp.Queue()
    if True: 
    #with redirect_stdout(file):
        print(type(arg), arg)
        p = mp.Process(target=myprint3, args=(queue, "z"))
        p.start()
    output = queue.get()
    print("after2",output)
    # output is a `str` whose 

def getvaluesGwrap(myid, start, end):
    import io
    from contextlib import redirect_stdout
    file = io.StringIO()
    with redirect_stdout(file):                                                
        getvalues(myid, start, end)
    output = file.getvalue()
    gui.view(output)

def getvaluesG(myid, start, end):
    import multiprocessing as mp
    mp.Process(target=getvaluesGwrap, args=(myid, start, end)).start()

def gettopgraphGwrap(market, start, end, numberdays, tablemoveintervaldays, topbottom, myperiodtexts, wantrise=False, wantmacd=False, wantrsi=False, sort=const.VALUE, macddays=180, reverse=False, deltadays=3, rebase=False, wantchart=True, interpolate=True, wantdays=False, days=1, wantgrid=False, interpolation = 'linear'):
    print("0", market)
    print(wantmacd, wantrsi, wantdays)
    import io
    from contextlib import redirect_stdout
    file = io.StringIO()
    with redirect_stdout(file):
        gettopgraph(market, start, end, numberdays, tablemoveintervaldays, topbottom, myperiodtexts, sort=sort, wantmacd=wantmacd, wantrise=wantrise, wantrsi=wantrsi, macddays=macddays, reverse=reverse, deltadays=deltadays, rebase=rebase, wantchart=wantchart, interpolate=interpolate, wantdays=wantdays, days=days, wantgrid=wantgrid, interpolation=interpolation)
    output = file.getvalue()
    gui.view(output)

def gettopgraphG(market, start, end, numberdays, tablemoveintervaldays, topbottom, myperiodtexts, wantrise=False, wantmacd=False, wantrsi=False, sort=const.VALUE, macddays=180, reverse=False, deltadays=3, percentize=False, wantchart=True, interpolate=True, wantdays=False, days=1, wantgrid=False, interpolation = 'linear'):
    print("0", market)
    print(wantmacd, wantrsi, wantdays)
    import multiprocessing as mp
    mp.Process(target = gettopgraphGwrap, args = (market, start, end, numberdays, tablemoveintervaldays, topbottom, myperiodtexts, wantrise, wantmacd, wantrsi, sort, macddays, reverse, deltadays, percentize, wantchart, interpolate, wantdays, days, wantgrid, interpolation)).start()

def simulateinvest2Gwrap(market, startdate, enddate, confidence, confidencevalue, confidencefindtimes, stoploss, stoplossvalue, indicatorpure, indicatorrebase, indicatorreverse, mldate, stocks, buyweight, interval, adviser, period, interpolate, intervalstoploss, intervalstoplossvalue, day, delay, intervalwhole, confidenceholdincrease, noconfidenceholdincrease, noconfidencetrenddecrease, noconfidencetrenddecreasetimes, confidencetrendincrease, confidencetrendincreasetimes, indicatordirection, indicatordirectionup, volumelimits, abovebelow):
    import io
    from contextlib import redirect_stdout
    file = io.StringIO()
    with redirect_stdout(file):
        simulateinvest2(market, startdate, enddate, confidence, confidencevalue, confidencefindtimes, stoploss, stoplossvalue, indicatorpure, indicatorrebase, indicatorreverse, mldate, stocks, buyweight, interval, adviser, period, interpolate, intervalstoploss, intervalstoplossvalue, day, delay, intervalwhole, confidenceholdincrease, noconfidenceholdincrease, noconfidencetrenddecrease, noconfidencetrenddecreasetimes, confidencetrendincrease, confidencetrendincreasetimes, indicatordirection, indicatordirectionup, volumelimits, abovebelow)
    output = file.getvalue()
    myfile = open("/tmp/" + str(time.time()) + ".txt", "w")
    myfile.write(output)
    myfile.close()
    gui.view(output)

def simulateinvest2G(market, startdate = None, enddate = None, confidence = False, confidencevalue = 0.7, confidencefindtimes = 4, stoploss = True, stoplossvalue = 0.9, indicatorpure = False, indicatorrebase = False, indicatorreverse = False, mldate = False, stocks = 5, buyweight = False, interval = 7, adviser = 0, period = 0, interpolate = False, intervalstoploss = True, intervalstoplossvalue = 0.9, day = 1, delay = 1, intervalwhole = False, confidenceholdincrease = False, noconfidenceholdincrease = True, noconfidencetrenddecrease = False, noconfidencetrenddecreasetimes = 1, confidencetrendincrease = False, confidencetrendincreasetimes = 1, indicatordirection = False, indicatordirectionup = True, volumelimits = None, abovebelow = False):
    mp.Process(target=simulateinvest2Gwrap, args=(market, startdate, enddate, confidence, confidencevalue, confidencefindtimes, stoploss, stoplossvalue, indicatorpure, indicatorrebase, indicatorreverse, mldate, stocks, buyweight, interval, adviser, period, interpolate, intervalstoploss, intervalstoplossvalue, day, delay, intervalwhole, confidenceholdincrease, noconfidenceholdincrease, noconfidencetrenddecrease, noconfidencetrenddecreasetimes, confidencetrendincrease, confidencetrendincreasetimes, indicatordirection, indicatordirectionup, volumelimits, abovebelow)).start()

f = False
t = True

def simulateinvests(market, startdate = None, enddate = None, c = f, cv = 0.7, ct = 4, st = t, stv = 0.9, ip = t, ib = f, ir = f, m = f, s = 5, b = t, i = 7, a = 0, p = 0, f = t, ist = t, istv = 0.9, d = 1, w = 1, iw = f, ch = f, nch = t, nctd = f, nctdt = 1, cti = f, ctit = 1, id = f, idu = t, vl = None, ab = f):
    simulateinvest2Gwrap(market, startdate, enddate, c, cv, ct, st, stv, ip, ib, ir, m, s, b, i, a, p, f, ist, istv, d, w, iw, ch, nch, nctd, nctdt, cti, ctit, id, idu, vl, ab)

def simulateinvestsG(market, startdate = None, enddate = None, c = f, cv = 0.7, ct = 4, st = t, stv = 0.9, ip = t, ib = f, ir = f, m = f, s = 5, b = t, i = 7, a = 0, p = 0, f = t, ist = t, istv = 0.9, d = 1, w = 1, iw = f, ch = f, nch = t, nctd = f, nctdt = 1, cti = f, ctit = 1, id = f, idu = t, vl = None, ab = f):
    mp.Process(target=simulateinvest2Gwrap, args=(market, startdate, enddate, c, cv, ct, st, stv, ip, ib, ir, m, s, b, i, a, p, f, ist, istv, d, w, iw, ch, nch, nctd, nctdt, cti, ctit, id, idu, vl, ab)).start()

def improvesimulateinvestGwrap(market, startdate, enddate, ga, adviser, indicatorpure, delay, intervalwhole, stocks, volumelimits, filters, futurecount, futuretime, improvefilters):
    import io
    from contextlib import redirect_stdout
    file = io.StringIO()
    with redirect_stdout(file):                                                
        improvesimulateinvest(market, startdate, enddate, ga, adviser, indicatorpure, delay, intervalwhole, stocks, volumelimits, filters, futurecount, futuretime, improvefilters)
    output = file.getvalue()
    gui.view(output)

def improvesimulateinvestG(market, startdate = None, enddate = None, ga = 0, adviser = None, indicatorPure = None, delay = 1, intervalwhole = True, stocks = None, volumelimits = None, filters = None, futurecount = 0, futuretime = 0, improvefilters = False):
    mp.Process(target=improvesimulateinvestGwrap, args=(market, startdate, enddate, ga, adviser, indicatorpure, delay, intervalwhole, stocks, volumelimits, filters, futurecount, futuretime, improvefilters)).start()

engine = create_engine('postgresql://stockread:password@localhost:5432/stockstat')
session = Session(bind=engine)
#conn = psycopg2.connect("host=localhost dbname=stockstat user=stockread password=password")

LUCKY=[ { 'lucky' : 0.0, 'stable' : 0.0, 'correlation' : 0.0 } ] * 10
ZERO=[ { 'lucky' : 0.0, 'stable' : 0.0 } ] * 10
CORR07=[ { 'lucky' : 0.0, 'stable' : 0.0, 'correlation' : 0.7 } ] * 10
CORR08=[ { 'lucky' : 0.0, 'stable' : 0.0, 'correlation' : 0.8 } ] * 10

def autosimulateinvests(market, startdate = None, enddate = None, i = 1, p = 0, l = 5, d = 0.5, s = 1.0, a = 0, iw = False, f = None, vl = None, v = False, ka = False, kal = 0.0):
    autosimulateinvest2Gwrap(market, startdate, enddate, i, p, l, d, s, a, iw, f, vl, v, ka, kal)

def autosimulateinvestsG(market, startdate = None, enddate = None, i = 1, p = 0, l = 5, d = 0.5, s = 1.0, a = 0, iw = False, f = None, vl = None, v = False, ka = False, kal = 0.0):
    mp.Process(target=autosimulateinvest2Gwrap, args=(market, startdate, enddate, i, p, l, d, s, a, iw, f, vl, v, ka, kal)).start()

def autosimulateinvest2Gwrap(market, startdate, enddate, interval = 1, period = 0, lastcount = 5, dellimit = 0.5, scorelimit = 1.0, autoscorelimit = 0.0, intervalwhole = False, filters = None, volumelimits = None, vote = False, keepadviser = False, keepadviserlimit = 0.0):
    import io
    from contextlib import redirect_stdout
    file = io.StringIO()
    with redirect_stdout(file):
        autosimulateinvest2(market, startdate, enddate, interval, period, lastcount, dellimit, scorelimit, autoscorelimit, intervalwhole, filters, volumelimits, vote, keepadviser, keepadviserlimit)
    output = file.getvalue()
    myfile = open("/tmp/" + str(time.time()) + ".txt", "w")
    myfile.write(output)
    myfile.close()
    gui.view(output)

def autosimulateinvest2(market, startdate = None, enddate = None, interval = 1, period = 0, lastcount = 5, dellimit = 0.5, scorelimit = 1.0, autoscorelimit = 0.0, intervalwhole = False, filters = None, volumelimits = None, vote = False, keepadviser = False, keepadviserlimit = 0.0):
    autosimulateinvest(market, startdate, enddate, interval, period, lastcount, dellimit, scorelimit, autoscorelimit, intervalwhole, filters, volumelimits, vote, keepadviser, keepadviserlimit)
    
def autosimulateinvest(market, startdate = None, enddate = None, interval = 1, period = 0, lastcount = 5, dellimit = 0.5, scorelimit = 1.0, autoscorelimit = 0.0, intervalwhole = False, filters = None, volumelimits = None, vote = False, keepadviser = False, keepadviserlimit = 0.0):
    data = { 'startdate' : startdate, 'enddate' : enddate, 'interval' : interval, 'intervalwhole' : intervalwhole, 'period' : period, 'lastcount' : lastcount, 'dellimit' : dellimit, 'scorelimit' : scorelimit, 'autoscorelimit' : autoscorelimit, 'volumelimits' : volumelimits, 'filters' : filters, 'vote' : vote, 'keepAdviser' : keepadviser, 'keepAdviserLimit' : keepadviserlimit }
    print(market, data)
    #if True:
    #    return
    response = request.request3(market, data)
    resp = response.json()
    webdata = resp['webdatajson']
    updatemap = webdata['updateMap']
    if 'empty' in updatemap:
        return
    if 'automax' in updatemap:
        print(updatemap['automax'])
    dates = updatemap['plotdates']
    commondays = dates
    default = updatemap['plotdefault']
    capital = updatemap['plotcapital']
    if len(capital) == 0:
        return
    geom = np.geomspace(capital[0], capital[-1],num=len(capital),endpoint=True, dtype=None, axis=0)
    if len(capital) >= 2:
        pearson = scipy.stats.pearsonr(capital, geom)
        spearman = scipy.stats.spearmanr(capital, geom)
        kendalltau = scipy.stats.kendalltau(capital, geom)
        pearson = round(pearson[0], 2)
        spearman = round(spearman[0], 2)
        kendalltau = round(kendalltau[0], 2)
    else:
        pearson = 0
        spearman = 0
        kendalltau = 0
    commonls = [ default, capital, geom ]
    mynames = [ "default", "capital", "geom" ]
    plt.rc('axes', grid=True)
    plt.rc('grid', color='0.75', linestyle='-', linewidth=0.5)

    mynames=["default","my", "geom"]
    mynames=["default","my" + " " + str(pearson) + " " + str(spearman) + " " + str(kendalltau), "geom"]
    olddate = None
    newdate = None
    if len(dates) > 0:
        olddate = dates[0]
        newdate = dates[len(dates) - 1]
    
    textsize = 9
    left, width = 0.1, 0.8
    rect1 = [left, 0.5, width, 0.4]
    #rect2 = [left, 0.3, width, 0.2]
    #rect3 = [left, 0.1, width, 0.2]
    plt.ion()
    #print("TT" + str(type(mynames[0])))
    title = market + " " + str(mynames) + " " + str(olddate) + " - " + str(newdate)
    fig = plt.figure(facecolor='white')
    axescolor = '#f6f6f6'  # the axes background color

    ax1 = fig.add_axes(rect1, facecolor=axescolor)  # left, bottom, width, height
    #print(type(commondays[0]))
    #print(commondays)
    commondays = [ w.replace('.', '-') for w in commondays ]
    commondays = [ np.datetime64(x) for x in commondays ]
    #print(type(commondays[0]))
    displayax(ax1, commonls, commondays, mynames, None, None, newdate, olddate, None, title, "Value")
    plt.show()
    for x in updatemap['stockhistory']:
        print(x)
    for x in updatemap['sumhistory']:
        print(x)
    for x in updatemap['tradestocks'][:10]:
        print(x)
    #print(webdata.keys())
    print(webdata['timingMap'])
    print(updatemap['startdate'])
    print(updatemap['enddate'])
    if intervalwhole:
      print(updatemap['scores'])
      print(updatemap['stats'])
      print(updatemap['minmax'])
    if 'advisers' in updatemap:
        print(updatemap['advisers'])
    print(updatemap['lastbuysell'])
    mypost.post(updatemap['lastbuysell'])
    return

def improveautosimulateinvest(market = None, startdate = None, enddate = None, ga = 0, stocks = 5, intervalwhole = False, filters = None, volumelimits = None, futurecount = 0, futuretime = 0, improvefilters = False, vote = False):
    data = { 'startdate' : startdate, 'enddate' : enddate, 'ga' : ga, 'stocks' : stocks, 'intervalwhole' : intervalwhole, 'volumelimits' : volumelimits, 'filters' : filters, 'futurecount' : futurecount, 'futuretime' : futuretime, 'improveFilters' : improvefilters, 'vote' : vote }
    from datetime import datetime
    tsstart = datetime.now().timestamp()
    response = request.request4(market, data)
    tsend = datetime.now().timestamp()
    time = tsend - tsstart
    resp = response.json()
    webdata = resp['webdatajson']
    updatemap = webdata['updateMap']
    #timingmap = webdata['timingMap']
    #key = list(timingmap.keys())[0]
    #timing = timingmap[key]
    #print(timing)
    #print(type(timing))
    #print(timing.keys)
    #print(updatemap.keys())
    #print(updatemap['scores'])
    #print(updatemap['stats'])
    #print(updatemap['minmax'])
    print("improve complete", market, startdate, enddate, time)
    #print(response.text)

def improveautosimulateinvestGwrap(market, startdate, enddate, ga, stocks, intervalwhole, filters, volumelimits, futurecount, futuretime, improvefilters, vote):
    import io
    from contextlib import redirect_stdout
    file = io.StringIO()
    with redirect_stdout(file):                                                
        improveautosimulateinvest(market, startdate, enddate, ga, stocks, intervalwhole, filters, volumelimits, futurecount, futuretime, improvefilters, vote)
    output = file.getvalue()
    gui.view(output)

def improveautosimulateinvestG(market, startdate = None, enddate = None, ga = 0, intervalwhole = False, futurecount = 0, futuretime = 0, improvefilters = False, vote = False):
    mp.Process(target=improveautosimulateinvestGwrap, args=(market, startdate, enddate, ga, stocks, intervalwhole, filters, volumelimits, futurecount, futuretime, improvefilters, vote)).start()

def eventpause():
    request.requestpause()
    
def eventcontinue():
    request.requestcontinue()
    
def gettasks():
    response = request.requestgettasks()
    resp = response.json()
    print("Tasks", len(resp));
    for e in resp:
        print(e)
    
def dbupdatestart():
    request.dbupdatestart()
    
def dbupdateend():
    request.dbupdateend()
    
def cacheinvalidate():
    request.cacheinvalidate()
    
def copydb(indb, outdb):
    request.copydb(indb, outdb)
    
if not 'allstocks' in globals():
    print("Loadings stocks")
    allstocks = getstocks(session)
    print("Stocks loaded");
    if filterweekend:
        allstocks = etl.filterweekend(allstocks)
    allmetas = getmetas(session)

plt.close('all')

today = datetime.today().strftime('%Y-%m-%d')
yesterday = datetime.strftime(datetime.now() - timedelta(1), '%Y-%m-%d')

#print(len(stock))
#print(meta)
#print(type(meta))
