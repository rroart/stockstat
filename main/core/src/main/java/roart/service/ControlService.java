package roart.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.aggregator.impl.AggregatorRecommenderIndicator;
import roart.aggregator.impl.MLIndicator;
import roart.aggregator.impl.MLMACD;
import roart.aggregator.impl.RecommenderRSI;
import roart.category.AbstractCategory;
import roart.category.impl.CategoryIndex;
import roart.category.impl.CategoryPeriod;
import roart.category.impl.CategoryPrice;
import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.CategoryConstants;
import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.db.dao.DbDao;
import roart.db.dao.util.DbDaoUtil;
import roart.graphcategory.GraphCategory;
import roart.graphcategory.GraphCategoryIndex;
import roart.graphcategory.GraphCategoryPeriod;
import roart.graphcategory.GraphCategoryPeriodTopBottom;
import roart.graphcategory.GraphCategoryPrice;
import roart.indicator.util.IndicatorUtils;
import roart.model.StockItem;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.pipeline.Pipeline;
import roart.pipeline.common.aggregate.Aggregator;
import roart.pipeline.common.predictor.AbstractPredictor;
import roart.pipeline.impl.DataReader;
import roart.pipeline.impl.ExtraReader;
import roart.result.model.GUISize;
import roart.result.model.ResultItem;
import roart.result.model.ResultItemTable;
import roart.result.model.ResultItemTableRow;
import roart.service.evolution.EvolutionService;
import roart.service.util.ServiceUtil;
import roart.stockutil.StockUtil;
import roart.util.Math3Util;

public class ControlService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public List<String> getMarkets() {
        try {
            return DbDao.getMarkets();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return new ArrayList<>();
    }

    public Map<String, String> getStocks(String market, MyMyConfig conf) {
        try {
            Map<String, String> stockMap = new HashMap<>();
            List<StockItem> stocks = DbDao.getAll(market, conf);
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
     * 
     * @return the tabular result lists
     */

    public List<ResultItem> getContent(MyMyConfig conf, Map<String, Map<String, Object>> maps, List<String> disableList) {
        log.info("mydate {}", conf.getdate());
        log.info("mydate {}", conf.getDays());
        //createOtherTables();
        List<StockItem> stocks = null;
        try {
            stocks = DbDao.getAll(conf.getMarket(), conf);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        if (stocks == null) {
            return null;
        }
        log.info("stocks {}", stocks.size());
        
        Set<String> markets = new HashSet();
        markets.add(conf.getMarket());
        Integer days = conf.getDays();

        ResultItemTable table = new ResultItemTable();
        List<ResultItemTable> otherTables = new ArrayList<>();
        otherTables.add(mlTimesTable);
        otherTables.add(eventTable);

        try {
            Map<String, List<StockItem>> stockidmap = StockUtil.splitId(stocks);
            Map<String, List<StockItem>> stockdatemap = StockUtil.splitDate(stocks);
            if (conf.getdate() == null) {
                new ServiceUtil().getCurrentDate(conf, stockdatemap);
            }

            Map<String, MarketData> marketdatamap = null;
            marketdatamap = new ServiceUtil().getMarketdatamap(days, markets, conf);
            Map<String, PeriodData> periodDataMap = new ServiceUtil().getPerioddatamap(markets,
                    marketdatamap);

            Integer cat = IndicatorUtils.getWantedCategory(stocks, periodDataMap.get("cy"));
            String[] periodText = DbDaoUtil.getPeriodText(conf.getMarket(), conf);
            String catName = EvolutionService.getCatName(cat, periodText);

            if (stocks.size() != marketdatamap.get(conf.getMarket()).stocks.size()) {
                log.error("Sizes {} {}", stocks.size(), marketdatamap.get(conf.getMarket()).stocks.size());
            }
            // TODO temp hack
            Map<String, String> idNameMap = getIdNameMap(stockidmap);

            // the main list, based on freshest or specific date.

            /*
             * For all days with intervals
             * Make stock lists based on the intervals
             */

            List<StockItem>[] datedstocklists = StockUtil.getDatedstocklists(stockdatemap, conf.getdate(), 2, conf.getTableMoveIntervalDays());

            List<StockItem> datedstocks = datedstocklists[0];
            if (datedstocks == null) {
                return new ArrayList<>();
            }
            log.info("Datestocksize {}", datedstocks.size());

            Pipeline[] datareaders = new ServiceUtil().getDataReaders(conf, stocks,
                    periodText, marketdatamap, periodDataMap);

            SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
            String mydate = dt.format(conf.getdate());
            List<StockItem> dayStocks = stockdatemap.get(mydate);
            AbstractCategory[] categories = new ServiceUtil().getCategories(conf, dayStocks,
                    periodText, marketdatamap, periodDataMap, datedstocklists, datareaders);

            Aggregator[] aggregates = getAggregates(conf, stocks,
                    periodText, marketdatamap, periodDataMap, categories, datareaders, disableList, idNameMap, catName, cat);

            ResultItemTableRow headrow = createHeadRow(categories, aggregates);
            table.add(headrow);
            //log.info("sizes " + stocks.size() + " " + datedstocks.size() + " " + datedstocksoffset.size());
            createRows(conf, table, datedstocks, categories, aggregates);
            log.info("retlist2 {}",table.size());
            addOtherTables(categories);
            addOtherTables(aggregates);
            for (AbstractCategory category : categories) {
                List<AbstractPredictor> predictors = category.getPredictors();
                addOtherTables(predictors);
            }
            if (maps != null) {
                for (int i = 0; i < datareaders.length; i++) {
                    Map map = datareaders[i].getLocalResultMap();
                    maps.put(datareaders[i].pipelineName(), map);
                    log.info("pi {}", datareaders[i].pipelineName());
                }
                for (int i = 0; i < Constants.ALLPERIODS; i++) {
                    Map map = categories[i].getIndicatorLocalResultMap();
                    maps.put(categories[i].getTitle(), map);
                    log.info("ca {}", categories[i].getTitle());
                }
                for (int i = 0; i < aggregates.length; i++) {
                    log.info("ag {}", aggregates[i].getName());
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
        return retlist;
    }

    private Map<String, String> getIdNameMap(Map<String, List<StockItem>> stockidmap) {
        Map<String, String> idNameMap = new HashMap<>();
        // sort based on date
        for (String key : stockidmap.keySet()) {
            List<StockItem> stocklist = stockidmap.get(key);
            stocklist.sort(StockUtil.StockDateComparator);
            idNameMap.put(key, stocklist.get(0).getName());
        }
        return idNameMap;
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
            Aggregator[] aggregates) {
        if (conf.getMarket() == null) {
            return;
        }
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        for (StockItem stock : datedstocks) {
            ResultItemTableRow row = new ResultItemTableRow();
            row.add(stock.getId());
            row.add(stock.getName());
            row.add(dt.format(stock.getDate()));
            try {
                for (int i = 0; i < Constants.ALLPERIODS; i++) {
                    categories[i].addResultItem(row, stock);
                }
                for (int i = 0; i < aggregates.length; i++) {
                    if (aggregates[i].isEnabled()) {
                        aggregates[i].addResultItem(row, stock);
                    }
                }
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            table.add(row);
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

    private ResultItemTableRow createHeadRow(AbstractCategory[] categories, Aggregator[] aggregates) {
        ResultItemTableRow headrow = new ResultItemTableRow();
        headrow.add(Constants.IMG);
        headrow.add("Name");
        headrow.add("Date");
        for (int i = 0; i < Constants.ALLPERIODS; i++) {
            categories[i].addResultItemTitle(headrow);
        }
        for (int i = 0; i < aggregates.length; i++) {
            if (aggregates[i].isEnabled()) {
                aggregates[i].addResultItemTitle(headrow);
            }
        }
        return headrow;
    }

    private Aggregator[] getAggregates(MyMyConfig conf, List<StockItem> stocks,
            String[] periodText,
            Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap, AbstractCategory[] categories, Pipeline[] datareaders, List<String> disableList, Map<String, String> idNameMap, String catName, Integer cat) throws Exception {
        Aggregator[] aggregates = new Aggregator[4];
        aggregates[0] = new AggregatorRecommenderIndicator(conf, Constants.PRICE, stocks, marketdatamap, periodDataMap, categories, datareaders, disableList);
        aggregates[1] = new RecommenderRSI(conf, Constants.PRICE, stocks, marketdatamap, periodDataMap, categories);
        aggregates[2] = new MLMACD(conf, Constants.PRICE, stocks, periodDataMap, catName, cat, categories, idNameMap);
        aggregates[3] = new MLIndicator(conf, Constants.PRICE, marketdatamap, periodDataMap, catName, cat, categories, datareaders);
        log.info("Aggregate {}", conf.getConfigValueMap().get(ConfigConstants.MACHINELEARNING));
        log.info("Aggregate {}", conf.getConfigValueMap().get(ConfigConstants.AGGREGATORSMLMACD));
        log.info("Aggregate {}", conf.getConfigValueMap().get(ConfigConstants.INDICATORSMACD));
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
     * @param guiSize TODO
     * 
     * @return the image list
     */

    public List<ResultItem> getContentGraph(MyMyConfig conf, GUISize guiSize) {
        List<ResultItem> retlist = new ArrayList<>();
        try {
            log.info("mydate {}", conf.getdate());
            List<StockItem> stocks = DbDao.getAll(conf.getMarket(), conf);
            log.info("stocks {}", stocks.size());
            String[] periodText = DbDaoUtil.getPeriodText(conf.getMarket(), conf);
            Map<String, List<StockItem>> stockidmap = StockUtil.splitId(stocks);
            Map<String, List<StockItem>> stockdatemap = StockUtil.splitDate(stocks);
            if (conf.getdate() == null) {
                new ServiceUtil().getCurrentDate(conf, stockdatemap);
            }

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

            List<StockItem>[] datedstocklistsmove = StockUtil.getDatedstocklists(stockdatemap, conf.getdate(), days, conf.getTableMoveIntervalDays());

            List<StockItem>[][] stocklistPeriod = StockUtil.getListSorted(datedstocklistsmove, days);

            GraphCategoryPeriodTopBottom[] categories = new GraphCategoryPeriodTopBottom[Constants.PERIODS];
            for (int i = 0; i < Constants.PERIODS; i++) {
                categories[i] = new GraphCategoryPeriodTopBottom(conf, i, periodText[i], stocklistPeriod);
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
     * @param guiSize TODO
     * @return the image list
     */

    public List<ResultItem> getContentGraph(MyMyConfig conf, Set<Pair<String, String>> ids, GUISize guiSize) {
        List<ResultItem> retlist = new ArrayList<>();
        try {
            log.info("mydate {}", conf.getdate());
            int days = conf.getTableDays();
            Set<String> markets = new ServiceUtil().getMarkets(ids);
            List<StockItem> stocks = null;
            stocks = DbDao.getAll(conf.getMarket(), conf);
            if (stocks == null) {
                return new ArrayList<>();
            }
            log.info("stocks {}", stocks.size());
            Map<String, List<StockItem>> stockdatemap = StockUtil.splitDate(stocks);
            if (conf.getdate() == null) {
                new ServiceUtil().getCurrentDate(conf, stockdatemap);
            }
            Map<String, MarketData> marketdatamap = new ServiceUtil().getMarketdatamap(days,
                    markets, conf);
            Map<String, PeriodData> periodDataMap = new ServiceUtil().getPerioddatamap(markets,
                    marketdatamap);
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
            List<StockItem> stocks = DbDao.getAll(conf.getMarket(), conf);
            log.info("stocks {}", stocks.size());
            Map<String, List<StockItem>> stockidmap = StockUtil.splitId(stocks);
            Map<String, List<StockItem>> stockdatemap = StockUtil.splitDate(stocks);

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
        List<StockItem> stocks = null;
        try {
            stocks = DbDao.getAll(conf.getMarket(), conf);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        if (stocks == null) {
            return;
        }
        log.info("stocks {}", stocks.size());
        Set<String> markets = new HashSet<>();
        markets.add(conf.getMarket());

        List<ResultItemTable> otherTables = new ArrayList<>();
        otherTables.add(mlTimesTable);
        otherTables.add(eventTable);

        try {
            Map<String, List<StockItem>> stockdatemap = StockUtil.splitDate(stocks);
            List<String> dates = new ArrayList<>(stockdatemap.keySet());
            Collections.sort(dates);
            Map<String, Object> map = new HashMap<>();
            map.put(PipelineConstants.DATELIST, dates);
            maps.put(PipelineConstants.DATELIST, map);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return;
        }
    }
}
