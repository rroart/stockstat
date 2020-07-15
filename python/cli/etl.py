def filterweekend(stocks):
    return stocks.loc[(stocks.date.dt.weekday < 5)]
