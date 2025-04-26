package roart.category.impl;

import java.util.List;
import java.util.Map;

import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.common.inmemory.model.Inmemory;
import roart.common.model.MetaDTO;
import roart.common.model.StockDTO;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialMeta;
import roart.common.pipeline.util.PipelineUtils;
import roart.common.util.MathUtil;
import roart.indicator.AbstractIndicator;
import roart.indicator.impl.Indicator;
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

    public CategoryPeriod(IclijConfig conf, int i, String periodText, List<StockDTO> stocks,PipelineData[] datareaders, Inmemory inmemory) throws Exception {
        super(conf, periodText, stocks, datareaders, inmemory);
        period = i;
        createResultMap(conf, stocks);
        PipelineData datareader = PipelineUtils.getPipeline(datareaders, periodText, inmemory);
        if (datareader == null) {
            log.info("empty {}", i);
            createIndicatorMap(periodText);
            return;
        }
        //Map<String, MarketData> marketdatamap = datareader.getMarketdatamap();
        String market = conf.getConfigData().getMarket();
        //MarketData marketData = marketdatamap.get(market);
        //List<StockDTO>[] datedstocklists = marketData.datedstocklists;
        //indicators.add(new IndicatorMove(conf, "Δ" + getTitle(), datedstocklists, period));
        PipelineData metadata = PipelineUtils.getPipeline(datareaders, PipelineConstants.META, inmemory);
        SerialMeta meta = PipelineUtils.getMeta(metadata);
        if (MetaUtil.currentYear(meta, periodText)) {
            indicators.add(new IndicatorMACD(conf, getTitle() + " MACD", getTitle(), i, datareaders, false, inmemory));
            indicators.add(new IndicatorRSI(conf, getTitle() + " RSI", getTitle(), i, datareaders, false, inmemory));
            indicators.add(new IndicatorSTOCHRSI(conf, getTitle() + " SRSI", getTitle(), i, datareaders, false, inmemory));
            indicators.add(new IndicatorSTOCH(conf, getTitle() + " STOCH", getTitle(), i, datareaders, false, inmemory));
            indicators.add(new IndicatorATR(conf, getTitle() + " ATR", getTitle(), i, datareaders, false, inmemory));
            indicators.add(new IndicatorCCI(conf, getTitle() + " CCI", getTitle(), i, datareaders, false, inmemory));
            for (AbstractIndicator indicator : indicators) {
                ((Indicator) indicator).calculate();
            }
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
    public void addResultItem(ResultItemTableRow r, StockDTO stock) {
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
