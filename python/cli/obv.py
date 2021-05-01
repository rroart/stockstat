import talib as ta

#import myutils as my

class OBV:

  def __init__(self, volumelist):
    self.volumelist = volumelist

  def title(self):
      return "OBV"

  def names(self):
      return [ "obv" ]

  def calculate(self, myma):
    l = myma[0]
    if not l.isnull().all():
        m = ta.OBV(l, self.volumelist)
    else:
        return None
    lses = m
    return [lses]


