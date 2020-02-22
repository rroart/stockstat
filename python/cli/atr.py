import talib as ta

#import myutils as my

class ATR:

  def title(self):
      return "ATR"

  def names(self):
      return [ "atr" ]

  def calculate(self, myma):
    l = myma[0]
    llow = myma[1]
    lhigh = myma[2]
    if len(l) < 40:
        return(None)
    if not l.isnull().all():
        m = ta.ATR(lhigh, llow, l, timeperiod=14)
    else:
        return None
    lses = m
    return [lses]


