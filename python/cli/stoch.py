import talib as ta

#import myutils as my

class STOCH:

  def title(self):
      return "STOCH"

  def names(self):
      return [ "slowk", "slowd" ]

  def calculate(self, myma):
    l = myma[0]
    llow = myma[1]
    lhigh = myma[2]
    if len(l) < 40:
        return(None)
    if not l.isnull().all():
        slowk, slowd = ta.STOCH(lhigh, llow, l, fastk_period=5, slowk_period=3, slowk_matype=0, slowd_period=3, slowd_matype=0)
    else:
        return None
    return [slowk, slowd]


