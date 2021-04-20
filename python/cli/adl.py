import time

import pandas as pd

import pdutils as pdu

def getadl(datedstocklists, period):
    start = time.time()
    days = len(datedstocklists)
    adl = [ 0 for x in range(days - 1) ]

    #k = days - 1
    for i in range(days - 1):
        #print(i)
        df = datedstocklists[i]
        df2 = datedstocklists[i + 1]
        ids = df.id.tolist()
        #print("ids", len(ids))
        ids2 = df2.id.tolist()
        #print("ids2", len(ids2))
        if i > 0:
            adl[i] = adl[i - 1]
        for j in range(len(ids)):
            anid = ids[j]
            el = df.loc[(df.id == anid)]
            el2 = df2.loc[(df2.id == anid)]
            if i == 0 and anid == 'VIX':
                print(el.indexvalue)
                print("date1", el.date)
            #print(el, el2)
            #print(len(el), len(el2))
            if not len(el) == 1 or not len(el2) == 1:
                #print("el", len(el), len(el2))
                #print("skip", id)
                continue
            val = pdu.getonedfperiod(el, period).values[0]
            val2 = pdu.getonedfperiod(el2, period).values[0]
            if i == 0 and anid == 'VIX':
                print(val, val2)
                print(el.date, el2.date)
            if val2 > val:
                adl[i] += 1
            if val2 < val:
                adl[i] -= 1
    print("time", time.time() - start)
    return adl        
        
def getadl2(stocks, period, listdates):
    start = time.time()
    days = len(listdates)
    print("days2", days)
    adate = pd.to_datetime(listdates[0])
    ddate = adate.strftime('%Y-%m-%d')
    ddate = listdates[len(listdates) - days]
    adate = listdates[len(listdates) - days]
    print(type(adate),type(ddate),ddate)
    stocks = stocks[stocks['date'] >= ddate]
    adl = pd.Series([ 0 for x in range(days - 1) ])
    ids = stocks[stocks['date'] >= ddate].id.unique().tolist()
    print("ids", len(ids))
    ids2 = stocks[stocks['date'] == ddate].id.unique().tolist()
    print("ids2", len(ids2))
    print("date2", ddate)
    dates2 = listdates[-days:]
    #stocks[stocks['date'] >= ddate].sort_values(by='date').date.unique().tolist()
    #print("dates2", dates2)
    for i in range(len(ids)):
        anid = ids[i]
        #list = stocks[(stocks.id == id)].sort_values(by='date')[period].tolist()
        dates = stocks[(stocks.id == id)].sort_values(by='date')['date'].tolist()
        alist = []
        for i in range(len(dates2)):
            adate = dates2[i]
            #print(adate, stocks[(stocks.id == anid) & (stocks.date == adate)])
            elem = stocks[(stocks.id == anid) & (stocks.date == adate)][period].tolist()
            #print(len(elem), elem)
            if len(elem) > 0:
                elem = elem[0]
            else:
                elem = None
            alist.append(elem)
        if anid == 'VIXX':
            print(alist)
        #print(period, alist)
        alist = alist[-days:]
        dates = dates[-days:]
        if anid == 'VIX':
            print(alist)
            print("dates", dates)
        if len(alist) < days:
            #print("skip", id, alist)
            continue
        for j in range(days - 1):
            val = alist[j]
            #print(len(alist), j)
            val2 = alist[j + 1]
            if j == 0 and anid == 'VIX':
                print(val, val2)
            #if j == 0:
            #    print(dates[0])
            #print("vvv", val, val2)
            if val2 is None or val is None:
                continue
            if val2 > val:
                adl[j : days - 1] += 1
            if val2 < val:
                adl[j : days - 1] -= 1
    print("time", time.time() - start)
    return adl.tolist()    

def getadl3(stockdata, datareader):
    start = time.time()
    days = len(stockdata.datedstocklists)
    print("days", days)
    listdates = datareader.datelist
    adate = pd.to_datetime(listdates[0])
    ddate = adate.strftime('%Y-%m-%d')
    ddate = listdates[len(listdates) - days]
    adate = listdates[len(listdates) - days]
    print(type(adate),type(ddate),ddate)
    #stocks = stocks[stocks['date'] >= ddate]
    adl = pd.Series([ 0 for x in range(days - 1) ])
    ids = stockdata.stocks.id.unique()
    print("ids", len(ids))
    #ids2 = stocks[stocks['date'] == ddate].id.unique().tolist()
    #print("ids2", len(ids2))
    #print("date2", ddate)
    #dates2 = listdates[-days:]
    #stocks[stocks['date'] >= ddate].sort_values(by='date').date.unique().tolist()
    #print("dates2", dates2)
    for i in range(len(ids)):
        anid = ids[i]
        if not anid in datareader.listmap:
            continue
        alist = datareader.listmap[anid][0]
        #print("lena",len(alist))
        #print(alist.tolist())
        for j in range(days - 1):
            val = alist[j]
            val2 = alist[j + 1]
            if val2 is None or val is None:
                continue
            #print(val, val2)
            if val2 > val:
                adl[j : days - 1] += 1
            if val2 < val:
                adl[j : days - 1] -= 1
    print("time", time.time() - start)
    return adl.tolist()    


