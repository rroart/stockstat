package roart.category.impl;

import java.util.List;
import java.util.Map;

import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.indicator.AbstractIndicator;
import roart.indicator.impl.IndicatorMACD;
import roart.indicator.impl.IndicatorMove;
import roart.indicator.impl.IndicatorRSI;
import roart.model.StockItem;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.pipeline.Pipeline;
import roart.result.model.ResultItemTableRow;
import roart.stockutil.MetaUtil;
import roart.stockutil.StockUtil;

public class CategoryPeriod extends Category {

    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;
    private Integer cy;
    
    public CategoryPeriod(MyMyConfig conf, int i, String periodText, List<StockItem> stocks,             Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap,
            List<StockItem>[] datedstocklists, Pipeline[] datareaders) throws Exception {
        super(conf, periodText, stocks, datareaders);
        period = i;
        createResultMap(conf, stocks);
        indicators.add(new IndicatorMove(conf, "Δ" + getTitle(), datedstocklists, period));
        String market = conf.getMarket();
        MarketData marketData = marketdatamap.get(market);
        if (MetaUtil.currentYear(marketData, periodText)) {
            indicators.add(new IndicatorMACD(conf, getTitle() + " MACD", getTitle(), i, datareaders, false));
            indicators.add(new IndicatorRSI(conf, getTitle() + " RSI", getTitle(), i, datareaders, false));
            cy = i;
        }
        createIndicatorMap(periodText);
    }

    @Override
    public void addResultItemTitle(ResultItemTableRow r) {
        try {
            if (StockUtil.hasStockPeriod(stocks, period)) {
                r.add(getTitle());
                for (AbstractIndicator indicator : indicators) {
                    if (indicator.isEnabled()) {
                        r.addarr(indicator.getResultItemTitle());
                    }
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    @Override
    public void addResultItem(ResultItemTableRow r, StockItem stock) {
        try {
            if (StockUtil.hasStockPeriod(stocks, period)) {
                r.addarr(resultMap.get(stock.getId()));
                for (AbstractIndicator indicator : indicators) {
                    if (indicator.isEnabled()) {
                        r.addarr(indicator.getResultItem(stock));
                    }
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

}
