package roart.category.impl;

import java.util.Arrays;
import java.util.List;

import roart.category.AbstractCategory;
import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.common.model.StockItem;
import roart.common.pipeline.data.PipelineData;
import roart.common.util.MathUtil;
import roart.indicator.AbstractIndicator;
import roart.indicator.impl.Indicator;
import roart.indicator.impl.IndicatorATR;
import roart.indicator.impl.IndicatorCCI;
import roart.indicator.impl.IndicatorMACD;
import roart.indicator.impl.IndicatorRSI;
import roart.indicator.impl.IndicatorSTOCH;
import roart.indicator.impl.IndicatorSTOCHRSI;
import roart.pipeline.Pipeline;
import roart.pipeline.common.predictor.AbstractPredictor;
import roart.predictor.impl.PredictorTensorflowLSTM;
import roart.result.model.ResultItemTableRow;
import roart.stockutil.StockDao;
import roart.stockutil.StockUtil;

public class CategoryIndex extends Category {

    public CategoryIndex(IclijConfig conf, String string, List<StockItem> stocks,
            PipelineData[] datareaders) throws Exception {
        super(conf, string, stocks, datareaders);
        period = Constants.INDEXVALUECOLUMN;
        createResultMap(conf, stocks);
        indicators.add(new IndicatorMACD(conf, getTitle() + " MACD", getTitle(), Constants.INDEXVALUECOLUMN, datareaders, false));
        indicators.add(new IndicatorRSI(conf, getTitle() + " RSI", getTitle(), Constants.INDEXVALUECOLUMN, datareaders, false));
        indicators.add(new IndicatorSTOCHRSI(conf, getTitle() + " SRSI", getTitle(), Constants.INDEXVALUECOLUMN, datareaders, false));
        indicators.add(new IndicatorSTOCH(conf, getTitle() + " STOCH", getTitle(), Constants.INDEXVALUECOLUMN, datareaders, false));
        indicators.add(new IndicatorATR(conf, getTitle() + " ATR", getTitle(), Constants.INDEXVALUECOLUMN, datareaders, false));
        indicators.add(new IndicatorCCI(conf, getTitle() + " CCI", getTitle(), Constants.INDEXVALUECOLUMN, datareaders, false));
        for (AbstractIndicator indicator : indicators) {
            ((Indicator) indicator).calculate();
        }
        createIndicatorMap(Constants.INDEX);
    }

    @Override
    public void addResultItemTitle(ResultItemTableRow r) {
        try {
            if (StockUtil.hasSpecial(stocks, Constants.INDEXVALUECOLUMN)) {
                r.add(getTitle());
                if (dataArraySize > 1) {
                    r.add(getTitle() + " l");
                    r.add(getTitle() + " h");
                    r.add(getTitle() + " o");
                }
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
            if (StockUtil.hasSpecial(stocks, Constants.INDEXVALUECOLUMN)) {
                Object[] array = resultMap.get(stock.getId());
                r.addarr(Arrays.copyOfRange(array, 0, dataArraySize));
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

