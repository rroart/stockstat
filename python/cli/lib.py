#!/usr/bin/python3

#exec(open("./lib.py").read())

import pandas as pd
import tensorflow as tf
import numpy as np
import psycopg2
import talib as ta
import matplotlib.pyplot as plt

#from sqlalchemy import create_engine

doprint = False

nafix = 0

pricetype = -1
indextype = -2
metaperiods = 6
periods = 8
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
    periodtext = [ "Period1", "Period2", "Period3", "Period4", "Period5", "Period6" ]
    mymeta = getmarketmeta(allmetas, market)
    print(len(mymeta))
    if len(mymeta > 0):
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
    return None

def split(df, group):
    gb = df.groupby(group)
    return [gb.get_group(x) for x in gb.groups]
          
def getdatedstocklists(listdate, listdates, mydate, days, tableintervaldays):
    print("mydate ", mydate)
    offset = 0
    if isinstance(mydate, float):
        offset = round(mydate)
        mydate = None
    if isinstance(mydate, int):
        offset = mydate
        mydate = None
    datedstocklists = []
    if mydate is not None:
        #print("listdates ", type(listdates), listdates)
        #dateindex = listdates.index(mydate)
        dateindex = np.where(listdates == mydate)
        print("h0")
    else:
        dateindex = len(listdate) - 1
        print("h1")
    index = dateindex - offset
    # -1 ok?
    print("Index %d" %(index))
                                        #index = len(listdate)
    c = 0
    c = c + 1
    #print(len(listdate))
    #print(len(datedstocklists))
    datedstocklists.append(listdate[index])
    #print(type(listdate[index]))
    print("days0 ", days)
    for j in range(0, days):
        index = index - tableintervaldays
        c = c + 1
        datedstocklists.append(listdate[index])
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
        ds = df.sort_values(by='price', ascending = 0)
    if period == 7:
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
        ds = df.sort_values(by='price', ascending = 0)
    if period == 7:
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
        return df.price
    if period == 7:
        return df.indexvalue
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
        return list.price.iloc[index]
    if period == 7:
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
        stocks = getstockmarket(allstocks, market)

        stocks = stocks.sort_values('date', ascending=[0])
        listdate = split(stocks, stocks.date)
        #print(type(listdate))
        listdates = stocks.date.unique()
        listdates.sort()
        listid = split(stocks, stocks.id)
        datedstocklists = getdatedstocklists(listdate, listdates, mydate, days, tablemoveintervaldays)
        stocklistperiod = getlistsorted(datedstocklists, listid, listdate, days, tablemoveintervaldays, reverse=False)
        dflist = []
        print("here")
        for j in range(0, days):
            df = stocklistperiod[period][j]
            df = df.loc[(df.id == id)]
            if len(df) == 1:
                name = df.name.iloc[0]
                list11 = df
                #print(df.name.iloc[0])
                print("%3d %-35s %12s % 6.2f %s" % (i, name[:33], df.date.iloc[0], listperiod(df, period, i), df.id.iloc[0]))

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


def getbottomgraph(market, mydate, days, tablemoveintervaldays, topbottom, myperiodtexts, wantrise=False, wantmacd=False, wantrsi=False, sort=VALUE, macddays=180, deltadays=3, percentize=True):
    return(gettopgraph(market, mydate, days, tablemoveintervaldays, topbottom, myperiodtexts, sort, wantmacd=wantmacd, wantrise=wantrise, wantrsi=wantrsi, macddays=macddays, reverse=True, deltadays=deltadays, percentize=percentize))

def getonedfvalue(df, atype):
    if atype > 0:
        return(getonedfperiod(df, atype))
    if atype < 0:
        return(getonedfspecial(df, atype))
    print("should not be here")

def gettopgraph(market, mydate, days, tablemoveintervaldays, topbottom, myperiodtexts, sort=VALUE, macddays=180, reverse=False, wantrise=False, wantmacd=False, wantrsi=False, deltadays=3, percentize=True):
    print("0", market)
    periodtexts = getperiodtexts(market)
    myperiodtexts = myperiodtextslist(myperiodtexts, periodtexts)
    print ("00 " , len(myperiodtexts), " ", days)
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
        datedstocklists = getdatedstocklists(listdate, listdates, mydate, days, tablemoveintervaldays)
        stocklistperiod = getlistsorted(datedstocklists, listid, listdate, days, tablemoveintervaldays, reverse=reverse)
        periodmaps = None
        if wantrise:
            periodmaps = getlistmove(datedstocklists, listid, listdate, days, tablemoveintervaldays, stocklistperiod)
        
        dflist = []
        headskiprsi = 0
        if mydate is None:
            headskiprsi = mydate
        headskipmacd = 0
        if mydate is None:
            headskipmacd = mydate
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
                    rsi = getrsi(myc)
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

def getmomhist(myma, deltadays):
    #print(type(myma))
    #print(myma.values)
    lses = getmylses(myma)
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

def fixna(v):
    #print(type(v))
    if nafix == 1:
        return(v.dropna())
    else:
        return (v.interpolate(method='linear'))

def getmylses(myma):
                                        #    print(myma)
                                        #    print("bla\n")
    myma = fixna(myma)
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
    myma = fixna(myma)
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

def getcontentgraph(mydate, days, tableintervaldays, ids, periodtext, wantmacd=False, wantrsi=False, interpolate = True):
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
        print("days ", days, " " , tableintervaldays)
        datedstocklists = getdatedstocklists(listdate, listdates, mydate, days, tableintervaldays)
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
                        l = bigretl[0]
                        print("gaga")
                        #print(l)
                        print(type(l))
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
                        ls.append(l)
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

    textsize = 9
    left, width = 0.1, 0.8
    rect1 = [left, 0.5, width, 0.4]
    rect2 = [left, 0.3, width, 0.2]
    rect3 = [left, 0.1, width, 0.2]
    plt.ion()
    title = mynames[0].values + " " + str(olddate) + " - " + str(newdate)
    fig = plt.figure(facecolor='white')
    axescolor = '#f6f6f6'  # the axes background color

    ax1 = fig.add_axes(rect1, facecolor=axescolor)  # left, bottom, width, height
    ax2 = fig.add_axes(rect2, facecolor=axescolor, sharex=ax1)
    #ax2t = ax2.twinx()
    ax3 = fig.add_axes(rect3, facecolor=axescolor, sharex=ax1)

    print("ll ", len(ls))
    #print("mynames", type(mynames), len(mynames), mynames, " ", type(mynames[0]), len(mynames[0]))
    print("daes", olddate, newdate)
    displayax(ax1, ls, daynames2, mynames[0].values, 5, periodtext, newdate, olddate, days, title, periodtext)
    if wantmacd:
        for i in range(len(ls)):
            myma = ls[i]
            #print("tmyma ", type(myma))
            #print(myma)
    percentize = True      
    if percentize:
      if periodtext == "Price" or periodtext == "Index":
        first = myma[0]
        print("t1 ", type(myma))
        myma = np.asarray(myma) * (100 / first)
        myma = pd.Series(data = myma)
        print("t2 ", type(myma))
    print("tmyma3 ", type(myma))
    print(myma)
    if wantmacd:
        lses = getmylses(myma)
        days2 = len(lses[1])
        olddate2 = daynames[days - days2]
        print("")
        print(lses[0])
        print("")
        print(lses[1])
        print("")
        print(lses[2]) 
        print("")
        print("ll ", len(lses))
        mynames2 = ["macd", "signal", "diff"]
        text = ''.join(mynames2)
        displayax(ax2, lses, daynames2, mynames2, 3, text, newdate, olddate2, days2, "MACD")
                                        #    displaymacd(lses, mynames[1], 1, periodtext, maindate, olddate, days)
    if wantrsi:
        rsis = []
        #for i in range(len(ls)):
        myma = ls[i]
            #print("ere")
            #print(myma)
                                        #    myma = fixna(myma)
                                        #    print(tail(myma, n=10L))
                                        #    print("\nsize ", len(myma), "\n")
                                        #                print(myma)
        rsi = getmyrsi(myma)
            #print(rsi)
            #print(unlist(rsi))
        rsis.append(rsi)
#    days2 = len(ls[1]])
#    olddate2 = daynames[days - days2]
                                        #    print(" tata ")
                                        #    print(len(lses[1]]))
                                        #    print(" tata ")
                                        #    print(days2)
#    posneg = getposneg(ls)
#    ls1 = lses[1]]
#    ls2 = lses[2]]
#    print("\ndays ", days2, " lastpos ", posneg[1]], " lastneg ", posneg[2]], " macd ", ls1[days2]], " signal ", ls2[days2]], "\n")
#    mynames2 = list("macd", "signal", "diff")
        mynames = [ "rsi" ]
        displayax(ax3, rsis, daynames2, mynames, 5, periodtext, newdate, olddate, days, "RSI")
#    displaychart(lses, mynames2, 3, periodtext, newdate, olddate2, days2)
    plt.show()
                                        #    displaymacd(lses, mynames[1], 1, periodtext, maindate, olddate, days)

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
    mytitle = "mynames[0]" + " " + str(olddate) + " - " + str(maindate)
    ax.set(title = title, ylabel = ylabel)
    #ax2.legend(loc = 'upper right')
    #ax2.grid(False)
    for i in range(topbottom):
        print("intc ", i, mynames[i])
        if i == 0:
                                        #print(l$id[[1]])
                                        #print(l$name[[2]])
            c = ls
            #ax.plot(range(len(c[i])), c[i], label = mynames[i])
            print(c[0])
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
            c = ls
            #print(c)
            ax.plot(daynames, c[i], label = mynames[i])
            #ax.plot(range(len(c[i])), c[i], label = mynames[i])
            #lines(c, type="o", lty = i, col = colours[i], pch = i)

        #title(main=sprintf("Period %s", periodtext))
        #title(xlab=sprintf("Time %s - %s", olddate, maindate))
        #title(ylab="Value")
        #n = mynames[1]
        ####legend(1, g_range[2], mynames, cex=0.8, lty=1:6, pch=1:25, col=colours)
    ax.legend()

def getelem3(id, days, datedstocklist, period, size):
    dayset = []
    dayset2 = []
    retl = [ None for x in range(days) ]
    c = 0
    for i in reversed(range(days)):
        retl[c] = np.NaN
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
            retl[c] = getonedfvalue(el, period).values[0]
            #print(type(retl[c].values[0]))
            str2 = str(el.date.values[0])
            #print(type(el.date.values[0]))
            #print("lenel", len(el.date), type(el.date), el.date.values[0])
            #print("str2", type(str2))
            dayset.append(str2)
            dayset2.append(el.date.values[0])
        else:
            prev = dayset[len(dayset) - 1]
            prev2 = dayset2[len(dayset2) - 1]
            dayset.append(prev)
            dayset2.append(prev2)
            print("err2")
        c = c + 1
        retls = pd.Series(data = retl)
        print("dayset ", type(dayset), type(dayset[0]))
    return([retls, dayset,dayset2])

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

#engine = create_engine('postgresql://stockread@localhost:5432/stockstat')
conn = psycopg2.connect("host=localhost dbname=stockstat user=stockread password=password")

allstocks = getstocks(conn)
if filterweekend:
    allstocks = allstocks.loc[(allstocks.date.dt.weekday < 5)]
allmetas = getmetas(conn)

plt.close('all')

#print(len(stock))
#print(meta)
#print(type(meta))
