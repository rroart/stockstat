package roart.category;

import java.util.List;
import java.util.Map;

import roart.config.MyConfig;
import roart.indicator.Indicator;
import roart.indicator.IndicatorMACD;
import roart.indicator.IndicatorRSI;
import roart.model.ResultItemTableRow;
import roart.model.Stock;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.StockDao;
import roart.util.StockUtil;

public class CategoryIndex extends Category {

    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;
    Map<String, Integer>[] periodmap;

    public CategoryIndex(MyConfig conf, String string, List<Stock> stocks,
            Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap,
            Map<String, Integer>[] periodmap) {
        super(conf, string, stocks);
        this.marketdatamap = marketdatamap;
        this.periodmap = periodmap;
        this.periodDataMap = periodDataMap;
        indicators.add(new IndicatorMACD(conf, title + " mom", marketdatamap, periodDataMap, periodmap, title));
        indicators.add(new IndicatorRSI(conf, title + " RSI", marketdatamap, periodDataMap, periodmap, title));
    }

    @Override
    public void addResultItemTitle(ResultItemTableRow r) {
        try {
            if (StockUtil.hasSpecial(stocks, Constants.INDEXVALUECOLUMN)) {
                r.add(title);
                for (Indicator indicator : indicators) {
                    if (indicator.isEnabled()) {
                        r.add(indicator.getResultItemTitle());
                    }
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    @Override
    public void addResultItem(ResultItemTableRow r, Stock stock) {
        try {
            if (StockUtil.hasSpecial(stocks, Constants.INDEXVALUECOLUMN)) {
                r.add(stock.getIndexvalue());
                for (Indicator indicator : indicators) {
                    if (indicator.isEnabled()) {
                        r.add(indicator.getResultItem(stock));
                    }
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

}

