package roart.service.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math3.util.Pair;

import roart.category.AbstractCategory;
import roart.category.impl.CategoryIndex;
import roart.category.impl.CategoryPeriod;
import roart.category.impl.CategoryPrice;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.db.dao.DbDao;
import roart.db.dao.util.DbDaoUtil;
import roart.model.MetaItem;
import roart.model.StockItem;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.pipeline.Pipeline;
import roart.pipeline.common.predictor.AbstractPredictor;
import roart.pipeline.impl.DataReader;
import roart.pipeline.impl.ExtraReader;
import roart.predictor.impl.PredictorLSTM;
import roart.result.model.ResultItemTable;
import roart.result.model.ResultItemTableRow;
import roart.stockutil.MetaUtil;
import roart.stockutil.StockUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceUtil {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * return perioddata for periodtext, or new one if not existing
     *
     * @param periodDataMap map from periodtext to perioddata
     * @param text periodtext
     * @return perioddata
     */
    
    public PeriodData getPeriodData(Map<String, PeriodData> periodDataMap, String text) {
        PeriodData perioddata = periodDataMap.get(text);
        if (perioddata == null) {
            perioddata = new PeriodData();
            periodDataMap.put(text, perioddata);
        }
        return perioddata;
    }

    /**
     * For a given set of markets
     * Create a map to the marketdata
     * the marketdata being the periodtexts, all stocks and 
     * datedstocklist
     * 
     * @param days
     * @param markets to iterate
     * @param conf
     * @return
     * @throws Exception
     */
    
    public Map<String, MarketData> getMarketdatamap(int days,
            Set<String> markets, MyMyConfig conf) throws Exception {
        Map<String, MarketData> marketdatamap = new HashMap();
        for (String market : markets) {
            log.info("prestocks");
            List<StockItem> stocks = DbDao.getAll(market, conf);
            log.info("stocks {}", stocks.size());
            MarketData marketdata = new MarketData();
            marketdata.stocks = stocks;
            String[] periodText = DbDaoUtil.getPeriodText(market, conf);
            marketdata.periodtext = periodText;
            marketdata.meta = DbDao.getById(market, conf);
            Map<String, List<StockItem>> stockdatemap = StockUtil.splitDate(stocks);
            System.out.println("grr " + stockdatemap.keySet());
            // the main list, based on freshest or specific date.
    
            /*
             * For all days with intervals
             * Make stock lists based on the intervals
             */
    
            List<StockItem> datedstocklists[] = StockUtil.getDatedstocklists(stockdatemap, conf.getdate(), days, conf.getTableIntervalDays());
            marketdata.datedstocklists = datedstocklists;
            marketdatamap.put(market,  marketdata);
        }
        return marketdatamap;
    }

    public Set<String> getMarkets(Set<Pair<String, String>> ids) {
        Set<String> markets = new HashSet<>();
        for (Pair<String, String> idpair : ids) {
            markets.add((String) idpair.getFirst());
        }
        return markets;
    }

    /**
     * Creates map from period name to period data
     * it gets the periodtexts from the marketdata
     * creates a pair of (market id, period id)
     * and adds it to the set of pairs in perioddata
     * 
     * @param markets to iterate through
     * @param marketdatamap
     * @return period name map
     */
    
    public Map<String, PeriodData> getPerioddatamap(Set<String> markets,
            Map<String, MarketData> marketdatamap) {
        Map<String, PeriodData> periodDataMap = new HashMap();
        for (String market : markets) {
            String[] periodText = marketdatamap.get(market).periodtext;
            for (int i = 0; i < Constants.PERIODS; i++) {
                String text = periodText[i];
                Pair<String, Integer> pair = new Pair<String, Integer>(market, i);
                addPairToPeriodDataMap(periodDataMap, text, pair);
            }
            if (true) {
                Pair<String, Integer> pair = new Pair<String, Integer>(market, Constants.PRICECOLUMN);
                addPairToPeriodDataMap(periodDataMap, Constants.PRICE, pair);                
            }
            if (true) {
                Pair<String, Integer> pair = new Pair<String, Integer>(market, Constants.INDEXVALUECOLUMN);
                addPairToPeriodDataMap(periodDataMap, Constants.INDEX, pair);                
            }
        }
        return periodDataMap;
    }

    private void addPairToPeriodDataMap(Map<String, PeriodData> periodDataMap, String text,
            Pair<String, Integer> pair) {
        PeriodData perioddata = getPeriodData(periodDataMap, text);
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        pairs.add(pair);
    }

    //protected static int[] otherTableNames = { Constants.EVENT, Constants.MLTIMES }; 
    
    public static ResultItemTable createMLTimesTable(Map<Integer, ResultItemTable> tableMap) {
        ResultItemTable mlTimesTable = new ResultItemTable();
        ResultItemTableRow headrow = new ResultItemTableRow();
        headrow.add("Period");
        headrow.add("Engine");
        headrow.add("Model name");
        headrow.add("Millis");
        mlTimesTable.add(headrow);
        tableMap.put(Constants.MLTIMES, mlTimesTable);
        return mlTimesTable;
    }

    public static ResultItemTable createEventTable(Map<Integer, ResultItemTable> tableMap) {
        ResultItemTable eventTable = new ResultItemTable();
        ResultItemTableRow headrow = new ResultItemTableRow();
        headrow.add("Period");
        headrow.add("Event");
        headrow.add("Name");
        headrow.add("Id");
        eventTable.add(headrow);
        tableMap.put(Constants.EVENT, eventTable);
        return eventTable;
    }

    public void getCurrentDate(MyMyConfig conf, Map<String, List<StockItem>> stockdatemap) throws ParseException {
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        String date = null;
        TreeSet<String> set = new TreeSet<>(stockdatemap.keySet());
        List<String> list = new ArrayList<>(set);
        int size = list.size();
        if (size == 0) {
            int jj = 0;
        }
        date = list.get(size - 1);
        conf.setdate(dt.parse(date));
        log.info("mydate2 {}", conf.getdate());
    }

    public Pipeline[] getDataReaders(MyMyConfig conf, List<StockItem> stocks,
            String[] periodText,
            Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap) throws Exception {
        Pipeline[] datareaders = new Pipeline[Constants.PERIODS + 3];
        datareaders[0] = new DataReader(conf, marketdatamap, periodDataMap, Constants.INDEXVALUECOLUMN);
        datareaders[1] = new DataReader(conf, marketdatamap, periodDataMap, Constants.PRICECOLUMN);
        datareaders[2] = new ExtraReader(conf, 0);
        for (int i = 0; i < Constants.PERIODS; i++) {
            datareaders[i + 3] = new DataReader(conf, marketdatamap, periodDataMap, i);
        }
        return datareaders;
    }

    public AbstractCategory[] getCategories(MyMyConfig conf, List<StockItem> stocks,
            String[] periodText,
            Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap, List<StockItem>[] datedstocklists, Pipeline[] datareaders) throws Exception {
        AbstractCategory[] categories = new AbstractCategory[Constants.PERIODS + 2];
        categories[0] = new CategoryIndex(conf, Constants.INDEX, stocks, marketdatamap, periodDataMap, datareaders);
        categories[1] = new CategoryPrice(conf, Constants.PRICE, stocks, marketdatamap, periodDataMap, datareaders);
        for (int i = 0; i < Constants.PERIODS; i++) {
            categories[i + 2] = new CategoryPeriod(conf, i, periodText[i], stocks, marketdatamap, periodDataMap, datedstocklists, datareaders);
        }
        return categories;
    }

    public AbstractPredictor[] getPredictors(MyMyConfig conf, List<StockItem> stocks,
            String[] periodText,
            Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap, List<StockItem>[] datedstocklists, Pipeline[] datareaders, AbstractCategory[] categories) throws Exception {
        AbstractPredictor[] predictors = new AbstractPredictor[Constants.ALLPERIODS];
        //predictors[0] = new PredictorLSTM(conf, Constants.INDEX, stocks, marketdatamap, periodDataMap, datareaders, categories);
        //predictors[1] = new PredictorLSTM(conf, Constants.PRICE, stocks, marketdatamap, periodDataMap, datareaders, categories);
        MarketData marketdata = marketdatamap.get(conf.getMarket());
        for (int i = 0; i < Constants.ALLPERIODS; i++) {
            AbstractPredictor predictor = new PredictorLSTM(conf, categories[i].getTitle() + " LSTM", marketdatamap, periodDataMap, categories[i].getTitle(), categories[i].getPeriod(), categories, datareaders);
            if (predictor.isEnabled()) {
                if (MetaUtil.normalPeriod(marketdata, categories[i].getPeriod(), categories[i].getTitle())) {
                    if (predictor.hasValue()) {
                        predictors[i] = predictor;
                    }
                }
            }
        }
        return predictors;
    }

    public void calculatePredictors(AbstractPredictor[] predictors) throws Exception {
        for (AbstractPredictor predictor : predictors) {
            if (predictor != null) {
                predictor.calculate();
            }
        }
    }
}
