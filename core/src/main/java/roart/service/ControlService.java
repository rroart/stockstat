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

import roart.aggregate.Aggregator;
import roart.aggregate.AggregatorRecommenderIndicator;
import roart.aggregate.DataReader;
import roart.aggregate.ExtraReader;
import roart.aggregate.MLIndicator;
import roart.aggregate.MLMACD;
import roart.aggregate.RecommenderRSI;
import roart.category.Category;
import roart.category.CategoryConstants;
import roart.category.CategoryIndex;
import roart.category.CategoryPeriod;
import roart.category.CategoryPrice;
import roart.config.ConfigConstants;
import roart.config.MyMyConfig;
import roart.evaluation.IndicatorEvaluation;
import roart.evaluation.Recommend;
import roart.evolution.Individual;
import roart.evolution.OrdinaryEvolution;
import roart.graphcategory.GraphCategory;
import roart.graphcategory.GraphCategoryIndex;
import roart.graphcategory.GraphCategoryPeriod;
import roart.graphcategory.GraphCategoryPeriodTopBottom;
import roart.graphcategory.GraphCategoryPrice;
import roart.indicator.Indicator;
import roart.indicator.IndicatorUtils;
import roart.model.GUISize;
import roart.model.MetaItem;
import roart.model.ResultItem;
import roart.model.ResultItemTable;
import roart.model.ResultItemTableRow;
import roart.model.StockItem;
import roart.pipeline.Pipeline;
import roart.pipeline.PipelineConstants;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.Math3Util;
import roart.util.MetaDao;
import roart.util.PeriodData;
import roart.util.StockUtil;
import roart.util.TaUtil;

public class ControlService {
    private static Logger log = LoggerFactory.getLogger(ControlService.class);

    // TODO temp hack
    private static Map<String, String> idNameMap;

    public static String getName(String id) {
        if (idNameMap == null) {
            return id;
        }
        String name = idNameMap.get(id);
        if (name == null) {
            name = id;
        }
        return name;
    }

    public List<String> getMarkets() {
        try {
            return StockItem.getMarkets();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return new ArrayList<>();
    }

    public Map<String, String> getStocks(String market, MyMyConfig conf) {
        try {
            Map<String, String> stockMap = new HashMap<>();
            List<StockItem> stocks = StockItem.getAll(market, conf);
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

    ResultItemTable mlTimesTable = null;
    ResultItemTable eventTable = null;

    Map<Integer, ResultItemTable> otherTableMap = new HashMap<>();

    public static final int EVENT = 0;
    public static final int MLTIMES = 1;

    protected static int[] otherTableNames = { EVENT, MLTIMES }; 

    public void createOtherTables() {
        mlTimesTable = new ResultItemTable();
        eventTable = new ResultItemTable();
        ResultItemTableRow headrow = new ResultItemTableRow();
        headrow.add("Period");
        headrow.add("Engine");
        headrow.add("Model name");
        headrow.add("Millis");
        mlTimesTable.add(headrow);
        headrow = new ResultItemTableRow();
        headrow.add("Period");
        headrow.add("Event");
        headrow.add("Name");
        headrow.add("Id");
        eventTable.add(headrow);
        otherTableMap.put(EVENT, eventTable);
        otherTableMap.put(MLTIMES, mlTimesTable);
    }

    /**
     * Create result lists
     * @param maps 
     * 
     * @return the tabular result lists
     */

    public List<ResultItem> getContent(MyMyConfig conf, Map<String, Map<String, Object>> maps, List<String> disableList) {
        log.info("mydate {}", conf.getdate());
        log.info("mydate {}", conf.getDays());
        createOtherTables();
        List<StockItem> stocks = null;
        try {
            stocks = StockItem.getAll(conf.getMarket(), conf);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        if (stocks == null) {
            return null;
        }
        log.info("stocks {}", stocks.size());
        String[] periodText = getPeriodText(conf.getMarket(), conf);
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
                getCurrentDate(conf, stockdatemap);
            }

            Map<String, MarketData> marketdatamap = null;
            marketdatamap = getMarketdatamap(days, markets, conf);
            Map<String, PeriodData> periodDataMap = getPerioddatamap(markets,
                    marketdatamap);

            if (stocks.size() != marketdatamap.get(conf.getMarket()).stocks.size()) {
                log.error("Sizes {} {}", stocks.size(), marketdatamap.get(conf.getMarket()).stocks.size());
            }
            idNameMap = new HashMap<>();
            // sort based on date
            for (String key : stockidmap.keySet()) {
                List<StockItem> stocklist = stockidmap.get(key);
                stocklist.sort(StockUtil.StockDateComparator);
                idNameMap.put(key, stocklist.get(0).getName());
            }

            // the main list, based on freshest or specific date.

            /*
             * For all days with intervals
             * Make stock lists based on the intervals
             */

            List<StockItem>[] datedstocklists = StockUtil.getDatedstocklists(stockdatemap, conf.getdate(), 2, conf.getTableMoveIntervalDays());

            List<StockItem>[][] stocklistPeriod = StockUtil.getListSorted(datedstocklists, 2);
            Map<String, Integer>[][] periodmaps = StockUtil.getListMove(datedstocklists, 2, stocklistPeriod);
            Map<String, Integer>[] periodmap = periodmaps[0];

            List<StockItem> datedstocks = datedstocklists[0];
            if (datedstocks == null) {
                return new ArrayList<>();
            }
            log.info("Datestocksize {}", datedstocks.size());

            Pipeline[] datareaders = getDataReaders(conf, stocks,
                    periodText, marketdatamap, periodDataMap, periodmap);

            Category[] categories = getCategories(conf, stocks,
                    periodText, marketdatamap, periodDataMap, periodmap, datareaders);

            Aggregator[] aggregates = getAggregates(conf, stocks,
                    periodText, marketdatamap, periodDataMap, periodmap, categories, datareaders, disableList);

            ResultItemTableRow headrow = createHeadRow(categories, aggregates);
            table.add(headrow);
            //log.info("sizes " + stocks.size() + " " + datedstocks.size() + " " + datedstocksoffset.size());
            createRows(conf, table, datedstocks, categories, aggregates);
            log.info("retlist2 {}",table.size());
            addOtherTables(categories);
            addOtherTables(aggregates);
            if (maps != null) {
                for (int i = 0; i < datareaders.length; i++) {
                    Map map = datareaders[i].getLocalResultMap();
                    maps.put(datareaders[i].pipelineName(), map);
                    log.info("pi {}", datareaders[i].pipelineName());
                }
                for (int i = 0; i < StockUtil.ALLPERIODS; i++) {
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

    private void createRows(MyMyConfig conf, ResultItemTable table, List<StockItem> datedstocks, Category[] categories,
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
                for (int i = 0; i < StockUtil.ALLPERIODS; i++) {
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

    private void addOtherTables(Category[] categories) {
        for (int i = 0; i < StockUtil.ALLPERIODS; i++) {
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

    private ResultItemTableRow createHeadRow(Category[] categories, Aggregator[] aggregates) {
        ResultItemTableRow headrow = new ResultItemTableRow();
        headrow.add(Constants.IMG);
        headrow.add("Name");
        headrow.add("Date");
        for (int i = 0; i < StockUtil.ALLPERIODS; i++) {
            categories[i].addResultItemTitle(headrow);
        }
        for (int i = 0; i < aggregates.length; i++) {
            if (aggregates[i].isEnabled()) {
                aggregates[i].addResultItemTitle(headrow);
            }
        }
        return headrow;
    }

    private void getCurrentDate(MyMyConfig conf, Map<String, List<StockItem>> stockdatemap) throws ParseException {
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        String date = null;
        TreeSet<String> set = new TreeSet<>(stockdatemap.keySet());
        List<String> list = new ArrayList<>(set);
        int size = list.size();
        date = list.get(size - 1);
        conf.setdate(dt.parse(date));
        log.info("mydate2 {}", conf.getdate());
    }

    private Aggregator[] getAggregates(MyMyConfig conf, List<StockItem> stocks,
            String[] periodText,
            Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap, Category[] categories, Pipeline[] datareaders, List<String> disableList) throws Exception {
        Aggregator[] aggregates = new Aggregator[4];
        aggregates[0] = new AggregatorRecommenderIndicator(conf, Constants.PRICE, stocks, marketdatamap, periodDataMap, periodmap, categories, datareaders, disableList);
        aggregates[1] = new RecommenderRSI(conf, Constants.PRICE, stocks, marketdatamap, periodDataMap, periodmap, categories);
        aggregates[2] = new MLMACD(conf, Constants.PRICE, stocks, periodDataMap, CategoryConstants.PRICE, 0, categories);
        aggregates[3] = new MLIndicator(conf, Constants.PRICE, marketdatamap, periodDataMap, CategoryConstants.PRICE, 0, categories, datareaders);
        log.info("Aggregate {}", conf.configValueMap.get(ConfigConstants.MACHINELEARNING));
        log.info("Aggregate {}", conf.configValueMap.get(ConfigConstants.AGGREGATORSMLMACD));
        log.info("Aggregate {}", conf.configValueMap.get(ConfigConstants.INDICATORSMACD));
        log.info("Aggregate {}", conf.configValueMap.get(ConfigConstants.INDICATORS));
        return aggregates;
    }

    private Pipeline[] getDataReaders(MyMyConfig conf, List<StockItem> stocks,
            String[] periodText,
            Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap) throws Exception {
        Pipeline[] datareaders = new Pipeline[StockUtil.PERIODS + 3];
        datareaders[0] = new DataReader(conf, marketdatamap, periodDataMap, periodmap, Constants.INDEXVALUECOLUMN);
        datareaders[1] = new DataReader(conf, marketdatamap, periodDataMap, periodmap, Constants.PRICECOLUMN);
        datareaders[2] = new ExtraReader(conf, 0);
        for (int i = 0; i < StockUtil.PERIODS; i++) {
            datareaders[i + 3] = new DataReader(conf, marketdatamap, periodDataMap, periodmap, i);
        }
        return datareaders;
    }

    private Category[] getCategories(MyMyConfig conf, List<StockItem> stocks,
            String[] periodText,
            Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap, Pipeline[] datareaders) throws Exception {
        Category[] categories = new Category[StockUtil.PERIODS + 2];
        categories[0] = new CategoryIndex(conf, Constants.INDEX, stocks, marketdatamap, periodDataMap, periodmap, datareaders);
        categories[1] = new CategoryPrice(conf, Constants.PRICE, stocks, marketdatamap, periodDataMap, periodmap, datareaders);
        for (int i = 0; i < StockUtil.PERIODS; i++) {
            categories[i + 2] = new CategoryPeriod(conf, i, periodText[i], stocks, marketdatamap, periodDataMap, periodmap, datareaders);
        }
        return categories;
    }

    private GraphCategory[] getGraphCategories(MyMyConfig conf,
            String[] periodTextNot,
            Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap) {
        GraphCategory[] categories = new GraphCategory[StockUtil.PERIODS + 2];
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
     * Get the period field text based on the eventual metadata
     * 
     * @return the period text fields
     * @param market
     */

    public static String[] getPeriodText(String market, MyMyConfig conf) {
        String[] periodText = { "Period1", "Period2", "Period3", "Period4", "Period5", "Period6", "Period7", "Period8", "Period9" };
        MetaItem meta = null;
        try {
            meta = MetaItem.getById(market, conf);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        try {
            if (meta != null) {
                for (int i = 0; i < StockUtil.PERIODS; i++) {
                    if (MetaDao.getPeriod(meta, i) != null) {
                        periodText[i] = MetaDao.getPeriod(meta, i);
                    }
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return periodText;
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
            List<StockItem> stocks = StockItem.getAll(conf.getMarket(), conf);
            log.info("stocks {}", stocks.size());
            String[] periodText = getPeriodText(conf.getMarket(), conf);
            Map<String, List<StockItem>> stockidmap = StockUtil.splitId(stocks);
            Map<String, List<StockItem>> stockdatemap = StockUtil.splitDate(stocks);
            if (conf.getdate() == null) {
                getCurrentDate(conf, stockdatemap);
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

            GraphCategoryPeriodTopBottom[] categories = new GraphCategoryPeriodTopBottom[StockUtil.PERIODS];
            for (int i = 0; i < StockUtil.PERIODS; i++) {
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
            Set<String> markets = getMarkets(ids);
            List<StockItem> stocks = null;
            stocks = StockItem.getAll(conf.getMarket(), conf);
            if (stocks == null) {
                return new ArrayList<>();
            }
            log.info("stocks {}", stocks.size());
            Map<String, List<StockItem>> stockdatemap = StockUtil.splitDate(stocks);
            if (conf.getdate() == null) {
                getCurrentDate(conf, stockdatemap);
            }
            Map<String, MarketData> marketdatamap = getMarketdatamap(days,
                    markets, conf);
            Map<String, PeriodData> periodDataMap = getPerioddatamap(markets,
                    marketdatamap);
            GraphCategory[] categories = getGraphCategories(conf, null, marketdatamap, periodDataMap);

            for (int i = 0; i < StockUtil.ALLPERIODS; i++) {
                categories[i].addResult(retlist, ids, guiSize);
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }

        log.info("retlist size {}", retlist.size());
        return retlist;
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

    private Map<String, PeriodData> getPerioddatamap(Set<String> markets,
            Map<String, MarketData> marketdatamap) {
        Map<String, PeriodData> periodDataMap = new HashMap();
        for (String market : markets) {
            String[] periodText = marketdatamap.get(market).periodtext;
            for (int i = 0; i < StockUtil.PERIODS; i++) {
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

    /**
     * return perioddata for periodtext, or new one if not existing
     *
     * @param periodDataMap map from periodtext to perioddata
     * @param text periodtext
     * @return perioddata
     */

    private PeriodData getPeriodData(Map<String, PeriodData> periodDataMap, String text) {
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

    private Map<String, MarketData> getMarketdatamap(int days,
            Set<String> markets, MyMyConfig conf) throws Exception {
        Map<String, MarketData> marketdatamap = new HashMap();
        for (String market : markets) {
            log.info("prestocks");
            List<StockItem> stocks = StockItem.getAll(market, conf);
            log.info("stocks {}", stocks.size());
            MarketData marketdata = new MarketData();
            marketdata.stocks = stocks;
            String[] periodText = getPeriodText(market, conf);
            marketdata.periodtext = periodText;
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

    private Set<String> getMarkets(Set<Pair<String, String>> ids) {
        Set<String> markets = new HashSet<>();
        for (Pair<String, String> idpair : ids) {
            markets.add((String) idpair.getFirst());
        }
        return markets;
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
            List<StockItem> stocks = StockItem.getAll(conf.getMarket(), conf);
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

    public List<ResultItem> getTestRecommender(MyMyConfig conf, List<String> disableList) {
        log.info("mydate {}", conf.getdate());
        log.info("mydate {}", conf.getDays());
        createOtherTables();
        List<StockItem> stocks = null;
        try {
            stocks = StockItem.getAll(conf.getMarket(), conf);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        if (stocks == null) {
            return new ArrayList<>();
        }
        log.info("stocks {}", stocks.size());
        Set<String> markets = new HashSet<>();
        markets.add(conf.getMarket());
        Integer days = conf.getDays();

        List<ResultItemTable> otherTables = new ArrayList<>();
        otherTables.add(mlTimesTable);
        otherTables.add(eventTable);

        ResultItemTable table = new ResultItemTable();
        ResultItemTableRow headrow = new ResultItemTableRow();
        headrow.add("Config");
        headrow.add("Old value");
        headrow.add("New value");
        table.add(headrow);

        try {
            Map<String, List<StockItem>> stockidmap = StockUtil.splitId(stocks);
            Map<String, List<StockItem>> stockdatemap = StockUtil.splitDate(stocks);
            log.info("datemapsize {}", stockdatemap.size());
            if (conf.getdate() == null) {
                getCurrentDate(conf, stockdatemap);
            }

            Map<String, MarketData> marketdatamap = null;
            marketdatamap = getMarketdatamap(days, markets, conf);
            Map<String, PeriodData> periodDataMap = getPerioddatamap(markets,
                    marketdatamap);

            if (stocks.size() != marketdatamap.get(conf.getMarket()).stocks.size()) {
                log.error("Sizes {} {}", stocks.size(), marketdatamap.get(conf.getMarket()).stocks.size());
            }
            idNameMap = new HashMap<>();
            // sort based on date
            for (Entry<String, List<StockItem>> entry : stockidmap.entrySet()) {
                List<StockItem> stocklist = entry.getValue();
                stocklist.sort(StockUtil.StockDateComparator);
                idNameMap.put(entry.getKey(), stocklist.get(0).getName());
            }

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

            Integer cat = IndicatorUtils.getWantedCategory(stocks, periodDataMap.get("cy"));
            if (cat == null) {
                return new ArrayList<>();
            }
            DataReader dataReader = new DataReader(conf, marketdatamap, periodDataMap, null, cat);
            Pipeline[] datareaders = new Pipeline[1];
            datareaders[0] = dataReader;

            // no...get this from the category
            // TODO make oo of this
            // TODO optimize with constructors, no need for duplicate
            // map from type (complex/simple) to recommender and keysets
            Map<String, List<Recommend>> usedRecommenders = Recommend.getUsedRecommenders(conf);
            Map<String, List<String>[]> recommendKeyMap = Recommend.getRecommenderKeyMap(usedRecommenders);
            Map<String, Indicator> indicatorMap = new HashMap<>();
            int category = cat;
            Map<String, Indicator> newIndicatorMap = new HashMap<>();
            createRecommendIndicatorMap(marketdatamap, datareaders, usedRecommenders, indicatorMap, category,
                    newIndicatorMap);

            findRecommendSettings(conf, disableList, table, usedRecommenders, recommendKeyMap, indicatorMap);
            List<ResultItem> retlist = new ArrayList<>();
            retlist.add(table);
            return retlist;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return new ArrayList<>();
        }
    }

    private void findRecommendSettings(MyMyConfig conf, List<String> disableList, ResultItemTable table,
            Map<String, List<Recommend>> usedRecommenders, Map<String, List<String>[]> recommendKeyMap,
            Map<String, Indicator> indicatorMap) throws Exception {
        TaUtil tu = new TaUtil();
        for (Entry<String, List<Recommend>> entry : usedRecommenders.entrySet()) {
            List<Indicator> indicators = Recommend.getIndicators(entry.getKey(), usedRecommenders, indicatorMap);
            List<String>[] recommendList = recommendKeyMap.get(entry.getKey());
            Recommend recommend = entry.getValue().get(0);
            Object[] retObj = IndicatorUtils.getDayIndicatorMap(conf, tu, indicators, recommend.getFutureDays(), conf.getTableDays(), recommend.getIntervalDays(), null);

            List<String> buyList = recommendList[0];
            List<String> sellList = recommendList[1];
            IndicatorEvaluation recommendBuy = new IndicatorEvaluation(conf, buyList, retObj, true, disableList);
            IndicatorEvaluation recommendSell = new IndicatorEvaluation(conf, sellList, retObj, false, disableList);

            OrdinaryEvolution evolution = new OrdinaryEvolution();

            Individual buy = evolution.getFittest(conf, recommendBuy);
            Individual sell = evolution.getFittest(conf, recommendSell);

            for (String id : buyList) {
                ResultItemTableRow row = new ResultItemTableRow();
                row.add(id);
                row.add("" + conf.configValueMap.get(id));
                log.info("Buy {} {}", id, buy.conf.configValueMap.get(id));
                log.info("Buy {}", buy.conf.configValueMap.get(id).getClass().getName());
                row.add("" + buy.conf.configValueMap.get(id));
                table.add(row);
            }
            for (String id : sellList) {
                ResultItemTableRow row = new ResultItemTableRow();
                row.add(id);
                row.add("" +conf.configValueMap.get(id));
                row.add("" + sell.conf.configValueMap.get(id));
                table.add(row);
            }
            // TODO have a boolean here
            for (String id : buyList) {
                conf.configValueMap.put(id, buy.conf.configValueMap.get(id));
            }
            for (String id : sellList) {
                conf.configValueMap.put(id, sell.conf.configValueMap.get(id));
            }
        }
    }

    private void createRecommendIndicatorMap(Map<String, MarketData> marketdatamap, Pipeline[] datareaders,
            Map<String, List<Recommend>> usedRecommenders, Map<String, Indicator> indicatorMap, int category,
            Map<String, Indicator> newIndicatorMap) throws Exception {
        for (Entry<String, List<Recommend>> entry : usedRecommenders.entrySet()) {
            List<Recommend> list = entry.getValue();
            for (Recommend recommend : list) {
                String indicator = recommend.indicator();
                indicatorMap.put(indicator, recommend.getIndicator(marketdatamap, category, newIndicatorMap, null, datareaders));
            }
        }
    }

    public void getDates(MyMyConfig conf, Map<String, Map<String, Object>> maps) {
        List<StockItem> stocks = null;
        try {
            stocks = StockItem.getAll(conf.getMarket(), conf);
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
