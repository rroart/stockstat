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
import roart.pipeline.Pipeline;
import roart.predictor.impl.PredictorLSTM;
import roart.result.model.ResultItemTableRow;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.stockutil.StockUtil;

public class CategoryPeriod extends Category {

    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;
    Map<String, Integer>[] periodmap;

    public CategoryPeriod(MyMyConfig conf, int i, String periodText, List<StockItem> stocks,             Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap,
            Map<String, Integer>[] periodmap, Pipeline[] datareaders) throws Exception {
        super(conf, periodText, stocks, datareaders);
        this.periodmap = periodmap;
        period = i;
        createResultMap(conf, stocks);
        indicators.add(new IndicatorMove(conf, "Δ" + getTitle(), periodmap, period));
        if (periodText.equals("cy")) {
            indicators.add(new IndicatorMACD(conf, getTitle() + " MACD", marketdatamap, periodDataMap, periodmap, getTitle(), i, datareaders, false));
            indicators.add(new IndicatorRSI(conf, getTitle() + " RSI", marketdatamap, periodDataMap, periodmap, getTitle(), i, datareaders, false));
            predictors.add(new PredictorLSTM(conf, getTitle() + "LSTM", marketdatamap, periodDataMap, periodmap, getTitle(), i));
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
