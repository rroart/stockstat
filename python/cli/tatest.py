import pandas as pd

import ma
import atr
import cci
import macd
import rsi
import stoch
import stochrsi

import unittest

import testdata

class MyTestCase(unittest.TestCase):

    def test_something(self):
        stockdata = None
        #myma = self.getohlc()
        myma = self.getrandomohlc()
        indicators = []
        indicators.append(atr.ATR())
        indicators.append(cci.CCI())
        indicators.append(macd.MACD(stockdata))
        indicators.append(rsi.RSI(stockdata))
        indicators.append(stoch.STOCH())
        indicators.append(stochrsi.STOCHRSI())
        numindicators = len(indicators)
        for i in range(numindicators):
            print("i", i)
            indicator = indicators[i]
            lses = indicator.calculate(myma)
            for i in range(len(lses)):
                print(lses[i].tolist())

    def getohlc(self):
        close = testdata.gen(100,280)
        low = testdata.add(close, -0.5)
        high = testdata.add(close, 0.5)
        myma = [ pd.Series(close), pd.Series(low), pd.Series(high) ]
        return myma
    
    def getrandomohlc(self):
        close = testdata.genrand(100, 180, 0.4)
        low = testdata.genrandadd(close, -1, 0.5)
        high = testdata.genrandadd(close, 1, 0.5)
        myma = [ pd.Series(close), pd.Series(low), pd.Series(high) ]
        return myma
        
if __name__ == '__main__':
    unittest.main()
