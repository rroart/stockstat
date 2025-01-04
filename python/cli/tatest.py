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
        expect = [ 1, 1, 3, 1, 2, 2 ]
        #myma = self.getohlc()
        myma = self.getrandomohlc()
        indicators = self.getindicators()
        numindicators = len(indicators)
        for i in range(numindicators):
            print("i", i)
            indicator = indicators[i]
            lses = indicator.calculate(myma)
            self.assertEqual(expect[i], len(lses))
            for j in range(len(lses)):
                print(lses[j].tolist())

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

    def getindicators(self):
        stockdata = None
        indicators = []
        indicators.append(atr.ATR())
        indicators.append(cci.CCI())
        indicators.append(macd.MACD(stockdata))
        indicators.append(rsi.RSI(stockdata))
        indicators.append(stoch.STOCH())
        indicators.append(stochrsi.STOCHRSI())
        return indicators

    
    def test_something2(self):
        import numpy as np
        arrnot =  [ 23.98, 23.92, 23.79, 23.67, 23.54, 23.36, 23.65, 23.72, 24.16,
            23.91, 23.81, 23.92, 23.74, 24.68, 24.94, 24.93, 25.10, 25.12, 25.20, 25.06, 24.50, 24.31, 24.57, 24.62,
            24.49, 24.37, 24.41, 24.35, 23.75, 24.09 ]
        arr = [ 50.45, 50.30, 50.20, 50.15, 50.05, 50.06, 50.10, 50.08, 50.03, 50.07, 50.01, 50.14, 50.22, 50.43, 50.50, 50.56, 50.52, 50.70, 50.55, 50.62, 50.90, 50.82, 50.86, 51.20, 51.30, 51.10,
                50.45, 50.30, 50.20, 50.15, 50.05, 50.06, 50.10, 50.08, 50.03, 50.07, 50.01, 50.14, 50.22, 50.43, 50.50, 50.56, 50.52, 50.70, 50.55, 50.62, 50.90, 50.82, 50.86, 51.20, 51.30, 51.10 ]
        myma = [ pd.Series(np.asarray(arr)), pd.Series(np.asarray(arr)), pd.Series(np.asarray(arr)) ]
        indicators = self.getindicators()
        numindicators = len(indicators)
        for i in range(numindicators):
            print("i", i)
            indicators = self.getindicators()
            indicator = indicators[i]
            lses = indicator.calculate(myma)
            for j in range(len(lses)):
                print(lses[j].tolist())

if __name__ == '__main__':
    unittest.main()
