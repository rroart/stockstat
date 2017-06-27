package roart.category;

import java.util.List;
import java.util.Map;

import roart.config.MyConfig;
import roart.indicator.Indicator;
import roart.indicator.IndicatorMACD;
import roart.indicator.IndicatorRSI;
import roart.indicator.IndicatorSTOCHRSI;
import roart.model.ResultItemTableRow;
import roart.model.StockItem;
import roart.predictor.Predictor;
import roart.predictor.PredictorLSTM;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.StockDao;
import roart.util.StockUtil;

public class CategoryPrice extends Category {

    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;
    Map<String, Integer>[] periodmap;

    public CategoryPrice(MyConfig conf, String string, List<StockItem> stocks,
            Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap,
            Map<String, Integer>[] periodmap) throws Exception {
        super(conf, string, stocks);
        this.marketdatamap = marketdatamap;
        this.periodmap = periodmap;
        this.periodDataMap = periodDataMap;
        indicators.add(new IndicatorMACD(conf, getTitle() + " MACD", marketdatamap, periodDataMap, periodmap, getTitle(), Constants.PRICECOLUMN));
        indicators.add(new IndicatorRSI(conf, getTitle() + " RSI", marketdatamap, periodDataMap, periodmap, getTitle(), Constants.PRICECOLUMN));
        indicators.add(new IndicatorSTOCHRSI(conf, getTitle() + " SRSI", marketdatamap, periodDataMap, periodmap, getTitle(), Constants.PRICECOLUMN));
        predictors.add(new PredictorLSTM(conf, getTitle() + "LSTM", marketdatamap, periodDataMap, periodmap, getTitle(), Constants.PRICECOLUMN));
    }

    @Override
    public void addResultItemTitle(ResultItemTableRow r) {
        try {
            if (StockUtil.hasSpecial(stocks, Constants.PRICECOLUMN)) {
                r.add(getTitle());
                r.add("Currency");
                for (Predictor predictor : predictors) {
                    if (predictor.isEnabled()) {
                        r.addarr(predictor.getResultItemTitle());
                    }
                }
                for (Indicator indicator : indicators) {
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
            if (StockUtil.hasSpecial(stocks, Constants.PRICECOLUMN)) {
                r.add(stock.getPrice());
                r.add(stock.getCurrency());
                for (Predictor predictor : predictors) {
                    if (predictor.isEnabled()) {
                        r.addarr(predictor.getResultItem(stock));
                    }
                }
                for (Indicator indicator : indicators) {
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

