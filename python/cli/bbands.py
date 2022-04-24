import talib as ta

#import myutils as my

class BBANDS:

  def title(self):
      return "BBANDS"

  def names(self):
      return [ "upperband", "middleband", "lowerband", "close" ]

  def calculate(self, myma):
    l = myma[0]
    llow = myma[1]
    lhigh = myma[2]
    if len(l) < 40:
        return(None)
    if not l.isnull().all():
        upperband, middleband, lowerband = ta.BBANDS(l, timeperiod=5, nbdevup=2, nbdevdn=2, matype=0)
    else:
        return None
    return [upperband, middleband, lowerband, l]


