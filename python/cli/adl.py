import time

import pandas as pd

import pdutils as pdu

def getadl(datedstocklists, period, days):
    start = time.time()
    if days is None:
        days = len(datedstocklists)
    adl = [ 0 for x in range(days - 1) ]

    #k = days - 1
    for i in range(days - 1):
        #print(i)
        df = datedstocklists[1 + i]
        df2 = datedstocklists[1 + i + 1]
        ids = df.id.tolist()
        #print("ids", len(ids))
        ids2 = df2.id.tolist()
        #print("ids2", len(ids2))
        if i > 0:
            adl[i] = adl[i - 1]
        for j in range(len(ids)):
            id = ids[j]
            el = df.loc[(df.id == id)]
            el2 = df2.loc[(df2.id == id)]
            if i == 0 and id == 'VIX':
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
            if i == 0 and id == 'VIX':
                print(val, val2)
                print(el.date, el2.date)
            if val2 > val:
                adl[i] += 1
            if val2 < val:
                adl[i] -= 1
    print(time.time() - start)
    return adl        
        
def getadl2(stocks, period, days, listdates):
    start = time.time()
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
    print("dates2", dates2)
    for i in range(len(ids)):
        id = ids[i]
        #list = stocks[(stocks.id == id)].sort_values(by='date')[period].tolist()
        dates = stocks[(stocks.id == id)].sort_values(by='date')['date'].tolist()
        list = []
        for i in range(len(dates2)):
            adate = dates2[i]
            elem = stocks[(stocks.id == id) & (stocks.date == adate)][period].tolist()
            #print(len(elem), elem)
            if len(elem) > 0:
                elem = elem[0]
            else:
                elem = None
            list.append(elem)
        if id == 'VIXX':
            print(list)
        list = list[-days:]
        dates = dates[-days:]
        if id == 'VIX':
            print(list)
            print("dates", dates)
        if len(list) < days:
            #print("skip", id, list)
            continue
        for j in range(days - 1):
            val = list[j]
            #print(len(list), j)
            val2 = list[j + 1]
            if j == 0 and id == 'VIX':
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
    print(time.time() - start)
    return adl.tolist()    

