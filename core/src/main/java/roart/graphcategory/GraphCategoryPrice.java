package roart.graphcategory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import roart.config.MyMyConfig;
import roart.graphindicator.GraphIndicator;
import roart.graphindicator.GraphIndicatorMACD;
import roart.graphindicator.GraphIndicatorRSI;
import roart.graphindicator.GraphIndicatorSTOCHRSI;
import roart.ml.MLPredictDao;
import roart.ml.MLPredictModel;
import roart.model.GUISize;
import roart.model.LearnTestPredict;
import roart.model.ResultItemBytes;
import roart.model.StockItem;
import roart.predictor.PredictorLSTM;
import roart.model.ResultItem;
import roart.util.ArraysUtil;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.StockDao;
import roart.util.StockUtil;
import roart.util.SvgUtil;

public class GraphCategoryPrice extends GraphCategory {

    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;

    public GraphCategoryPrice(MyMyConfig conf, String string,
            Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap) {
        super(conf, string);
        this.marketdatamap = marketdatamap;
        this.periodDataMap = periodDataMap;
        indicators.add(new GraphIndicatorMACD(conf, title + " mom", marketdatamap, periodDataMap, title));
        indicators.add(new GraphIndicatorRSI(conf, title + " RSI", marketdatamap, periodDataMap, title));
        indicators.add(new GraphIndicatorSTOCHRSI(conf, title + " SRSI", marketdatamap, periodDataMap, title));
        //predictors.add(new PredictorLSTM(conf, title + "LSTM", marketdatamap, periodDataMap, periodmap, title, Constants.PRICECOLUMN));
        //predictors.add();
    }

    @Override
    public void addResult(List<ResultItem> retlist, Set<Pair<String, String>> ids, GUISize guiSize) {
        try {
            if (StockUtil.hasSpecial(marketdatamap, Constants.PRICECOLUMN)) {
                String periodText = title;
                int days = conf.getTableDays();
                int topbottom = conf.getTopBottom();

                System.out.println("check " + periodText + " " + periodDataMap.keySet());
                PeriodData perioddata = periodDataMap.get(periodText);
                //PeriodData perioddata = new PeriodData();
                DefaultCategoryDataset dataseteq = null;
                if (conf.isGraphEqualize()) {    
                    dataseteq = new DefaultCategoryDataset( );
                }
                DefaultCategoryDataset dataset = StockUtil.getFilterChartDated(days, ids, marketdatamap, perioddata, Constants.PRICECOLUMN, conf.isGraphEqualize(), dataseteq);
                if (conf.wantPredictorLSTM()){
                    Pair pair2 = ids.iterator().next();
                    String market = (String) pair2.getFirst();
                    String id = (String) pair2.getSecond();
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
                            Pair<String, String> pair = new Pair(market, stock.getId());
                            if (ids.contains(pair)) {
                                try {
                                    Double value = StockDao.getSpecial(stock, Constants.PRICECOLUMN);
                                    if (value == null) {
                                        continue;
                                    }
                                    String stockName = stock.getName();
                                    log.info("info " + stockName + " " + value + " " + new Integer(-j));
                                    endlist.add(value);
                                } catch (Exception e) {
                                    log.error("E", e);
                                }
                            }
                        }
                    }
                     LearnTestPredict pred = predictme((Double[])endlist.toArray());
                    Double[] predme = pred.predicted;
                    for (int k = 0; k < predme.length; k++) {
                        dataset.addValue(predme[k], "p", new Integer(k+1));
                    }
                }
                if (dataset != null) {
                    String currency = null; //datedstocklists[0].get(0).getCurrency();
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
                    String currency = null; //datedstocklists[0].get(0).getCurrency();
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
    
    LearnTestPredict predictme(Double[] list) {
        MLPredictDao mldao = new MLPredictDao("tensorflow", conf);
        int horizon = conf.getPredictorLSTMHorizon();
        int windowsize = conf.getPredictorLSTMWindowsize();
        int epochs = conf.getPredictorLSTMEpochs();
        //Double[] list = listMap.get(id);
        // TODO check reverse. move up before if?
        list = ArraysUtil.getArrayNonNullReverse(list);
        log.info("bla " + list.length + " " + windowsize);
        if (list != null && list.length > 2 * windowsize ) {
            Map map = null;
            String mapName = null;
            List next = null;
            String key = null;
            Map<MLPredictModel, Long> mapTime = null; //new HashMap<>();;
            LearnTestPredict result = mldao.learntestpredict(null, list, next, map, null, conf.getMACDDaysBeforeZero(), key, mapName, 4, mapTime, windowsize, horizon, epochs);  
            return result;
        }
        return null;
    }

}

