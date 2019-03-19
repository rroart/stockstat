package roart.category.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import roart.category.AbstractCategory;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.indicator.AbstractIndicator;
import roart.indicator.impl.IndicatorATR;
import roart.indicator.impl.IndicatorCCI;
import roart.indicator.impl.IndicatorMACD;
import roart.indicator.impl.IndicatorRSI;
import roart.indicator.impl.IndicatorSTOCH;
import roart.indicator.impl.IndicatorSTOCHRSI;
import roart.model.StockItem;
import roart.pipeline.Pipeline;
import roart.pipeline.common.predictor.AbstractPredictor;
import roart.predictor.impl.PredictorLSTM;
import roart.result.model.ResultItemTableRow;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.stockutil.StockUtil;

public class CategoryPrice extends Category {

    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;

    public CategoryPrice(MyMyConfig conf, String string, List<StockItem> stocks,
            Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap,
            Pipeline[] datareaders) throws Exception {
        super(conf, string, stocks, datareaders);
        this.marketdatamap = marketdatamap;
        this.periodDataMap = periodDataMap;
        period = Constants.PRICECOLUMN;
        createResultMap(conf, stocks);
        indicators.add(new IndicatorMACD(conf, getTitle() + " MACD", getTitle(), Constants.PRICECOLUMN, datareaders, false));
        indicators.add(new IndicatorRSI(conf, getTitle() + " RSI", getTitle(), Constants.PRICECOLUMN, datareaders, false));
        indicators.add(new IndicatorSTOCHRSI(conf, getTitle() + " SRSI", getTitle(), Constants.PRICECOLUMN, datareaders, false));
        indicators.add(new IndicatorSTOCH(conf, getTitle() + " STOCH", getTitle(), Constants.PRICECOLUMN, datareaders, false));
        indicators.add(new IndicatorATR(conf, getTitle() + " ATR", getTitle(), Constants.PRICECOLUMN, datareaders, false));
        indicators.add(new IndicatorCCI(conf, getTitle() + " CCI", getTitle(), Constants.PRICECOLUMN, datareaders, false));
        createIndicatorMap(Constants.PRICE);
    }

    @Override
    public void addResultItemTitle(ResultItemTableRow r) {
        try {
            if (StockUtil.hasSpecial(stocks, Constants.PRICECOLUMN)) {
                r.add(getTitle());
                if (dataArraySize > 1) {
                    r.add(getTitle() + " l");
                    r.add(getTitle() + " h");
                }
                r.add("Currency");
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
            if (StockUtil.hasSpecial(stocks, Constants.PRICECOLUMN)) {
                Object[] array = resultMap.get(stock.getId());
                r.addarr(Arrays.copyOfRange(array, 0, dataArraySize));
                r.add(stock.getCurrency());
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

