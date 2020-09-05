import talib as ta

#import myutils as my

doprint = False

class RSI:

  def title(self):
      return "RSI"

  def names(self):
      return [ "rsi" ]

  def getrsi(self, myma):
    lses = self.calculate(myma)
    if lses is None:
        return(None)
        #return pd.Series()
    #print(type(lses), len(lses))
    ls = lses
    #print(len(ls))
    #print("kkk",ls[0].keys())
    keys = ls[0].keys()
    #return ls[0]
    v = ls[0][keys[len(keys) - 1]]
    #print("v ", v)
    return [ v ]


  def calculate(self, myma):
                                        #    print(myma)
                                        #    print("bla\n")
    #myma = my.fixna(myma)
    l = myma[0]
    #llow = myma[1]
    #lhigh = myma[2]
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
        if doprint:
          print("dop",l.values)
          print(m.values)
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


