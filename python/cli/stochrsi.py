import talib as ta

#import myutils as my

class STOCHRSI:

  def title(self):
      return "STOCHRSI"

  def names(self):
      return [ "fastk", "fastd" ]

  def calculate(self, myma):
    l = myma[0]
    llow = myma[1]
    lhigh = myma[2]
    if len(l) < 40:
        return(None)
    if not l.isnull().all():
        fastk, fastd = ta.STOCHRSI(l, timeperiod=14, fastk_period=5, fastd_period=3, fastd_matype=0)
    else:
        return None
    return [fastk, fastd]


