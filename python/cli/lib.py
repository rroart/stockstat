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
    # -1 ok?
    print("Index %d" %(index))
                                        #index = len(listdate)
    c = 0
    c = c + 1
    print(len(listdate))
    print(len(datedstocklists))
    datedstocklists.append(listdate[index])
    print(type(listdate[index]))
    for j in range(0, days):
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

def getlistmove(datedstocklists, listid, listdate, count, tableintervaldays, stocklistperiod):
    periodmaps = matrix([], nrow = periods, ncol = (count - 1))
    for j in range(0, count):
        for i in range(0, periods):
            hasperiod = stocklistperiod[i][j] is not None
            if hasperiod:
                if j > 1:
                    df1 = stocklistperiod[i, j - 1]
                    df2 = stocklistperiod[i, j]
                    tmplist = getperiodmap(df1, df2)
                    periodmaps[i, j - 1] = list(tmplist)
                
             #else:
                                        #cat("no period day ", j, " period ", i)
            
        
    
    return(periodmaps)


def getlistsorted(datedstocklists, listid, listdate, count, tableintervaldays, wantrise = True, reverse = False):
    stocklistperiod = [[0 for x in range(count)] for y in range(periods)]
                  #matrix([], nrow = periods, ncol = count)
    print(stocklistperiod)
    print("count %d %d" % (count, periods))
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
                #print(stocklistperiod.shape)
                #print("ij")
                #print(i)
                #print(j)
                stocklistperiod[i][j] = tmp
            else:
                print("no")
                                        #cat("no period day ", j, " period ", i)
    return stocklistperiod

def getperiodmap(list1, list2):
    list = []
    df1 = data.frame(list1[1])
    df2 = data.frame(list2[1])
    list1 = df1.id
    list2 = df2.id
    for j in range(0, len(list2)):
        id = list2[j]
        list[id] = NA
        i = list1.periodtext(id)
#        if not is.na(i):
#            list[id] = j - i
        
    
    return (list)


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
    for j in range(0, days):
        df = dflist[[j]]
        if max < len(df):
            max = len(df)
        
        for i in range(0, max):
            rsi = NA
            if wantrsi:
                rsi = df.rsic.iloc[i]
            
            macd = NA
            hist = NA
            macdd = NA
            histd = NA
            if wantmacd:
                macd = df.momc.iloc[i]
                hist = df.histc.iloc[i]
                macdd = df.momdc.iloc[i]
                histd = df.histdc.iloc[i]
            
            rise = NA
            if wantrise:
                rise = df.risec.iloc[i]
            
            name = df.name.iloc[i]
	    #Encoding(name) = "UTF-8"
            print(sprintf("%3d %-35s %12s % 6.2f %3d % 3.2f % 3.2f % 3.2f % 3.2f %3.2f %s", i, strtrim(name,33), df.date.iloc[i], listperiod(df, period, i), rise, hist, histd, macd, macdd, rsi, df.id.iloc[i]))
        
                                        #        str(df$id[[1]])
    


def myperiodtextslist(myperiodtexts, periodtexts):
    retlist = myperiodtexts
    if myperiodtexts is None:
        retlist = periodtexts
    
    if type(myperiodtexts) is not list:
       retlist = [myperiodtexts]
    
    return(retlist)


def getbottomgraph(market, mydate, days, tablemoveintervaldays, topbottom, myperiodtexts, wantrise=False, wantmacd=False, wantrsi=False, sort=VALUE, macddays=180, deltadays=3, percentize=True):
    return(gettopgraph(market, mydate, days, tablemoveintervaldays, topbottom, myperiodtexts, sort, wantmacd=wantmacd, wantrise=wantrise, wantrsi=wantrsi, macddays=macddays, reverse=True, deltadays=deltadays, percentize=percentize))


def gettopgraph(market, mydate, days, tablemoveintervaldays, topbottom, myperiodtexts, sort=VALUE, macddays=180, reverse=False, wantrise=False, wantmacd=False, wantrsi=False, deltadays=3, percentize=True):
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
        listdates = stocks.date.unique()
        listdates.sort()
        listid = split(stocks, stocks.id)
        datedstocklists = getdatedstocklists(listdate, listdates, mydate, days, tablemoveintervaldays)
        stocklistperiod = getlistsorted(datedstocklists, listid, listdate, days, tablemoveintervaldays, reverse=reverse)
        if wantrise:
            periodmaps = getlistmove(datedstocklists, listid, listdate, days, tablemoveintervaldays, stocklistperiod)
        
        dflist = []
        headskiprsi = 0
        if mydate is None:
            headskiprsi = mydate
        headskipmacd = 0
        if mydate is None:
            headskipmacd = mydate
        for j in range(0, days):
            df = stocklistperiod[period, j][[1]]
            if wantrise:
                list2 = []
                if j < days:
                    list2 = periodmaps[period, j][[1]]
                riselist = []
                for i in range(0, len(df)):
                    id = df.id.iloc[i]
                    rise = 0
                    if j < days:
                        rise = list2[[id]]
                        if rise is None:
                            rise = 0
                    riselist[i] = rise
                risec = c(unlist(riselist))
                df = cbind(df, risec)
            idc = df.id
            namec = df.name
            datec = df.date
            periodc = getonedfperiod(df, period)
            if wantmacd:
                momlist = []
                histlist = []
                momdlist = []
                histdlist = []
                for i in range(0, len(df)):
                    mydf = df[i,]
                    el = listid[mydf.id]
                    el = el[order(el.date),]
                    myc = c(getonedfvalue(el, period))
                    myclen = len(myc)
                    myc = head(myc, n=(myclen-headskipmacd))
                    myc = tail(myc, n=macddays)
                                        #str(myc)
                    if percentize:
                        if periodtext == "Price" or periodtext == "Index":
                            first = myc[1]
                            myc = myc * (100 / first)
                    momhist = getmomhist(myc, deltadays)
                                        #str(mom)
                    momlist[i] = momhist[1]
                    histlist[i] = momhist[2]
                    momdlist[i] = momhist[3]
                    histdlist[i] = momhist[4]
                headskipmacd = headskipmacd + tablemoveintervaldays
                momc = c(unlist(momlist))
                histc = c(unlist(histlist))
                momdc = c(unlist(momdlist))
                histdc = c(unlist(histdlist))
                df = cbind(df, momc)
                df = cbind(df, histc)
                df = cbind(df, momdc)
                df = cbind(df, histdc)
                if sort == MACD:
                    if reverse:
                        df = df[order(df.histc),]
                    else:
                        df = df[order(-df.histc),]
            if wantrsi:
                rsilist = []
                headskip = 0
                for i in range(0, len(df)):
                    mydf = df[i,]
                    el = listid[[mydf.id]]
                    el = el[order(el.date),]
                    myc = c(getonedfvalue(el, period))
                    if mydf.id == "F00000IRBFF":
                        str(" gr2 ")
                        cat(myc)
                    myclen = len(myc)
                    myc = head(myc, n=(myclen-headskiprsi))
                    myc = tail(myc, n=macddays)
                    if percentize:
                        if periodtext == "Price" or periodtext == "Index":
                            first = myc[1]
                            myc = myc * (100 / first)
                    rsi = getrsi(myc)
                    rsilist[i] = rsi
                headskiprsi = headskiprsi + tablemoveintervaldays
                rsic = c(unlist(rsilist))
                df = cbind(df, rsic)
                if sort == RSI:
                    if reverse:
                        df = df[order(df.rsic),]
                    else:
                        df = df[order(-df.rsic),]
            dflist[j] = list(df)
        mytopperiod2(dflist, period, topbottom, days, wantrise=wantrise, wantmacd=wantmacd, wantrsi=wantrsi)
        if reverse:
            getbottomchart(market, days, topbottom, stocklistperiod, period)
        else:
            gettopchart(market, days, topbottom, stocklistperiod, period)
              
def gettopchart(market, days, topbottom, stocklistperiod, period):
    mainlist = stocklistperiod[period, 1][[1]]
    oldlist = stocklistperiod[period, days][[1]]
    maindate = mainlist.date[1]
    olddate = oldlist.date[1]
    ls = []
    names = []
    c = 0
    for i in range(0, topbottom):
        l = getelem(mainlist.id[i], days, stocklistperiod, period, topbottom)
        c = c + 1
        ls[c] = list(l)
        names[c] = mainlist.name[i]
    
    periodtext = getmyperiodtext(market, period)
    displaychart(ls, names, topbottom, periodtext, maindate, olddate, days)


def getbottomchart(market, days, topbottom, stocklistperiod, period):
    mainlist = stocklistperiod[period, 1][[1]]
    oldlist = stocklistperiod[period, days][[1]]
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
        ls[c] = list(l)
        names[c] = mainlist.name[len - i]
    
    periodtext = getmyperiodtext(market, period)
    displaychart(ls, names, topbottom, periodtext, maindate, olddate, days)


def displaychart(ls, mynames, topbottom, periodtext, maindate, olddate, days):
    dev.new()
    colours = rainbow(topbottom)
    print("g_range")

def getmacd(m):
    l = len(m) / 2
                                        #    cat("hei\n")
                                        #    str(l)
                                        #    cat("\nhei2\n")
                                        #    m
    c = 1
    retlist1 = [];
    retlist2 = [];
    retlist3 = [];
    for i in range(0, l):
        elem = m[i,]
        first = elem[1]
        second = elem[2]
        if isna(first) and isna(second):
            retlist1[c] = first
            retlist2[c] = second
            retlist3[c] = first - second
            c = c + 1
    #cat(unlist(retlist1))
    #str("")
    #cat(unlist(retlist2))
    #str("")
    #cat(unlist(retlist3))
    return(list(retlist1, retlist2, retlist3))


def getmomhist(myma, deltadays):
    lses = getmylses(myma)
    if lses is None:
        return(0)
    
    retl = []
    ls1 = lses[[1]]
    ls3 = lses[[3]]
    retl[1] = ls1[[len(ls1)]]
    retl[2] = ls3[[len(ls3)]]
    last1 = len(ls1)
    last3 = len(ls3)
    delta = deltadays - 1
    prevs1 = last1 - delta
    prevs3 = last3 - delta
    retl[3] = (ls1[[last1]] - ls1[[prevs1]])/delta
    retl[4] = (ls3[[last3]] - ls3[[prevs3]])/delta
    return(retl)


def getmylses(myma):
                                        #    cat(myma)
                                        #    cat("bla\n")
    myma = fixna(myma)
    if len(myma) < 40:
        return(NULL)
    
    scalebeginning100 = 0
#    if scalebeginning100 == 0:
                                        #        this does not matter?
                                        #        myma = fixpercent(myma)
    
                                        #    cat(myma)
    maType = 'EMA'
    fast = 12
    slow = 26
    sig = 9
    m = MACD(myma, nFast=fast, nSlow=slow, nSig=sig, maType = maType, percent = False )
                                        #    str(m)
                                        #    cat(m)
                                        #    cat("\ngrr\n")
    lses = getmacd(m)
    l = len(myma)
    cat(myma)
    cat(m)
    
    return(lses)


def getrsi(myma):
    lses = getmyrsi(myma)
    if lses is None:
        return(0)
    
    ls = lses[[1]]
    return(ls[[len(ls)]])


def getmyrsi(myma):
                                        #    cat(myma)
                                        #    cat("bla\n")
    myma = fixna(myma)
    if len(myma) < 40:
        return(NULL)
    
    scalebeginning100 = 0
#    if scalebeginning100 == 0:
                                        #        this does not matter?
                                        #        myma = fixpercent(myma)
    
                                        #    cat(myma)
    num = 14
    m = RSI(myma)
                                        #    str(m)
                                        #    cat(m)
                                        #    cat("\ngrr\n")
    l = len(myma)
#    str(myma[l])
    cat(myma)
    cat(m)
    
    return(list(m))


def getmyperiodtext(market, period):
    periodtext = period
    if period >= 0:
        mymeta = getmarketmeta(allmetas, market)
        newtext = getperiodtext(mymeta, period)
        if isna(newtext):
            periodtext = newtext
        
    
    return(periodtext)


#engine = create_engine('postgresql://stockread@localhost:5432/stockstat')
conn = psycopg2.connect("dbname=stockstat user=stockread password=password")

allstocks = getstocks(conn)
allmetas = getmetas(conn)

#print(len(stock))
#print(meta)
#print(type(meta))
