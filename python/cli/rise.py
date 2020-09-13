import pandas as pd
import numpy as np

periods = 11

class RISE:
    def __init__(self, stockdata):
        self.count = 0;
        self.stockdata = stockdata
        self.periodmaps = getlistmove(stockdata.datedstocklists, stockdata.listid, stockdata.listdate, stockdata.days, stockdata.tablemoveintervaldays, stockdata.stocklistperiod)
        
    def title(self):
        return "RISE";

    def names(self):
        return "rise";

    def dfextend(self, df, period, periodtext, sort, interpolate = True, rebase = False, deltadays = 3, reverse = False):
        list2 = []
        if self.count < self.stockdata.days:
            list2 = self.periodmaps[period][self.count]
        riselist = [ None for x in range(len(df)) ]
        for i in range(len(df)):
            id = df.id.iloc[i]
            rise = 0
            if self.count < self.stockdata.days:
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
        self.count = self.count + 1

    def titles(self):
      return [ "rise" ]

    def values(self, df, i):
      rise = df.risec.iloc[i]
      if np.isnan(rise):
        rise = 0
      return [ rise ]
    
    def formats(self):
      return [ "{:3d}" ]

def getlistmove(datedstocklists, listid, listdate, count, tableintervaldays, stocklistperiod):
    #periodmaps = [] #matrix([], nrow = periods, ncol = (count - 1))
    periodmaps = [[None for x in range(count)] for y in range(periods)]
    print("ddd")
    print(count)
    print(periods)
    for j in range(count):
        for i in range(periods):
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

