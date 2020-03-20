import pandas as pd
pd.core.common.is_list_like = pd.api.types.is_list_like
import pandas_datareader as web

from datetime import datetime, timedelta

class Datareader:
    def my(self, days, delta):
        result = []
        date_n_days_ago = datetime.now() - timedelta(days=days)
        end = date_n_days_ago
        delta_days_ago = date_n_days_ago - timedelta(days=delta)
        start = delta_days_ago
        symbols = [ 'DJCA', 'DJIA', 'DJTA', 'SP500', 'DJUA', 'NASDAQ100', 'NASDAQCOM', 'VXNCLS', 'VXFXICLS', 'VXEEMCLS', 'VXDCLS', 'OVXCLS', 'GVZCLS', 'VXVCLS', 'VXTYN', 'VIXCLS' ]
        #print("start end date " + str(start) + " " + str(end))
        for symbol in symbols:
            #name = symbol[1]
            ticker = symbol
            try:
                frame = web.DataReader(ticker, 'fred', start, end)
                #print(frame)
                #print(frame.dtypes)
                list = []
                list.append(symbol)
                list.append(frame)
                result.append(list)
            except:
                print("Exception for " + ticker)
                import sys,traceback
                traceback.print_exc(file=sys.stdout)
                print("\n")
        return result
