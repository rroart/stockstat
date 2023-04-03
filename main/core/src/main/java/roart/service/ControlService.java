package roart.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.aggregator.impl.AggregatorRecommenderIndicator;
import roart.aggregator.impl.MACDBase;
import roart.aggregator.impl.MLATR;
import roart.aggregator.impl.MLCCI;
import roart.aggregator.impl.MLIndicator;
import roart.aggregator.impl.MLMACD;
import roart.aggregator.impl.MLMulti;
import roart.aggregator.impl.MLRSI;
import roart.aggregator.impl.MLSTOCH;
import roart.aggregator.impl.RecommenderRSI;
import roart.category.AbstractCategory;
import roart.category.impl.CategoryIndex;
import roart.category.impl.CategoryPeriod;
import roart.category.impl.CategoryPrice;
import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.CategoryConstants;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetCommand;
import roart.common.model.MetaItem;
import roart.common.model.StockItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.db.dao.DbDao;
import roart.etl.CleanETL;
import roart.etl.PeriodDataETL;
import roart.etl.db.Extract;
import roart.graphcategory.GraphCategory;
import roart.graphcategory.GraphCategoryIndex;
import roart.graphcategory.GraphCategoryPeriod;
import roart.graphcategory.GraphCategoryPeriodTopBottom;
import roart.graphcategory.GraphCategoryPrice;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.model.data.StockData;
import roart.pipeline.Pipeline;
import roart.pipeline.common.aggregate.Aggregator;
import roart.pipeline.common.predictor.AbstractPredictor;
import roart.pipeline.impl.DataReader;
import roart.pipeline.impl.ExtraReader;
import roart.result.model.GUISize;
import roart.result.model.ResultItem;
import roart.result.model.ResultItemTable;
import roart.result.model.ResultItemTableRow;
import roart.service.util.ServiceUtil;
import roart.stockutil.StockUtil;
import roart.util.Math3Util;

public class ControlService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static CuratorFramework curatorClient;

    public List<String> getMarkets() {
        try {
            return dbDao.getMarkets();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return new ArrayList<>();
    }

    public List<MetaItem> getMetas() {
        try {
            return dbDao.getMetas();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return new ArrayList<>();
    }

    private DbDao dbDao;

    public ControlService(DbDao dbDao) {
        super();
        this.dbDao = dbDao;
    }

    public Map<String, String> getStocks(String market, MyMyConfig conf) {
        try {
            Map<String, String> stockMap = new HashMap<>();
            List<StockItem> stocks = dbDao.getAll(market, conf);
            stocks.remove(null);
            for (StockItem stock : stocks) {
                stockMap.put(stock.getId(), stock.getName());
            }
            return stockMap;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    Map<Integer, ResultItemTable> otherTableMap = new HashMap<>();

    ResultItemTable mlTimesTable = ServiceUtil.createMLTimesTable(otherTableMap);
    ResultItemTable eventTable = ServiceUtil.createEventTable(otherTableMap);

    //protected static int[] otherTableNames = { Constants.EVENT, Constants.MLTIMES }; 

    /**
     * Create result lists
     * @param maps 
     * @param neuralnetcommand TODO
     * @return the tabular result lists
     */

    public List<ResultItem> getContent(MyMyConfig conf, Map<String, Map<String, Object>> maps, List<String> disableList, NeuralNetCommand neuralnetcommand) {
        log.info("mydate {}", conf.getdate());
        log.info("mydate {}", conf.getDays());
        //createOtherTables();
        StockData stockData = new Extract(dbDao).getStockData(conf);
        if (stockData == null) {
            return new ArrayList<>();
        }
        
        ResultItemTable table = new ResultItemTable();
        List<ResultItemTable> otherTables = new ArrayList<>();
        otherTables.add(mlTimesTable);
        otherTables.add(eventTable);

        try {
            Pipeline[] datareaders = new ServiceUtil().getDataReaders(conf, stockData.periodText,
                    stockData.marketdatamap, stockData, dbDao);

            SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
            String mydate = dt.format(conf.getdate());
            int dateIndex = TimeUtil.getIndexEqualBefore(stockData.stockdates, mydate);
            if (dateIndex >= 0) {
                mydate = stockData.stockdates.get(dateIndex);
            }
            List<StockItem> dayStocks = stockData.stockdatemap.get(mydate);
            AbstractCategory[] categories = new ServiceUtil().getCategories(conf, dayStocks,
                    stockData.periodText, datareaders);
            AbstractPredictor[] predictors = new ServiceUtil().getPredictors(conf, stockData.periodText,
                    stockData.marketdatamap, stockData.datedstocklists, datareaders, categories, neuralnetcommand);
            //new ServiceUtil().createPredictors(categories);
            new ServiceUtil().calculatePredictors(predictors);
            
            Aggregator[] aggregates = getAggregates(conf, stockData.periodText,
                    stockData.marketdatamap, categories, datareaders, disableList, stockData.idNameMap, stockData.catName, stockData.cat, neuralnetcommand);

            ResultItemTableRow headrow = createHeadRow(categories, predictors, aggregates);
            table.add(headrow);
            //log.info("sizes " + stocks.size() + " " + datedstocks.size() + " " + datedstocksoffset.size());
            createRows(conf, table, stockData.datedstocks, categories, predictors, aggregates);
            log.info("retlist2 {}",table.size());
            cleanRows(headrow, table);
            addOtherTables(categories);
            addOtherTables(predictors);
            addOtherTables(aggregates);
            /*
            for (AbstractCategory category : categories) {
                List<AbstractPredictor> predictors = category.getPredictors();
                addOtherTables(predictors);
            }
            */
            if (maps != null) {
                Map<String, Object> aMap = new HashMap<>();
                aMap.put(PipelineConstants.WANTEDCAT, stockData.cat);
                maps.put(PipelineConstants.META, aMap);
                
                for (int i = 0; i < datareaders.length; i++) {
                    Map map = datareaders[i].getLocalResultMap();
                    maps.put(datareaders[i].pipelineName(), map);
                    log.debug("pi {}", datareaders[i].pipelineName());
                }
                for (int i = 0; i < Constants.ALLPERIODS; i++) {
                    Map map = categories[i].getIndicatorLocalResultMap();
                    maps.put(categories[i].getTitle(), map);
                    log.debug("ca {}", categories[i].getTitle());
                }
                for (int i = 0; i < Constants.ALLPERIODS; i++) {
                    if (predictors[i] == null) {
                        continue;
                    }
                    Map map = predictors[i].getLocalResultMap();
                    maps.put(predictors[i].getName(), map);
                    log.debug("ca {}", predictors[i].getName());
                }
                for (int i = 0; i < aggregates.length; i++) {
                    if (!aggregates[i].isEnabled()) {
                        continue;
                    }
                    log.debug("ag {}", aggregates[i].getName());
                    Map map = aggregates[i].getLocalResultMap();
                    maps.put(aggregates[i].getName(), map);
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<ResultItem> retlist = new ArrayList<>();
        retlist.add(table);
        for (ResultItemTable list : otherTables) {
            retlist.add(list);
        }
        new CleanETL().fixmap((Map) maps);
        printmap(maps, 0);
        return retlist;
    }
    
    public void printmap(Object o, int i) {
        if (o == null) {
            return;
        }
        //System.out.println("" + i + " " + o.hashCode());
        Map<String, Object> m = (Map<String, Object>) o;
        for (Entry<String, Object> e : m.entrySet()) {
            Object value = e.getValue();
            if (value instanceof Map) {
                System.out.println("" + i + " " + e.getKey() + " " + value.hashCode());
                printmap((Map<String, Object>) value, i + 1);
            } else {
                if (value == null) {
                    System.out.println("" + i + " " + e.getKey() + " " + null);
                    //System.out.println(" v " + null);
                }
            }
        }
    }

    private void cleanRows(ResultItemTableRow headrow, ResultItemTable table) {
        ResultItemTableRow sum = null;
        if (table.rows.size() > 1) {
            sum = table.rows.get(1);
            log.info("Cols {}", sum.cols.size());
        } else {
            return;
        }
        for (int j = 2; j < table.rows.size(); j++) {
            ResultItemTableRow row = table.rows.get(j);
            for (int i = 0; i < sum.cols.size(); i++) {
                if (sum.cols.get(i) == null) {
                    sum.cols.set(i, row.cols.get(i));
                }
            }
        }
        for (int i = sum.cols.size() - 1; i >= 0; i--) {
            if (sum.cols.get(i) == null) {
                //headrow.cols.remove(i);
                for (ResultItemTableRow row : table.rows) {
                    row.cols.remove(i);
                }
            }
        }
        if (table.rows.size() > 0) {
            log.info("Cols new {}",table.rows.get(0).cols.size());
        }
    }

    private void addOtherTables(Aggregator[] aggregates) {
        for (int i = 0; i < aggregates.length; i++) {
            Map<Integer, List<ResultItemTableRow>> tableMap = aggregates[i].otherTables();
            if (tableMap == null) {
                continue;
            }
            for (Entry<Integer, List<ResultItemTableRow>> entry : tableMap.entrySet()) {
                List<ResultItemTableRow> resultItems = entry.getValue();
                ResultItemTable otherTable = otherTableMap.get(entry.getKey());
                for (ResultItemTableRow row : resultItems) {
                    otherTable.add(row);
                }
            }
        }
    }

    private void addOtherTables(List<AbstractPredictor> predictors) {
        for (AbstractPredictor predictor : predictors) {
            if (predictor == null) {
                continue;
            }
            Map<Integer, List<ResultItemTableRow>> tableMap = predictor.otherTables();
            if (tableMap == null) {
                continue;
            }
            for (Entry<Integer, List<ResultItemTableRow>> entry : tableMap.entrySet()) {
                List<ResultItemTableRow> resultItems = entry.getValue();
                ResultItemTable otherTable = otherTableMap.get(entry.getKey());
                for (ResultItemTableRow row : resultItems) {
                    otherTable.add(row);
                }
            }
        }
    }

    private void createRows(MyMyConfig conf, ResultItemTable table, List<StockItem> datedstocks, AbstractCategory[] categories,
            AbstractPredictor[] predictors, Aggregator[] aggregates) {
        if (conf.getMarket() == null) {
            return;
        }
        int cols = table.get(0).cols.size();
        Set set = new HashSet<>();
        set.addAll(table.get(0).cols);
        int cols2 = set.size();
        List list = table.get(0).cols;
        /*
        for (int i = 0; i < list.size(); i++) {
            for (int j = i+1; j <list.size() ; j++) {
                if(list.get(i).equals(list.get(j))){
                    System.out.println(list.get(i));
                }
            }
        }
        */
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        for (StockItem stock : datedstocks) {
            ResultItemTableRow row = new ResultItemTableRow();
            row.add(stock.getId());
            row.add(stock.getIsin());
            row.add(stock.getName());
            row.add(dt.format(stock.getDate()));
            try {
                for (int i = 0; i < Constants.ALLPERIODS; i++) {
                    categories[i].addResultItem(row, stock);
                }
                for (AbstractPredictor predictor : predictors) {
                    if (predictor != null) {
                        row.addarr(predictor.getResultItem(stock));
                    }
                }
                for (int i = 0; i < aggregates.length; i++) {
                    if (aggregates[i].isEnabled()) {
                        aggregates[i].addResultItem(row, stock);
                    }
                }
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            if (cols == row.cols.size()) {
                table.add(row);
            } else {
                int jj = 0;
            }
        }
    }

    private void addOtherTables(AbstractCategory[] categories) {
        for (int i = 0; i < Constants.ALLPERIODS; i++) {
            Map<Integer, List<ResultItemTableRow>> tableMap = categories[i].otherTables();
            for (Entry<Integer, List<ResultItemTableRow>> entry : tableMap.entrySet()) {
                List<ResultItemTableRow> resultItems = entry.getValue();
                ResultItemTable otherTable = otherTableMap.get(entry.getKey());
                for (ResultItemTableRow row : resultItems) {
                    otherTable.add(row);
                }
            }
        }
    }

    private void addOtherTables(AbstractPredictor[] predictors) {
        for (int i = 0; i < Constants.ALLPERIODS; i++) {
            if (predictors[i] == null) {
                continue;
            }
            Map<Integer, List<ResultItemTableRow>> tableMap = predictors[i].otherTables();
            for (Entry<Integer, List<ResultItemTableRow>> entry : tableMap.entrySet()) {
                List<ResultItemTableRow> resultItems = entry.getValue();
                ResultItemTable otherTable = otherTableMap.get(entry.getKey());
                for (ResultItemTableRow row : resultItems) {
                    otherTable.add(row);
                }
            }
        }
    }

    private ResultItemTableRow createHeadRow(AbstractCategory[] categories, AbstractPredictor[] predictors, Aggregator[] aggregates) {
        ResultItemTableRow headrow = new ResultItemTableRow();
        headrow.add(Constants.IMG);
        headrow.add("ISIN");
        headrow.add("Name");
        headrow.add("Date");
        for (int i = 0; i < Constants.ALLPERIODS; i++) {
            categories[i].addResultItemTitle(headrow);
        }
        for (AbstractPredictor predictor : predictors) {
            if (predictor != null) {
                headrow.addarr(predictor.getResultItemTitle());
            }
        }
        for (int i = 0; i < aggregates.length; i++) {
            if (aggregates[i].isEnabled()) {
                aggregates[i].addResultItemTitle(headrow);
            }
        }
        List<Object> duplicates = headrow.cols.stream()
                .filter(e -> Collections.frequency(headrow.cols, e) > 1)
                .distinct()
                .collect(Collectors.toList());
        if (duplicates.size() > 0) {
            log.error("Duplicates {}", duplicates);
        }
        return headrow;
    }

    private Aggregator[] getAggregates(MyMyConfig conf, String[] periodText,
            Map<String, MarketData> marketdatamap,
            AbstractCategory[] categories,
            Pipeline[] datareaders, List<String> disableList, Map<String, String> idNameMap, String catName, Integer cat, NeuralNetCommand neuralnetcommand) throws Exception {
        Aggregator[] aggregates = new Aggregator[10];
        aggregates[0] = new MACDBase(conf, catName, catName, cat, categories, idNameMap, datareaders);
        aggregates[1] = new AggregatorRecommenderIndicator(conf, catName, marketdatamap, categories, datareaders, disableList);
        aggregates[2] = new RecommenderRSI(conf, catName, marketdatamap, categories);
        aggregates[3] = new MLMACD(conf, catName, catName, cat, categories, idNameMap, datareaders, neuralnetcommand);
        aggregates[4] = new MLRSI(conf, catName, catName, cat, categories, idNameMap, datareaders, neuralnetcommand);
        aggregates[5] = new MLATR(conf, catName, catName, cat, categories, idNameMap, datareaders, neuralnetcommand);
        aggregates[6] = new MLCCI(conf, catName, catName, cat, categories, idNameMap, datareaders, neuralnetcommand);
        aggregates[7] = new MLSTOCH(conf, catName, catName, cat, categories, idNameMap, datareaders, neuralnetcommand);
        aggregates[8] = new MLMulti(conf, catName, catName, cat, categories, idNameMap, datareaders, neuralnetcommand);
        aggregates[9] = new MLIndicator(conf, catName, marketdatamap, catName, cat, categories, datareaders, neuralnetcommand);
        log.info("Aggregate {}", conf.getConfigValueMap().get(ConfigConstants.MACHINELEARNING));
        log.info("Aggregate {}", conf.getConfigValueMap().get(ConfigConstants.AGGREGATORSMLMACD));
        log.info("Aggregate {}", conf.getConfigValueMap().get(ConfigConstants.INDICATORSMACD));
        log.info("Aggregate {}", conf.getConfigValueMap().get(ConfigConstants.INDICATORSRSI));
        log.info("Aggregate {}", conf.getConfigValueMap().get(ConfigConstants.INDICATORS));
        return aggregates;
    }

    private GraphCategory[] getGraphCategories(MyMyConfig conf,
            String[] periodTextNot,
            Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap) {
        GraphCategory[] categories = new GraphCategory[Constants.PERIODS + 2];
        categories[0] = new GraphCategoryIndex(conf, Constants.INDEX, marketdatamap, periodDataMap);
        categories[1] = new GraphCategoryPrice(conf, Constants.PRICE, marketdatamap, periodDataMap);
        int i = 2;
        Set<String> keys = new TreeSet<>(periodDataMap.keySet());
        keys.remove(Constants.INDEX);
        keys.remove(Constants.PRICE);
        for (String periodText : keys) {
            categories[i ++] = new GraphCategoryPeriod(conf, i, periodText, marketdatamap, periodDataMap);
        }
        return categories;
    }

    /**
     * Create result graphs
     * @param guiSize gui size
     * 
     * @return the image list
     */

    public List<ResultItem> getContentGraph(MyMyConfig conf, GUISize guiSize) {
        List<ResultItem> retlist = new ArrayList<>();
        try {
            log.info("mydate {}", conf.getdate());
            log.info("mydate {}", conf.getDays());
            StockData stockData = new Extract(dbDao).getStockData(conf);
            if (stockData == null) {
                return new ArrayList<>();
            }
            // sort based on date
            for (Entry<String, List<StockItem>> entry : stockData.stockidmap.entrySet()) {
                List<StockItem> stocklist = entry.getValue();
                stocklist.sort(StockUtil.StockDateComparator);
            }

            List<StockItem>[] datedstocklistsmove = StockUtil.getDatedstocklists(stockData.stockdatemap, conf.getdate(), stockData.days, conf.getTableMoveIntervalDays());

            List<StockItem>[][] stocklistPeriod = StockUtil.getListSorted(datedstocklistsmove, stockData.days);
            
            GraphCategoryPeriodTopBottom[] categories = new GraphCategoryPeriodTopBottom[Constants.PERIODS];
            for (int i = 0; i < Constants.PERIODS; i++) {
                categories[i] = new GraphCategoryPeriodTopBottom(conf, i, stockData.periodText[i], stocklistPeriod);
            }

            for (GraphCategory category : categories) {
                category.addResult(retlist, null, guiSize);
            }

        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        log.info("retlist size {}", retlist.size());
        return retlist;
    }

    /**
     * Create result graphs for one
     * 
     * myid the id of the unit
     * @param guiSize gui size
     * @return the image list
     */

    public List<ResultItem> getContentGraph(MyMyConfig conf, Set<Pair<String, String>> ids, GUISize guiSize) {
        List<ResultItem> retlist = new ArrayList<>();
        try {
            log.info("mydate {}", conf.getdate());
            Map<String, MarketData> marketdatamap = new HashMap<>();
            Map<String, PeriodData> periodDataMap = new HashMap<>();
            Map<String, StockData> stockDataMap = new HashMap<>();
            Set<String> markets = new ServiceUtil().getMarkets(ids);
            for (String market : markets) {
                StockData stockData = new Extract(dbDao).getStockData(conf, market);
                if (stockData == null) {
                    return new ArrayList<>();
                }
                stockDataMap.put(market, stockData);
                marketdatamap.putAll(stockData.marketdatamap);
                periodDataMap.putAll(new PeriodDataETL().getPerioddatamap(markets,
                        marketdatamap));
            }
            GraphCategory[] categories = getGraphCategories(conf, null, marketdatamap, periodDataMap);

            for (int i = 0; i < Constants.ALLPERIODS; i++) {
                categories[i].addResult(retlist, ids, guiSize);
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }

        log.info("retlist size {}", retlist.size());
        return retlist;
    }

    /**
     * Create stat result lists
     * 
     * @return the tabular result lists
     */

    public List<ResultItem> getContentStat(MyMyConfig conf) {
        List<ResultItem> retList = new ArrayList<>();
        ResultItemTable table = new ResultItemTable();
        ResultItemTableRow row = new ResultItemTableRow();
        row.add(Constants.IMG);
        row.add("Name 1");
        row.add("Name 2");
        row.add("Period");
        row.add("Size");
        row.add("Paired t");
        row.add("P-value");
        row.add("Alpha 0.05");
        row.add("Paired t (e)");
        row.add("P-value (e)");
        row.add("Alpha 0.05 (e)");
        row.add("Spearman (e)");
        row.add("Kendall (e)");
        row.add("Pearson (e)");
        table.add(row);
        try {
            List<StockItem> stocks = dbDao.getAll(conf.getMarket(), conf);
            log.info("stocks {}", stocks.size());
            Map<String, List<StockItem>> stockidmap = StockUtil.splitId(stocks);
            Map<String, List<StockItem>> stockdatemap = StockUtil.splitDate(stocks);
            stockdatemap = StockUtil.filterFew(stockdatemap, conf.getFilterDate());

            // sort based on date
            for (Entry<String, List<StockItem>> entry : stockidmap.entrySet()) {
                List<StockItem> stocklist = entry.getValue();
                stocklist.sort(StockUtil.StockDateComparator);
            }

            int days = conf.getTableDays();

            // the main list, based on freshest or specific date.

            /*
             * For all days with intervals
             * Make stock lists based on the intervals
             */

            List<StockItem> datedstocklists[] = StockUtil.getDatedstocklists(stockdatemap, conf.getdate(), days, conf.getTableIntervalDays());

            List<StockItem> datedstocks = datedstocklists[0];
            if (datedstocks == null) {
                return new ArrayList<>();
            }
            Math3Util.getStats(table, conf.getdate(), days, stockidmap, stockdatemap);

            log.info("retlist {}",retList.size());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        retList.add(table);
        return retList;
    }

    public void getDates(MyMyConfig conf, Map<String, Map<String, Object>> maps) {
        Map<String, Object> aMap = new HashMap<>();
        /*
        aMap.put(ConfigConstants.MACHINELEARNING, false);
        aMap.put(ConfigConstants.AGGREGATORS, false);
        aMap.put(ConfigConstants.INDICATORS, false);
        aMap.put(ConfigConstants.MISCTHRESHOLD, null);
        */        
        aMap.put(ConfigConstants.MISCMYTABLEDAYS, 0);
        aMap.put(ConfigConstants.MISCMYDAYS, 0);
        /*
        aMap.put(ConfigConstants.MISCPERCENTIZEPRICEINDEX, true);
        aMap.put(ConfigConstants.MISCINTERPOLATIONMETHOD, market.getConfig().getInterpolate());
        aMap.put(ConfigConstants.MISCINTERPOLATIONLASTNULL, Boolean.TRUE);
        aMap.put(ConfigConstants.MISCMERGECY, false);        
        conf.setConfigValueMap(new HashMap<>(conf.getConfigValueMap()));
        */
        conf.getConfigValueMap().putAll(aMap);
        StockData stockData = new Extract(dbDao).getStockData(conf);
        if (stockData != null) {
            Map<String, Object> map = new HashMap<>();
            map.put(PipelineConstants.DATELIST, stockData.stockdates);
            maps.put(PipelineConstants.DATELIST, map);
            return;
        }
        
        List<String> dates = null;
        try {
            if ("0".equals(conf.getMarket())) {
                int jj = 0;
            }
            dates = dbDao.getDates(conf.getMarket(), conf);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        if (dates == null) {
            return;
        }
        log.info("stocks {}", dates.size());
        Set<String> markets = new HashSet<>();
        markets.add(conf.getMarket());

        try {
            Collections.sort(dates);
            Map<String, Object> map = new HashMap<>();
            map.put(PipelineConstants.DATELIST, dates);
            maps.put(PipelineConstants.DATELIST, map);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return;
        }
    }
    
    public static void configCurator(MyMyConfig conf) {
        if (true) {
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);        
            String zookeeperConnectionString = conf.getZookeeper();
            if (curatorClient == null) {
                curatorClient = CuratorFrameworkFactory.newClient(zookeeperConnectionString, retryPolicy);
                curatorClient.start();
            }
        }
    }

}
