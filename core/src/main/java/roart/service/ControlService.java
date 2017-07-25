package roart.service;

import roart.model.GUISize;
import roart.model.MetaItem;
import roart.model.ResultItemTable;
import roart.model.ResultItemTableRow;
import roart.model.ResultItem;
import roart.model.StockItem;
import roart.mutation.Mutate;
import roart.aggregate.Aggregator;
import roart.aggregate.AggregatorRecommenderMacdRsi;
import roart.aggregate.MLMACD;
import roart.aggregate.RecommenderRSI;
import roart.category.Category;
import roart.category.CategoryIndex;
import roart.category.CategoryPeriod;
import roart.category.CategoryPrice;
import roart.config.ConfigConstants;
import roart.config.MyMyConfig;
import roart.config.MyPropertyConfig;
import roart.db.DbDao;
import roart.evaluation.MACDRSIEvaluation;
import roart.evolution.FitnessBuySellMACD;
import roart.evolution.OrdinaryEvolution;
import roart.evolution.Individual;
import roart.graphcategory.GraphCategory;
import roart.graphcategory.GraphCategoryIndex;
import roart.graphcategory.GraphCategoryPeriod;
import roart.graphcategory.GraphCategoryPeriodTopBottom;
import roart.graphcategory.GraphCategoryPrice;
import roart.indicator.Indicator;
import roart.indicator.IndicatorMACD;
import roart.indicator.IndicatorRSI;
import roart.indicator.IndicatorUtils;

import javax.servlet.http.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.io.*;

import roart.util.Constants;
import roart.util.MarketData;
import roart.util.StockDao;
import roart.util.StockUtil;
import roart.util.SvgUtil;
import roart.util.TaUtil;
import roart.util.Math3Util;
import roart.util.MetaDao;
import roart.util.PeriodData;

import org.apache.commons.math3.util.Pair;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    	return null;
    }
    
    public Map<String, String> getStocks(String market, MyMyConfig conf) {
        try {
        	Map<String, String> stockMap = new HashMap();
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
    
    public static int otherTableNames[] = { EVENT, MLTIMES }; 
    
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
     * 
     * @return the tabular result lists
     */

    public List<ResultItem> getContent(MyMyConfig conf) {
        log.info("mydate " + conf.getdate());
        log.info("mydate " + conf.getDays());
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
        log.info("stocks " + stocks.size());
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
            try {
                marketdatamap = getMarketdatamap(days, markets, conf);
            } catch(Exception e) {
                log.error(Constants.EXCEPTION, e);            
            }
            Map<String, PeriodData> periodDataMap = getPerioddatamap(markets,
                    marketdatamap);

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

            List<StockItem> datedstocklists[] = StockUtil.getDatedstocklists(stockdatemap, conf.getdate(), 2, conf.getTableMoveIntervalDays());

            List<StockItem>[][] stocklistPeriod = StockUtil.getListSorted(datedstocklists, 2);
            Map<String, Integer>[][] periodmaps = StockUtil.getListMove(datedstocklists, 2, stocklistPeriod);
            Map<String, Integer>[] periodmap = periodmaps[0];

            List<StockItem> datedstocks = datedstocklists[0];
            System.out.println("dat sto siz " + datedstocks.size());
            /*
            Set<String> curIds = new HashSet<>();
            for (StockItem stock : datedstocks) {
            	curIds.add(stock.getId());
            }
            */
            List<StockItem> datedstocksoffset = datedstocklists[1];
            if (datedstocks == null) {
                return null;
            }

            Category[] categories = getCategories(conf, stocks,
                    periodText, marketdatamap, periodDataMap, periodmap);
            
            Aggregator[] aggregates = getAggregates(conf, stocks,
                    periodText, marketdatamap, periodDataMap, periodmap, categories);
            
            ResultItemTableRow headrow = new ResultItemTableRow();
            //ri.add("Id");
            headrow.add(Constants.IMG);
            headrow.add("Name");
            headrow.add("Date");
            try {
                for (int i = 0; i < StockUtil.ALLPERIODS; i++) {
                    categories[i].addResultItemTitle(headrow);
                    //System.out.print("first " + ri.get().size());
                }
                for (int i = 0; i < aggregates.length; i++) {
                    aggregates[i].addResultItemTitle(headrow);
                    //System.out.print("first " + ri.get().size());
                }
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }

            table.add(headrow);
            //log.info("sizes " + stocks.size() + " " + datedstocks.size() + " " + datedstocksoffset.size());
            for (StockItem stock : datedstocks) {
                //System.out.println("" + mydate.getTime() + "|" + stock.getDate().getTime());
                if (conf.getMarket() == null) {
                    continue;
                }
                if (false &&  conf.getdate() != null && conf.getdate().getTime() != stock.getDate().getTime()) {
                    continue;
                }
                ResultItemTableRow row = new ResultItemTableRow();
                row.add(stock.getId());
                row.add(stock.getName());
                SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
                row.add(dt.format(stock.getDate()));
                try {
                    for (int i = 0; i < StockUtil.ALLPERIODS; i++) {
                        categories[i].addResultItem(row, stock);
                        //System.out.print("others " + r.get().size());
                    }
                    for (int i = 0; i < aggregates.length; i++) {
                        aggregates[i].addResultItem(row, stock);
                        //System.out.print("others " + r.get().size());
                    }
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }

                 table.add(row);

            }
            log.info("retlist2 " +table.size());
            for (int i = 0; i < StockUtil.ALLPERIODS; i++) {
                Map<Integer, List<ResultItemTableRow>> tableMap = categories[i].otherTables();
                for (Integer key : tableMap.keySet()) {
                    List<ResultItemTableRow> resultItems = tableMap.get(key);
                    ResultItemTable otherTable = otherTableMap.get(key);
                    for (ResultItemTableRow row : resultItems) {
                    otherTable.add(row);
                    }
                }
                   //System.out.print("first " + ri.get().size());
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<ResultItem> retlist = new ArrayList<ResultItem>();
        retlist.add(table);
        for (ResultItemTable list : otherTables) {
            retlist.add(list);
        }
        return retlist;
    }

	private void getCurrentDate(MyMyConfig conf, Map<String, List<StockItem>> stockdatemap) throws ParseException {
		SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
		String date = null;
		TreeSet set = new TreeSet<String>(stockdatemap.keySet());
		List<String> list = new ArrayList(set);
		int size = list.size();
		date = list.get(size - 1);
		conf.setdate(dt.parse(date));
		log.info("mydate2 " + conf.getdate());
	}

    private Category[] getCategoriesSmall(MyMyConfig conf, List<StockItem> stocks,
            String[] periodText,
            Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap) throws Exception {
        Category[] categories = new Category[1];
        //categories[0] = new CategoryIndex(conf, Constants.INDEX, stocks, marketdatamap, periodDataMap, periodmap);
        categories[1] = new CategoryPrice(conf, Constants.PRICE, stocks, marketdatamap, periodDataMap, periodmap);
        return categories;
    }

    private Aggregator[] getAggregates(MyMyConfig conf, List<StockItem> stocks,
            String[] periodText,
            Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap, Category[] categories) throws Exception {
        Aggregator[] aggregates = new Aggregator[3];
        aggregates[0] = new AggregatorRecommenderMacdRsi(conf, Constants.PRICE, stocks, marketdatamap, periodDataMap, periodmap, categories);
        aggregates[1] = new RecommenderRSI(conf, Constants.PRICE, stocks, marketdatamap, periodDataMap, periodmap, categories);
        aggregates[2] = new MLMACD(conf, Constants.PRICE, stocks, marketdatamap, periodDataMap, "Price", 0, categories);
        return aggregates;
    }

    private Category[] getCategories(MyMyConfig conf, List<StockItem> stocks,
            String[] periodText,
            Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap) throws Exception {
        Category[] categories = new Category[StockUtil.PERIODS + 2];
        categories[0] = new CategoryIndex(conf, Constants.INDEX, stocks, marketdatamap, periodDataMap, periodmap);
        categories[1] = new CategoryPrice(conf, Constants.PRICE, stocks, marketdatamap, periodDataMap, periodmap);
        for (int i = 0; i < StockUtil.PERIODS; i++) {
            categories[i + 2] = new CategoryPeriod(conf, i, periodText[i], stocks, marketdatamap, periodDataMap, periodmap);
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
        Set<String> keys = new TreeSet(periodDataMap.keySet());
        keys.remove(Constants.INDEX);
        keys.remove(Constants.PRICE);
        for (String periodText : keys) {
        // periodDataMap.keySet
        //for (int i = 0; i < StockUtil.PERIODS; i++) {
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
    
    private String[] getPeriodText(String market, MyMyConfig conf) {
        String[] periodText = { "Period1", "Period2", "Period3", "Period4", "Period5", "Period6" };
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
            log.info("mydate " + conf.getdate());
            List<StockItem> stocks = StockItem.getAll(conf.getMarket(), conf);
            log.info("stocks " + stocks.size());
            String[] periodText = getPeriodText(conf.getMarket(), conf);
            Map<String, List<StockItem>> stockidmap = StockUtil.splitId(stocks);
            Map<String, List<StockItem>> stockdatemap = StockUtil.splitDate(stocks);
            if (conf.getdate() == null) {
                getCurrentDate(conf, stockdatemap);
            }

            // sort based on date
            for (String key : stockidmap.keySet()) {
                List<StockItem> stocklist = stockidmap.get(key);
                stocklist.sort(StockUtil.StockDateComparator);
            }

            int days = conf.getTableDays();

            // the main list, based on freshest or specific date.

            /*
             * For all days with intervals
             * Make stock lists based on the intervals
             */
            
            List<StockItem> datedstocklistsmove[] = StockUtil.getDatedstocklists(stockdatemap, conf.getdate(), days, conf.getTableMoveIntervalDays());
            
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
        log.info("retlist size " + retlist.size());
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
            log.info("mydate " + conf.getdate());
           int days = conf.getTableDays();
            Set<String> markets = getMarkets(ids);
            List<StockItem> stocks = null;
            try {
                stocks = StockItem.getAll(conf.getMarket(), conf);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            if (stocks == null) {
                return null;
            }
            log.info("stocks " + stocks.size());
            Map<String, List<StockItem>> stockdatemap = StockUtil.splitDate(stocks);
            if (conf.getdate() == null) {
                getCurrentDate(conf, stockdatemap);
            }
            Map<String, MarketData> marketdatamap = getMarketdatamap(days,
                    markets, conf);
            Map<String, PeriodData> periodDataMap = getPerioddatamap(markets,
                    marketdatamap);
            int topbottom = conf.getTopBottom();
            GraphCategory[] categories = getGraphCategories(conf, null, marketdatamap, periodDataMap);

            try {
                for (int i = 0; i < StockUtil.ALLPERIODS; i++) {
                    categories[i].addResult(retlist, ids, guiSize);
                }
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }

        log.info("retlist size " + retlist.size());
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
        //System.out.println("siz " + marketdatamap.size());
        Map<String, PeriodData> periodDataMap = new HashMap();
        for (String market : markets) {
            System.out.println("market " + market);
            String[] periodText = marketdatamap.get(market).periodtext;
            for (int i = 0; i < StockUtil.PERIODS; i++) {
                String text = periodText[i];
                //System.out.println("text " + market + " " + i + " " + text);
                Pair<String, Integer> pair = new Pair(market, i);
                addPairToPeriodDataMap(periodDataMap, text, pair);
             }
            if (true) {
                Pair<String, Integer> pair = new Pair(market, Constants.PRICECOLUMN);
                addPairToPeriodDataMap(periodDataMap, Constants.PRICE, pair);                
            }
            if (true) {
                Pair<String, Integer> pair = new Pair(market, Constants.INDEXVALUECOLUMN);
                addPairToPeriodDataMap(periodDataMap, Constants.INDEX, pair);                
            }
        }
        //System.out.println("per " + periodDataMap.keySet());
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
		    //System.out.println("new " + text);
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
            log.info("stocks " + stocks.size());
            MarketData marketdata = new MarketData();
            marketdata.stocks = stocks;
            String[] periodText = getPeriodText(market, conf);
            marketdata.periodtext = periodText;
            //Map<String, List<StockItem>> stockidmap = StockUtil.splitId(stocks);
            Map<String, List<StockItem>> stockdatemap = StockUtil.splitDate(stocks);
            // the main list, based on freshest or specific date.

            /*
             * For all days with intervals
             * Make stock lists based on the intervals
             */
            
            List<StockItem> datedstocklists[] = StockUtil.getDatedstocklists(stockdatemap, conf.getdate(), days, conf.getTableIntervalDays());
            marketdata.datedstocklists = datedstocklists;
            
            //Object[] arr = new Object[1];
            //Map<String, Integer>[][] periodmaps = StockUtil.getListAndDiff(datedstocklists, stockidmap, stockdatemap, days, getTableIntervalDays(), arr);
            //HashMap<String, Integer>[] periodmap = periodmaps[0];
            //List<StockItem>[][] stocklistPeriod = (List<StockItem>[][]) arr[0];
            //marketdata.stocklistperiod = stocklistPeriod;

            marketdatamap.put(market,  marketdata);
        }
        return marketdatamap;
    }

    private Set<String> getMarkets(Set<Pair<String, String>> ids) {
        //List<String> ids = new ArrayList<String>();
        Set<String> markets = new HashSet<String>();
        for (Pair idpair : ids) {
            markets.add((String) idpair.getFirst());
            //ids.add((String) idpair.getSecond());
        }
        return markets;
    }

    /**
     * Create stat result lists
     * 
     * @return the tabular result lists
     */

    public List<ResultItem> getContentStat(MyMyConfig conf) {
        //mydate.setHours(0);
        //mydate.setMinutes(0);
        //mydate.setSeconds(0);
        List<ResultItem> retList = new ArrayList<ResultItem>();
        ResultItemTable table = new ResultItemTable();
        ResultItemTableRow row = new ResultItemTableRow();
        //ri.add("Id");
        String delta = "Delta";
        delta = "Δ";
        row.add(Constants.IMG);
        row.add("Name 1");
        row.add("Name 2");
        //ri.add("Date");
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
            log.info("stocks " + stocks.size());
            Map<String, List<StockItem>> stockidmap = StockUtil.splitId(stocks);
            Map<String, List<StockItem>> stockdatemap = StockUtil.splitDate(stocks);

            // sort based on date
            for (String key : stockidmap.keySet()) {
                List<StockItem> stocklist = stockidmap.get(key);
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
                return null;
            }
            Math3Util.getStats(table, conf.getdate(), days, stockidmap, stockdatemap);
            
            log.info("retlist " +retList.size());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        retList.add(table);
        return retList;
    }
    /*
    public void config(MyMyConfig config) {    	
    	configDb(config.useSpark);
    }
    
    public void configDb(Boolean useSpark) {
    	if (useSpark) {
    		DbDao.instance("spark");
    	} else {
    		DbDao.instance("hibernate");
    	}
    } 
    */

    public List<ResultItem> getTestRecommender(MyMyConfig conf) {
        conf.disableML();
        log.info("mydate " + conf.getdate());
        log.info("mydate " + conf.getDays());
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
        log.info("stocks " + stocks.size());
        String[] periodText = getPeriodText(conf.getMarket(), conf);
        Set<String> markets = new HashSet();
        markets.add(conf.getMarket());
        Integer days = conf.getDays();

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
            try {
                marketdatamap = getMarketdatamap(days, markets, conf);
            } catch(Exception e) {
                log.error(Constants.EXCEPTION, e);            
            }
            Map<String, PeriodData> periodDataMap = getPerioddatamap(markets,
                    marketdatamap);

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

            List<StockItem> datedstocklists[] = StockUtil.getDatedstocklists(stockdatemap, conf.getdate(), 2, conf.getTableMoveIntervalDays());

            List<StockItem>[][] stocklistPeriod = StockUtil.getListSorted(datedstocklists, 2);
            //Map<String, Integer>[][] periodmaps = StockUtil.getListMove(datedstocklists, 2, stocklistPeriod);
            //Map<String, Integer>[] periodmap = periodmaps[0];

            List<StockItem> datedstocks = datedstocklists[0];
            System.out.println("dat sto siz " + datedstocks.size());
            /*
            Set<String> curIds = new HashSet<>();
            for (StockItem stock : datedstocks) {
                curIds.add(stock.getId());
            }
             */
            List<StockItem> datedstocksoffset = datedstocklists[1];
            if (datedstocks == null) {
                return null;
            }

            //Category[] categories = getCategoriesSmall(conf, stocks,
            //       periodText, marketdatamap, periodDataMap, null);
            String title = "Price";
            Map<String, Integer>[] periodmap = null;
            // no...get this from the category
            IndicatorMACD indicatorMACD = new IndicatorMACD(conf, title + " MACD", marketdatamap, periodDataMap, periodmap, title, Constants.PRICECOLUMN);
            IndicatorRSI indicatorRSI = new IndicatorRSI(conf, title + " RSI", marketdatamap, periodDataMap, periodmap, title, Constants.PRICECOLUMN);

            Map<String, Object> macdResultMap = indicatorMACD.getResultMap();
            Map<String, Object> rsiResultMap = indicatorRSI.getResultMap();
            
            Map<String, Object[]> objectMACDMap = (Map<String, Object[]>) macdResultMap.get("OBJECT");
            Map<String, Double[]> listMACDMap = (Map<String, Double[]>) macdResultMap.get("LIST");
            Map<String, Object[]> objectRSIMap = (Map<String, Object[]>) rsiResultMap.get("OBJECT");
            Map<String, Double[]> listRSIMap = (Map<String, Double[]>) rsiResultMap.get("LIST");

            int selectionSize = conf.getTestRecommendSelect();

            List<Individual> populationBuy = new ArrayList<>();
            List<Individual> populationSell = new ArrayList<>();
            
            TaUtil tu = new TaUtil();
            Object[] retObj = IndicatorUtils.getDayMomRsiMap(conf, objectMACDMap, listMACDMap, objectRSIMap, listRSIMap, tu);
            MACDRSIEvaluation recommend = new MACDRSIEvaluation(null, null, null, false);
            
            List<String> buyList = recommend.getBuyList();
            List<String> sellList = recommend.getSellList();
            MACDRSIEvaluation recommendBuy = new MACDRSIEvaluation(conf, buyList, retObj, true);
            MACDRSIEvaluation recommendSell = new MACDRSIEvaluation(conf, sellList, retObj, false);
            
            int category = 0;
            String market = null; //"tradcomm";
            //List<Double> macdLists[] = new ArrayList[4];

            /*
            Object[] retMACDObj = getDayMomMap(conf, objectMACDMap, listMACDMap, tu);
            Object[] retRSIObj = getDayRsiMap(conf, objectRSIMap, listRSIMap, tu);
            Map<Integer, Map<String, Double[]>> dayMomMap = (Map<Integer, Map<String, Double[]>>) retMACDObj[0];
            Map<Integer, List<Double>[]> dayMacdListsMap = (Map<Integer, List<Double>[]>) retMACDObj[1];
*/
            // TODO clone config
            //MyMyConfig bestBuyConfig = new MyMyConfig();
            //MyMyConfig bestSellConfig = new MyMyConfig();
            
            int macdlen = conf.getTableDays();
            int listlen = conf.getTableDays();
            
            OrdinaryEvolution evolution = new OrdinaryEvolution();
            
            Individual buy = evolution.getFittest(conf, marketdatamap, listMACDMap, objectMACDMap, objectRSIMap, recommendBuy);
            Individual sell = evolution.getFittest(conf, marketdatamap, listMACDMap, objectMACDMap, objectRSIMap, recommendSell);
            /*
            double buy = getScores(true, conf, listMap, bestBuyConfig, buyList, macdlen, listlen, dayMomMap, dayMacdListsMap);
            double sell = getScores(false, conf, listMap, bestSellConfig, sellList, macdlen, listlen, dayMomMap, dayMacdListsMap);
                populationBuy.add(new Populi(bestBuyConfig, buy));
            populationSell.add(new Populi(bestSellConfig, sell));
            for (int i = 1; i < 2 * selectionSize; i ++) {
                Map<String, Object> buyValueMap = new HashMap<>(bestBuyConfig.configValueMap);
                Map<String, Object> sellValueMap = new HashMap<>(bestSellConfig.configValueMap);

                getRandom(buyValueMap, recommend.getBuyList());
                getRandom(sellValueMap, recommend.getBuyList());
                MyMyConfig testBuyConfig = new MyMyConfig();
                MyMyConfig testSellConfig = new MyMyConfig();
                testBuyConfig.configValueMap = buyValueMap;
                testSellConfig.configValueMap = sellValueMap;
                buy = getScores(true, conf, listMap, bestBuyConfig, buyList, macdlen, listlen, dayMomMap, dayMacdListsMap);
                sell = getScores(false, conf, listMap, bestSellConfig, sellList, macdlen, listlen, dayMomMap, dayMacdListsMap);
                populationBuy.add(new Populi(testBuyConfig, buy));
                 populationSell.add(new Populi(testSellConfig, sell));
                 List<Populi> crossoverBuy = crossover(conf.getTestRecommendChildren(), populationBuy, recommend.getBuyList());
                 List<Populi> crossoverSell = crossover(conf.getTestRecommendChildren(), populationSell, recommend.getSellList());
                 mutate(crossoverBuy, recommend.getBuyList());
                 mutate(crossoverSell, recommend.getSellList());
            }
            
            
            MyMyConfig testBuyConfig = new MyMyConfig();
            MyMyConfig testSellConfig = new MyMyConfig();
            bestBuyConfig.configValueMap = new HashMap<>(conf.configValueMap);
            bestSellConfig.configValueMap = new HashMap<>(conf.configValueMap);
            testBuyConfig.configValueMap = new HashMap<>(conf.configValueMap);
            testSellConfig.configValueMap = new HashMap<>(conf.configValueMap);

            bestBuyConfig.disableML();
            bestSellConfig.disableML();
            testBuyConfig.disableML();
            testSellConfig.disableML();

            //int macdlen = ((double[])(objectMap.values().iterator().next())[0]).length;
            //int listlen = (listMap.values()).iterator().next().length;
            System.out.println("macdlen"+macdlen + " " + listlen);

            double bestRecommendQualBuy = -1000;
            double bestRecommendQualSell = -1000;
            for (int i = 0; i < conf.getTestRecommendIterations(); i++) {
                System.out.println("i " + i);
                double buy = getScores(true, conf, listMap, testBuyConfig, testSellConfig, buyList, sellList, macdlen, listlen, dayMomMap,
                        dayMacdListsMap);
                //buysell
                //double l
                //System.out.println(buysell);
                System.out.println("test " + testRecommendQualBuy + " " + bestRecommendQualBuy + " " + testRecommendQualSell + " " + bestRecommendQualSell);
                if (testRecommendQualBuy > bestRecommendQualBuy) {
                    bestRecommendQualBuy = testRecommendQualBuy;
                    bestBuyConfig.configValueMap = new HashMap<>(testBuyConfig.configValueMap);
                } else {
                    testBuyConfig.configValueMap = new HashMap<>(bestBuyConfig.configValueMap);                    
                }
                if (testRecommendQualSell > bestRecommendQualSell) {
                    bestRecommendQualSell = testRecommendQualSell;
                    bestSellConfig.configValueMap = new HashMap<>(testSellConfig.configValueMap);
                } else {
                    testSellConfig.configValueMap = new HashMap<>(bestSellConfig.configValueMap);                    
                }
                Mutate.mutate(testBuyConfig.configValueMap, buyList);
                Mutate.mutate(testSellConfig.configValueMap, sellList);
            }
            */
            ResultItemTable table = new ResultItemTable();
            ResultItemTableRow headrow = new ResultItemTableRow();
            headrow.add("Config");
            headrow.add("Old value");
            headrow.add("New value");
            table.add(headrow);
            for (String id : buyList) {
                System.out.println("r1");
                ResultItemTableRow row = new ResultItemTableRow();
                row.add(id);
                row.add(conf.configValueMap.get(id));
                System.out.println(buy.conf.configValueMap.get(id));
                System.out.println(buy.conf.configValueMap.get(id).getClass().getName());
                row.add(buy.conf.configValueMap.get(id));
                table.add(row);
            }
            for (String id : sellList) {
                System.out.println("r2");
                ResultItemTableRow row = new ResultItemTableRow();
                row.add(id);
                row.add(conf.configValueMap.get(id));
                row.add(sell.conf.configValueMap.get(id));
                table.add(row);
            }
            List<ResultItem> retlist = new ArrayList<>();
            retlist.add(table);
            for (ResultItemTable list : otherTables) {
                //retlist.add(list);
            }
            System.out.println("retlist ");
            // TODO have a boolean here
            for (String id : buyList) {
                conf.configValueMap.put(id, buy.conf.configValueMap.get(id));
            }
            for (String id : sellList) {
                conf.configValueMap.put(id, sell.conf.configValueMap.get(id));
            }
            return retlist;
        } catch (Exception e) {
            log.error("E ", e);
            return null;
        }
    }
    
}
