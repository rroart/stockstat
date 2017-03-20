package roart.service;

import roart.model.GUISize;
import roart.model.Meta;
import roart.model.ResultItemTable;
import roart.model.ResultItemTableRow;
import roart.model.ResultItem;
import roart.model.Stock;

import roart.category.Category;
import roart.category.CategoryIndex;
import roart.category.CategoryPeriod;
import roart.category.CategoryPrice;
import roart.config.MyConfig;
import roart.config.MyPropertyConfig;
import roart.graphcategory.GraphCategory;
import roart.graphcategory.GraphCategoryIndex;
import roart.graphcategory.GraphCategoryPeriod;
import roart.graphcategory.GraphCategoryPeriodTopBottom;
import roart.graphcategory.GraphCategoryPrice;

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
import java.util.Collections;
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

    public List<String> getMarkets() {
    	try {
			return Stock.getMarkets();
		} catch (Exception e) {
			log.error(Constants.EXCEPTION, e);
		}
    	return null;
    }
    
    public Map<String, String> getStocks(String market) {
        try {
        	Map<String, String> stockMap = new HashMap();
            List<Stock> stocks = Stock.getAll(market);
            stocks.remove(null);
            for (Stock stock : stocks) {
                stockMap.put(stock.getId(), stock.getName());
            }
            return stockMap;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }
    
    /**
     * Create result lists
     * 
     * @return the tabular result lists
     */

    public List<ResultItem> getContent(MyConfig conf) {
        log.info("mydate " + conf.getdate());
        List<Stock> stocks = null;
        try {
            stocks = Stock.getAll(conf.getMarket());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        if (stocks == null) {
            return null;
        }
        log.info("stocks " + stocks.size());
        String[] periodText = getPeriodText(conf.getMarket());
        Set<String> markets = new HashSet();
        markets.add(conf.getMarket());
        Integer days = conf.getDays();

        ResultItemTable table = new ResultItemTable();
        
        try {
            Map<String, List<Stock>> stockidmap = StockUtil.splitId(stocks);
            Map<String, List<Stock>> stockdatemap = StockUtil.splitDate(stocks);
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

            // sort based on date
            for (String key : stockidmap.keySet()) {
                List<Stock> stocklist = stockidmap.get(key);
                stocklist.sort(StockUtil.StockDateComparator);
            }

            // the main list, based on freshest or specific date.

            /*
             * For all days with intervals
             * Make stock lists based on the intervals
             */

            List<Stock> datedstocklists[] = StockUtil.getDatedstocklists(stockdatemap, conf.getdate(), 2, conf.getTableMoveIntervalDays());

            List<Stock>[][] stocklistPeriod = StockUtil.getListSorted(datedstocklists, 2);
            Map<String, Integer>[][] periodmaps = StockUtil.getListMove(datedstocklists, 2, stocklistPeriod);
            Map<String, Integer>[] periodmap = periodmaps[0];

            List<Stock> datedstocks = datedstocklists[0];
            List<Stock> datedstocksoffset = datedstocklists[1];
            if (datedstocks == null) {
                return null;
            }

            Category[] categories = getCategories(conf, stocks,
                    periodText, marketdatamap, periodDataMap, periodmap);

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
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }

            table.add(headrow);
            //log.info("sizes " + stocks.size() + " " + datedstocks.size() + " " + datedstocksoffset.size());
            for (Stock stock : datedstocks) {
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
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }

                 table.add(row);

            }
            log.info("retlist2 " +table.size());
                    } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<ResultItem> retlist = new ArrayList<ResultItem>();
        retlist.add(table);
        return retlist;
    }

	private void getCurrentDate(MyConfig conf, Map<String, List<Stock>> stockdatemap) throws ParseException {
		SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
		String date = null;
		TreeSet set = new TreeSet<String>(stockdatemap.keySet());
		List<String> list = new ArrayList(set);
		int size = list.size();
		date = list.get(size - 1);
		conf.setdate(dt.parse(date));
		log.info("mydate2 " + conf.getdate());
	}

    private Category[] getCategories(MyConfig conf, List<Stock> stocks,
            String[] periodText,
            Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap) {
        Category[] categories = new Category[StockUtil.PERIODS + 2];
        categories[0] = new CategoryIndex(conf, Constants.INDEX, stocks, marketdatamap, periodDataMap, periodmap);
        categories[1] = new CategoryPrice(conf, Constants.PRICE, stocks, marketdatamap, periodDataMap, periodmap);
        for (int i = 0; i < StockUtil.PERIODS; i++) {
            categories[i + 2] = new CategoryPeriod(conf, i, periodText[i], stocks, marketdatamap, periodDataMap, periodmap);
        }
        return categories;
    }

    private GraphCategory[] getGraphCategories(MyConfig conf,
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
    
    private String[] getPeriodText(String market) {
        String[] periodText = { "Period1", "Period2", "Period3", "Period4", "Period5", "Period6" };
        Meta meta = null;
        try {
            meta = Meta.getById(market);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        try {
            if (meta != null) {
                for (int i = 0; i < StockUtil.PERIODS; i++) {
                    if (MetaDao.getPeriod(meta, i + 1) != null) {
                        periodText[i] = MetaDao.getPeriod(meta, i + 1);
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

    public List<ResultItem> getContentGraph(MyConfig conf, GUISize guiSize) {
        List<ResultItem> retlist = new ArrayList<>();
        try {
            log.info("mydate " + conf.getdate());
            List<Stock> stocks = Stock.getAll(conf.getMarket());
            log.info("stocks " + stocks.size());
            String[] periodText = getPeriodText(conf.getMarket());
            Map<String, List<Stock>> stockidmap = StockUtil.splitId(stocks);
            Map<String, List<Stock>> stockdatemap = StockUtil.splitDate(stocks);
            if (conf.getdate() == null) {
                getCurrentDate(conf, stockdatemap);
            }

            // sort based on date
            for (String key : stockidmap.keySet()) {
                List<Stock> stocklist = stockidmap.get(key);
                stocklist.sort(StockUtil.StockDateComparator);
            }

            int days = conf.getTableDays();

            // the main list, based on freshest or specific date.

            /*
             * For all days with intervals
             * Make stock lists based on the intervals
             */
            
            List<Stock> datedstocklistsmove[] = StockUtil.getDatedstocklists(stockdatemap, conf.getdate(), days, conf.getTableMoveIntervalDays());
            
            List<Stock>[][] stocklistPeriod = StockUtil.getListSorted(datedstocklistsmove, days);

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

    public List<ResultItem> getContentGraph(MyConfig conf, Set<Pair<String, String>> ids, GUISize guiSize) {
        List<ResultItem> retlist = new ArrayList<>();
        try {
            log.info("mydate " + conf.getdate());
           int days = conf.getTableDays();
            Set<String> markets = getMarkets(ids);
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
                System.out.println("text " + market + " " + i + " " + text);
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
        System.out.println("per " + periodDataMap.keySet());
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
            Set<String> markets, MyConfig conf) throws Exception {
        Map<String, MarketData> marketdatamap = new HashMap();
        for (String market : markets) {
            log.info("prestocks");
            List<Stock> stocks = Stock.getAll(market);
            log.info("stocks " + stocks.size());
            MarketData marketdata = new MarketData();
            marketdata.stocks = stocks;
            String[] periodText = getPeriodText(market);
            marketdata.periodtext = periodText;
            //Map<String, List<Stock>> stockidmap = StockUtil.splitId(stocks);
            Map<String, List<Stock>> stockdatemap = StockUtil.splitDate(stocks);
            // the main list, based on freshest or specific date.

            /*
             * For all days with intervals
             * Make stock lists based on the intervals
             */
            
            // TODO check out the 15 days
            List<Stock> datedstocklists[] = StockUtil.getDatedstocklists(stockdatemap, conf.getdate(), days + 15, conf.getTableIntervalDays());
            marketdata.datedstocklists = datedstocklists;
            
            //Object[] arr = new Object[1];
            //Map<String, Integer>[][] periodmaps = StockUtil.getListAndDiff(datedstocklists, stockidmap, stockdatemap, days, getTableIntervalDays(), arr);
            //HashMap<String, Integer>[] periodmap = periodmaps[0];
            //List<Stock>[][] stocklistPeriod = (List<Stock>[][]) arr[0];
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

    public List<ResultItem> getContentStat(MyConfig conf) {
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
            List<Stock> stocks = Stock.getAll(conf.getMarket());
            log.info("stocks " + stocks.size());
            Map<String, List<Stock>> stockidmap = StockUtil.splitId(stocks);
            Map<String, List<Stock>> stockdatemap = StockUtil.splitDate(stocks);

            // sort based on date
            for (String key : stockidmap.keySet()) {
                List<Stock> stocklist = stockidmap.get(key);
                stocklist.sort(StockUtil.StockDateComparator);
            }

            int days = conf.getTableDays();

            // the main list, based on freshest or specific date.

            /*
             * For all days with intervals
             * Make stock lists based on the intervals
             */
            
            List<Stock> datedstocklists[] = StockUtil.getDatedstocklists(stockdatemap, conf.getdate(), days, conf.getTableIntervalDays());
            
            List<Stock> datedstocks = datedstocklists[0];
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

    private void printstock(List<Stock> stocklistPeriod4Day1, int imax) {
        int i = 0;
        for (Stock s : stocklistPeriod4Day1) {
            //log.info(s.getId() + " : " + s.getName() + " : " + s.getPeriod4());
            i++;
            if (i > imax) {
                return;
            }
        }
    }

}
