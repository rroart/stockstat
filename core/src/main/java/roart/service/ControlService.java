package roart.service;

import roart.model.GUISize;
import roart.model.Meta;
import roart.model.ResultItemNot;
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

        List<ResultItemNot> retList = new ArrayList<ResultItemNot>();
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

            ResultItemNot ri = new ResultItemNot();
            //ri.add("Id");
            ri.add(Constants.IMG);
            ri.add("Name");
            ri.add("Date");
            try {
                for (int i = 0; i < StockUtil.ALLPERIODS; i++) {
                    categories[i].addResultItemTitle(ri);
                    //System.out.print("first " + ri.get().size());
                }
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }

            retList.add(ri);
            ResultItemTableRow ri2head = new ResultItemTableRow();
            ri2head.cols = ri.get();
            table.add(ri2head);
            //log.info("sizes " + stocks.size() + " " + datedstocks.size() + " " + datedstocksoffset.size());
            for (Stock stock : datedstocks) {
                //System.out.println("" + mydate.getTime() + "|" + stock.getDate().getTime());
                if (conf.getMarket() == null) {
                    continue;
                }
                if (false &&  conf.getdate() != null && conf.getdate().getTime() != stock.getDate().getTime()) {
                    continue;
                }
                ResultItemNot r = new ResultItemNot();
                r.add(stock.getId());
                r.add(stock.getName());
                SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
                r.add(dt.format(stock.getDate()));
                try {
                    for (int i = 0; i < StockUtil.ALLPERIODS; i++) {
                        categories[i].addResultItem(r, stock);
                        //System.out.print("others " + r.get().size());
                    }
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }

                /*
                try {
                    if (StockUtil.hasSpecial(stocks, Constants.INDEXVALUE)) {
                        r.add(stock.getIndexvalue());
                    }

                    if (StockUtil.hasSpecial(stocks, Constants.PRICE)) {
                        r.add(stock.getPrice());
                        if (isMACDenabled()) {
                            TaUtil tu = new TaUtil();
                            String market = getMarket();
                            String id = stock.getId();
                            Pair pair = new Pair(market, id);
                            Set ids = new HashSet();
                            ids.add(pair);
                            String periodstr = "price";
                            PeriodData perioddata = periodDataMap.get(periodstr);
                            double momentum = tu.getMom(days, market, id, ids, marketdatamap, perioddata, periodstr);
                            r.add(momentum);
                        }
                        if (isRSIenabled()) {
                            TaUtil tu = new TaUtil();
                            String market = getMarket();
                            String id = stock.getId();
                            Pair pair = new Pair(market, id);
                            Set ids = new HashSet();
                            ids.add(pair);
                            String periodstr = "price";
                            PeriodData perioddata = periodDataMap.get(periodstr);
                            double rsi = tu.getRSI2(days, market, id, ids, marketdatamap, perioddata, periodstr);
                            r.add(rsi);
                        }
                        r.add(stock.getCurrency());
                    }
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
                 */

                //r.add(stock.get());
                retList.add(r);
                ResultItemTableRow ri2 = new ResultItemTableRow();
                ri2.cols = r.get();
                table.add(ri2);

            }
            log.info("retlist " +retList.size());
            log.info("retlist2 " +table.size());
                    } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<List> retlistlist = new ArrayList<List>();
        retlistlist.add(retList);
        List<ResultItem> retlistlist2 = new ArrayList<ResultItem>();
        retlistlist2.add(table);
        return retlistlist2;
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
        categories[0] = new CategoryIndex(conf, "Index", stocks, marketdatamap, periodDataMap, periodmap);
        categories[1] = new CategoryPrice(conf, "Price", stocks, marketdatamap, periodDataMap, periodmap);
        for (int i = 0; i < StockUtil.PERIODS; i++) {
            categories[i + 2] = new CategoryPeriod(conf, i, periodText[i], stocks, marketdatamap, periodDataMap, periodmap);
        }
        return categories;
    }

    private GraphCategory[] getGraphCategories(MyConfig conf,
            String[] periodTextNot,
            Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap) {
        GraphCategory[] categories = new GraphCategory[StockUtil.PERIODS + 2];
        categories[0] = new GraphCategoryIndex(conf, "Index", marketdatamap, periodDataMap);
        categories[1] = new GraphCategoryPrice(conf, "Price", marketdatamap, periodDataMap);
        int i = 2;
        Set<String> keys = new TreeSet(periodDataMap.keySet());
        keys.remove("Index");
        keys.remove("Price");
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
                PeriodData perioddata = periodDataMap.get(text);
                if (perioddata == null) {
                    perioddata = new PeriodData();
                    periodDataMap.put(text, perioddata);
                    //System.out.println("new " + text);
                }
                Set<Pair<String, Integer>> pairs = perioddata.pairs;
                pairs.add(pair);
            }
            if (true) {
                String text = "Price";
                Pair<String, Integer> pair = new Pair(market, Constants.PRICE);
                PeriodData perioddata = periodDataMap.get(text);
                if (perioddata == null) {
                    perioddata = new PeriodData();
                    periodDataMap.put(text, perioddata);
                    //System.out.println("new " + text);
                }
                Set<Pair<String, Integer>> pairs = perioddata.pairs;
                pairs.add(pair);                
            }
            if (true) {
                String text = "Index";
                Pair<String, Integer> pair = new Pair(market, Constants.INDEXVALUE);
                PeriodData perioddata = periodDataMap.get(text);
                if (perioddata == null) {
                    perioddata = new PeriodData();
                    periodDataMap.put(text, perioddata);
                    //System.out.println("new " + text);
                }
                Set<Pair<String, Integer>> pairs = perioddata.pairs;
                pairs.add(pair);                
            }
        }
        System.out.println("per " + periodDataMap.keySet());
        return periodDataMap;
    }

    private Map<String, MarketData> getMarketdatamap(int days,
            Set<String> markets, MyConfig conf) throws Exception {
        /*
        String date0 = null;
        String date1 = null;
        */
        /*
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        List<Date> stockdatelist2 = StockDao.getDates();
        List<String> stockdatelist = new ArrayList();
        for (Date date : stockdatelist2) {
            stockdatelist.add(dt.format(date));
        }
        Collections.sort(stockdatelist);
        String date = null;
        if (getdate() != null) {
            date = dt.format(getdate());
        }
        System.out.println("grr " + stockdatelist.size()  + " " + stockdatelist.get(0) + " " + stockdatelist.get(stockdatelist.size()-1) + " " + date);
        int index = StockUtil.getStockDate(stockdatelist, date);
        System.out.println("index " + index);
        if (index >= 0) {
            date1 = stockdatelist.get(index);
            for (int j = 1; j < getTableDays(); j++) {
                index = index - mytableintervaldays;
                if (index >= 0) {
                    date0 = stockdatelist.get(index);
                }
            }
        }
        */
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
            //marketdata.stockidmap = stockidmap;
            //marketdata.stockdatemap = stockdatemap;
            
            /*
            // sort based on date
            for (String key : stockidmap.keySet()) {
                List<Stock> stocklist = stockidmap.get(key);
                stocklist.sort(StockUtil.StockDateComparator);
            }
            */
            // the main list, based on freshest or specific date.

            /*
             * For all days with intervals
             * Make stock lists based on the intervals
             */
            
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
        ResultItemNot ri = new ResultItemNot();
        //ri.add("Id");
        String delta = "Delta";
        delta = "Δ";
        ri.add(Constants.IMG);
        ri.add("Name 1");
        ri.add("Name 2");
        //ri.add("Date");
        ri.add("Period");
        ri.add("Size");
        ri.add("Paired t");
        ri.add("P-value");
        ri.add("Alpha 0.05");
        ri.add("Paired t (e)");
        ri.add("P-value (e)");
        ri.add("Alpha 0.05 (e)");
        ri.add("Spearman (e)");
        ri.add("Kendall (e)");
        ri.add("Pearson (e)");
        ResultItemTableRow ri2head = new ResultItemTableRow();
        ri2head.cols = ri.get();
        table.add(ri2head);
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
