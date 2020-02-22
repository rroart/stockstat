import talib as ta

#import myutils as my

class CCI:

  def title(self):
      return "CCI"

  def names(self):
      return [ "cci" ]

  def calculate(self, myma):
    l = myma[0]
    llow = myma[1]
    lhigh = myma[2]
    if len(l) < 40:
        return(None)
    if not l.isnull().all():
        m = ta.CCI(lhigh, llow, l, timeperiod=14)
    else:
        return None
    lses = m
    return [lses]


