#!/usr/bin/python3

import pandas as pd
import tensorflow as tf
import numpy as np
import psycopg2

#from sqlalchemy import create_engine

pricetype = -1
indextype = -2
metaperiods = 6
periods = 8

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
    periodtext = [ "Period1", "Period2", "Period3", "Period4", "Period5", "Period6" ]
    mymeta = getmarketmeta(allmetas, market)
    print(len(mymeta))
    if (len(mymeta > 0)):
        for i in range(1, metaperiods):
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
    if period == 1:
        return meta.period1.iloc[0]
    if period == 2:
        return meta.period2.iloc[0]
    if period == 3:
        return meta.period3.iloc[0]
    if period == 4:
        return meta.period4.iloc[0]
    if period == 5:
        return meta.period5.iloc[0]
    if period == 6:
        return meta.period6.iloc[0]
    return None

def split(df, group):
    gb = df.groupby(group)
    return [gb.get_group(x) for x in gb.groups]
          
def getdatedstocklists(listdate, listdates, mydate, days, tableintervaldays):
    offset = 0
    if offset == 0:
        offset = round(mydate)
        mydate = None
    datedstocklists = []
    if mydate is not None:
        dateindex = listdates.index(mydate)
    else:
        dateindex = len(listdate)
    index = dateindex - offset
    print("Index %d" %(index))
                                        #index = length(listdate)
    c = 0
    c = c + 1
    datedstocklists.append(listdate[index])
    print(type(listdate[index]))
    for j in range(1, days):
        index = index - tableintervaldays
        c = c + 1
        datedstocklists.append(listdate[index])
    return datedstocklists

def getdforderperiod(df, period):
    ds = df
    if period == 1:
        ds = df.sort_values(by='period1', ascending = 0)
    if period == 2:
        ds = df.sort_values(by='period2', ascending = 0)
    if period == 3:
        ds = df.sort_values(by='period3', ascending = 0)
    if period == 4:
        ds = df.sort_values(by='period4', ascending = 0)
    if period == 5:
        ds = df.sort_values(by='period5', ascending = 0)
    if period == 6:
        ds = df.sort_values(by='period6', ascending = 0)
    if period == 7:
        ds = df.sort_values(by='price', ascending = 0)
    if period == 8:
        ds = df.sort_values(by='indexvalue', ascending = 0)
    return ds

def getdforderperiodreverse(df, period):
    ds = df
    if period == 1:
        ds = df.sort_values(by='period1', ascending = 0)
    if period == 2:
        ds = df.sort_values(by='period2', ascending = 0)
    if period == 3:
        ds = df.sort_values(by='period3', ascending = 0)
    if period == 4:
        ds = df.sort_values(by='period4', ascending = 0)
    if period == 5:
        ds = df.sort_values(by='period5', ascending = 0)
    if period == 6:
        ds = df.sort_values(by='period6', ascending = 0)
    if period == 7:
        ds = df.sort_values(by='price', ascending = 0)
    if period == 8:
        ds = df.sort_values(by='indexvalue', ascending = 0)
    return ds

def getonedfperiod(df, period):
    if period == 1:
        return df.period1
    if period == 2:
        return df.period2
    if period == 3:
        return df.period3
    if period == 4:
        return df.period4
    if period == 5:
        return df.period5
    if period == 6:
        return df.period6
    if period == 7:
        return df.price
    if period == 8:
        return df.indexvalue
    #cat("should not be here")
    return None

def getlistsorted(datedstocklists, listid, listdate, count, tableintervaldays, wantrise = True, reverse = False):
    stocklistperiod = [[0 for x in range(count)] for y in range(periods)]
                  #matrix([], nrow = periods, ncol = count)
    print(stocklistperiod)
    print("count %d %d" % (count, periods))
    for j in range(1, count):
        for i in range(1, periods):
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
                #print(stocklistperiod.shape)
                #print("ij")
                #print(i)
                #print(j)
                stocklistperiod[i][j] = tmp
            else:
                print("no")
                                        #cat("no period day ", j, " period ", i)
    return stocklistperiod

def listperiod(list, period, index):
    #print(type(list))
    #print(len(list))
    if period == 1:
        return list.period1.iloc[index]
    if period == 2:
        return list.period2.iloc[index]
    if period == 3:
        return list.period3.iloc[index]
    if period == 4:
        return list.period4.iloc[index]
    if period == 5:
        return list.period5.iloc[index]
    if period == 6:
        return list.period6.iloc[index]
    if period == 7:
        return list.price.iloc[index]
    if period == 8:
        return list.indexvalue.iloc[index]
    return None
                                        # out of use
def myperiodtextslist(myperiodtexts, periodtexts):
    retlist = myperiodtexts
    if myperiodtexts is None:
        retlist = periodtexts
#    if (!is.list(myperiodtexts)) {
#        retlist <- list(myperiodtexts)
#    }
    return retlist

def getvalues(market, id, mydate, days, myperiodtexts):
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
                                        #    cat("perind ", period)
        stocks = getstockmarket(allstocks, market)

        stocks = stocks.sort_values('date', ascending=[0])
        listdate = split(stocks, stocks.date)
        listdates = stocks.date.unique()
        listdates.sort()
        listid = split(stocks, stocks.id)
        datedstocklists = getdatedstocklists(listdate, listdates, mydate, days, tablemoveintervaldays)
        stocklistperiod = getlistsorted(datedstocklists, listid, listdate, days, tablemoveintervaldays, reverse=False)
        dflist = []
        print("here")
        for j in range(1, days):
            df = stocklistperiod[period][j]
            df = df.loc[(df.id == id)]
            if len(df) == 1:
                name = df.name.iloc[0]
                list11 = df
                #print(df.name.iloc[0])
                print("%3d %-35s %12s % 6.2f %s" % (i, name[:33], df.date.iloc[0], listperiod(df, period, i), df.id.iloc[0]))
    
#engine = create_engine('postgresql://stockread@localhost:5432/stockstat')
conn = psycopg2.connect("dbname=stockstat user=stockread password=password")

allstocks = getstocks(conn)
allmetas = getmetas(conn)

#print(len(stock))
#print(meta)
#print(type(meta))
