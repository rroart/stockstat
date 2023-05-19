package roart.graphcategory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.ImmutablePair;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetConfigs;
import roart.common.model.StockItem;
import roart.graphindicator.GraphIndicator;
import roart.graphindicator.GraphIndicatorATR;
import roart.graphindicator.GraphIndicatorCCI;
import roart.graphindicator.GraphIndicatorMACD;
import roart.graphindicator.GraphIndicatorRSI;
import roart.graphindicator.GraphIndicatorSTOCH;
import roart.graphindicator.GraphIndicatorSTOCHRSI;
import roart.ml.dao.MLPredictDao;
import roart.ml.model.LearnTestPredictResult;
import roart.ml.model.MLPredictModel;
import roart.result.model.GUISize;
import roart.result.model.ResultItem;
import roart.result.model.ResultItemBytes;
import roart.stockutil.StockDao;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.stockutil.StockUtil;
import roart.util.SvgUtil;

public class GraphCategoryPrice extends GraphCategory {

    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;

    public GraphCategoryPrice(IclijConfig conf, String string,
            Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap) {
        super(conf, string);
        this.marketdatamap = marketdatamap;
        this.periodDataMap = periodDataMap;
        indicators.add(new GraphIndicatorMACD(conf, title + " mom", marketdatamap, periodDataMap, title));
        indicators.add(new GraphIndicatorRSI(conf, title + " RSI", marketdatamap, periodDataMap, title));
        indicators.add(new GraphIndicatorSTOCHRSI(conf, title + " SRSI", marketdatamap, periodDataMap, title));
        indicators.add(new GraphIndicatorSTOCH(conf, title + " STOCH", marketdatamap, periodDataMap, title));
        indicators.add(new GraphIndicatorCCI(conf, title + " CCI", marketdatamap, periodDataMap, title));
        indicators.add(new GraphIndicatorATR(conf, title + " ATR", marketdatamap, periodDataMap, title));
    }

    @Override
    public void addResult(List<ResultItem> retlist, Set<Pair<String, String>> ids, GUISize guiSize) {
        try {
            if (StockUtil.hasSpecial(marketdatamap, Constants.PRICECOLUMN)) {
                String periodText = title;
                int days = conf.getTableDays();
                int topbottom = conf.getTopBottom();

                log.info("check {} {}", periodText, periodDataMap.keySet());
                PeriodData perioddata = periodDataMap.get(periodText);
                DefaultCategoryDataset dataseteq = null;
                if (conf.isGraphEqualize()) {    
                    dataseteq = new DefaultCategoryDataset( );
                }
                DefaultCategoryDataset dataset = StockUtil.getFilterChartDated(days, ids, marketdatamap, perioddata, Constants.PRICECOLUMN, conf.isGraphEqualize(), dataseteq);
                if (conf.wantPredictorTensorflowLSTM()){
                    Pair<String, String> pair2 = ids.iterator().next();
                    String market = pair2.getLeft();
                    MarketData marketdata = marketdatamap.get(market);
                    List<StockItem>[] datedstocklists = marketdata.datedstocklists;
                    List<Double> endlist = new ArrayList<>();
                    for (int j = days - 1; j >= 0; j--) {
                        List<StockItem> list = datedstocklists[j];
                        if (list == null) {
                            continue;
                        }
                        for (int i = 0; i < list.size(); i++) {
                            StockItem stock = list.get(i);
                            Pair<String, String> pair = new ImmutablePair(market, stock.getId());
                            if (ids.contains(pair)) {
                                try {
                                    Double value = StockDao.getMainSpecial(stock, Constants.PRICECOLUMN);
                                    if (value == null) {
                                        continue;
                                    }
                                    String stockName = stock.getName();
                                    log.info("info {} {} {}", stockName, value, new Integer(-j));
                                    endlist.add(value);
                                } catch (Exception e) {
                                    log.error(Constants.EXCEPTION, e);
                                }
                            }
                        }
                    }
                    Double[] predme = predictme((Double[])endlist.toArray());
                    for (int k = 0; k < predme.length; k++) {
                        dataset.addValue(predme[k], "p", new Integer(k+1));
                    }
                }
                if (dataset != null) {
                    String currency = null;
                    if (currency == null) {
                        currency = "Value";
                    }
                    JFreeChart c = SvgUtil.getChart(dataset, Constants.PRICE, "Time " + perioddata.date0 + " - " + perioddata.date1, currency, days, topbottom);
                    OutputStream r = SvgUtil.chartToStream(c, "/tmp/new20"+ 1 +".svg", days, topbottom, conf.getTableDays(), conf.getTopBottom(), guiSize, Constants.FULLSIZE);
                    ResultItemBytes stream = new ResultItemBytes();
                    stream.bytes = ((ByteArrayOutputStream) r).toByteArray();
                    stream.fullsize = true;
                    retlist.add(stream);
                }
                if (dataset != null && dataseteq != null) {
                    String currency = null; 
                    if (currency == null) {
                        currency = "Value";
                    }
                    JFreeChart c = SvgUtil.getChart(dataseteq, Constants.PRICE, "Time " + perioddata.date0 + " - " + perioddata.date1, currency, days, topbottom);
                    OutputStream r = SvgUtil.chartToStream(c, "/tmp/new20"+ 1 +".svg", days, topbottom, conf.getTableDays(), conf.getTopBottom(), guiSize, Constants.FULLSIZE);
                    ResultItemBytes stream = new ResultItemBytes();
                    stream.bytes = ((ByteArrayOutputStream) r).toByteArray();
                    stream.fullsize = true;
                    retlist.add(stream);
                }
                for (GraphIndicator indicator : indicators) {
                    if (indicator.isEnabled()) {
                        indicator.getResult(retlist, ids, guiSize);
                    }
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    Double[] predictme(Double[] list) {
        MLPredictDao mldao = new MLPredictDao(MLConstants.TENSORFLOW, conf);
/*
        int horizon = conf.getPredictorLSTMHorizon();
        int windowsize = conf.getPredictorLSTMWindowsize();
        int epochs = conf.getPredictorLSTMEpochs();
        */
        
        // check reverse. move up before if?
        if (list != null) {
            log.info("list {} {}", list.length);
            String key = null;
            Map<MLPredictModel, Long> mapTime = null;
            LearnTestPredictResult result = mldao.predictone(new NeuralNetConfigs(), null, list, null, conf.getMACDDaysBeforeZero(), key, 4, mapTime);  
            return result.predicted;
        }
        return null;
    }

}

