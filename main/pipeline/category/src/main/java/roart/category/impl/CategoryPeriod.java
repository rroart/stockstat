package roart.category.impl;

import java.util.List;
import java.util.Map;

import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.model.StockItem;
import roart.common.util.MathUtil;
import roart.indicator.AbstractIndicator;
import roart.indicator.impl.IndicatorATR;
import roart.indicator.impl.IndicatorCCI;
import roart.indicator.impl.IndicatorMACD;
import roart.indicator.impl.IndicatorMove;
import roart.indicator.impl.IndicatorRSI;
import roart.indicator.impl.IndicatorSTOCH;
import roart.indicator.impl.IndicatorSTOCHRSI;
import roart.indicator.util.IndicatorUtils;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.pipeline.Pipeline;
import roart.pipeline.impl.DataReader;
import roart.result.model.ResultItemTableRow;
import roart.stockutil.MetaUtil;
import roart.stockutil.StockUtil;

public class CategoryPeriod extends Category {

    public CategoryPeriod(MyMyConfig conf, int i, String periodText, List<StockItem> stocks,             Pipeline[] datareaders) throws Exception {
        super(conf, periodText, stocks, datareaders);
        period = i;
        createResultMap(conf, stocks);
        Map<String, Pipeline> pipelineMap = IndicatorUtils.getPipelineMap(datareaders);
        DataReader datareader = (DataReader) pipelineMap.get("" + i);
        if (datareader == null) {
            log.info("empty {}", i);
            createIndicatorMap(periodText);
            return;
        }
        Map<String, MarketData> marketdatamap = datareader.getMarketdatamap();
        String market = conf.getMarket();
        MarketData marketData = marketdatamap.get(market);
        List<StockItem>[] datedstocklists = marketData.datedstocklists;
        indicators.add(new IndicatorMove(conf, "Δ" + getTitle(), datedstocklists, period));
        if (MetaUtil.currentYear(marketData, periodText)) {
            indicators.add(new IndicatorMACD(conf, getTitle() + " MACD", getTitle(), i, datareaders, false));
            indicators.add(new IndicatorRSI(conf, getTitle() + " RSI", getTitle(), i, datareaders, false));
            indicators.add(new IndicatorSTOCHRSI(conf, getTitle() + " SRSI", getTitle(), i, datareaders, false));
            indicators.add(new IndicatorSTOCH(conf, getTitle() + " STOCH", getTitle(), i, datareaders, false));
            indicators.add(new IndicatorATR(conf, getTitle() + " ATR", getTitle(), i, datareaders, false));
            indicators.add(new IndicatorCCI(conf, getTitle() + " CCI", getTitle(), i, datareaders, false));
        }
        createIndicatorMap(periodText);
    }

    @Override
    public void addResultItemTitle(ResultItemTableRow r) {
        try {
            if (StockUtil.hasStockPeriod(stocks, period)) {
                r.add(getTitle());
                for (AbstractIndicator indicator : indicators) {
                    if (indicator.isEnabled() && indicator.fieldSize > 0) {
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
                    if (indicator.isEnabled() && indicator.fieldSize > 0) {
                        Object[] fields = indicator.getResultItem(stock);
                        //fields = MathUtil.round2(fields, 3);
                        r.addarr(fields);
                    }
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

}
