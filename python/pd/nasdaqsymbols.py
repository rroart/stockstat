from pandas_datareader.nasdaq_trader import get_nasdaq_symbols

# symbols.groupby('Market Category').count()

def get_nasdaq_symbols_category(category):
    symbols = get_nasdaq_symbols()
    symbols_category = symbols.loc[symbols['Market Category'] == category]
    return symbols_category[['NASDAQ Symbol', 'Security Name']]
