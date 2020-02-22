import talib as ta

#import myutils as my

class RSI:

  def title(self):
      return "RSI"

  def names(self):
      return [ "rsi" ]

  def getrsi(self, myma):
    lses = self.getmyrsi(myma)
    if lses is None:
        return(None)
        #return pd.Series()
    #print(type(lses), len(lses))
    ls = lses
    keys = ls.keys()
    return ls[keys[len(keys) - 1]]


  def calculate(self, myma):
                                        #    print(myma)
                                        #    print("bla\n")
    #myma = my.fixna(myma)
    l = myma[0]
    llow = myma[1]
    lhigh = myma[2]
    if len(l) < 40:
        return(None)
    
    scalebeginning100 = 0
#    if scalebeginning100 == 0:
                                        #        this does not matter?
                                        #        myma = fixpercent(myma)
    
                                        #    print(myma)
    num = 14
    if not l.isnull().all():
        m = ta.RSI(l)
    else:
        return None
                                        #    print(m)
                                        #    print(m)
                                        #    print("\ngrr\n")
    l = len(myma)
#    print(myma[l])
    #print(myma)
    #print(m)
    
    return [m]


