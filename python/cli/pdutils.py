def getonedfvalue(df, atype):
    if atype >= 0:
        return(getonedfperiod(df, atype))
    if atype < 0:
        return(getonedfspecial(df, atype))
    print("should not be here")

def getonedfvaluearr(df, atype, meta):
    if atype >= 0:
        return(getonedfperiodarr(df, atype, meta))
    if atype < 0:
        return(getonedfspecialarr(df, atype))
    print("should not be here")

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
    if isinstance(period, str):
        return df[period]
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

def getonedfperiodarr(df, period, meta):
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
        text = meta.pricename.values[0]
        if not text == 'price':
            return [ df[text] ]
        return [ df.price, df.pricelow, df.pricehigh, df.priceopen ]
    if period == 10:
        text = meta.indexvaluename.values[0]
        if not text == 'indexvalue':
            return [ df[text] ]
        return [ df.indexvalue, df.indexvaluelow, df.indexvaluehigh, df.indexvalueopen ]
    #print("should not be here")
    return None

def listperiod2(list, period):
    #print(len(list))
    if period == 0:
        return list.period1
    if period == 1:
        return list.period2
    if period == 2:
        return list.period3
    if period == 3:
        return list.period4
    if period == 4:
        return list.period5
    if period == 5:
        return list.period6
    if period == 6:
        return list.period7
    if period == 7:
        return list.period8
    if period == 8:
        return list.period9
    if period == 9:
        return list.price
    if period == 10:
        return list.indexvalue
    return None
                                        # out of use
