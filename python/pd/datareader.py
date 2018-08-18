import pandas as pd
pd.core.common.is_list_like = pd.api.types.is_list_like
import pandas_datareader as web

import nasdaqsymbols as nd

from datetime import datetime, timedelta

class Datareader:
    def my(self, days, delta):
        result = []
        date_n_days_ago = datetime.now() - timedelta(days=days)
        end = date_n_days_ago
        delta_days_ago = date_n_days_ago - timedelta(days=delta)
        start = delta_days_ago
        symbols = nd.get_nasdaq_symbols_category('G')
        #symbols = symbols.head()
        #print("start end date " + str(start) + " " + str(end))
        for symbol in symbols.itertuples():
            ticker = symbol[0]
            #name = symbol[1]
            try:
                frame = web.DataReader(ticker, 'iex', start, end)
                #print(frame)
                #print(frame.dtypes)
                list = []
                list.append(symbol)
                list.append(frame)
                result.append(list)
            except:
                print("Exception for " + ticker)
        return result
