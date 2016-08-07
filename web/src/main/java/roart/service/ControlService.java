package roart.service;

import roart.model.Meta;
import roart.model.ResultItem;
import roart.model.Stock;

import javax.servlet.http.*;

import java.text.SimpleDateFormat;
import java.util.Date;
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
import roart.util.Math3Util;
import roart.util.MetaDao;
import roart.util.PeriodData;

import org.apache.commons.math3.util.Pair;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.StreamResource;

public class ControlService {
    private static Logger log = LoggerFactory.getLogger(ControlService.class);

    public enum Config { REINDEXLIMIT, INDEXLIMIT, FAILEDLIMIT, OTHERTIMEOUT, TIKATIMEOUT, MLTCOUNT, MLTMINTF, MLTMINDF };
    public static Map<Config, Integer> configMap = new HashMap<Config, Integer>();
    public static Map<Config, String> configStrMap = new HashMap<Config, String>();

    private static volatile Integer writelock = new Integer(-1);

    private static int dirsizelimit = 100;

    private static volatile int mycounter = 0;

    public static int getMyCounter() {
        return mycounter++;
    }

    // called from ui
    public void overlapping() {
    }

    @SuppressWarnings("rawtypes")
    public List<List> overlappingDo() {
        List<ResultItem> retList = new ArrayList<ResultItem>();
        ResultItem ri = new ResultItem();
        ri.add("Value");
        ri.add("Count");
        ri.add("Directory 1");
        ri.add("Directory 2");
        retList.add(ri);

        try {
            List<Stock> stock = Stock.getAll();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }

        List<List> retlistlist = new ArrayList<List>();
        retlistlist.add(retList);
        return retlistlist;
    }

    public void dbindex(String md5) throws Exception {
    }

    public void dbsearch(String md5) throws Exception {
    }

    public static Date mydate = null; //new Date();

    public static void setdate(Date date) {
        mydate = date;
    }

    public static Date getdate() {
        return mydate;
    }

    /**
     * Create result lists
     * 
     * @return the tabular result lists
     */

    public static List getContent() {
        List<Stock> stocks = null;
        try {
            stocks = Stock.getAll(mymarket);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        log.info("stocks " + stocks.size());
        String[] periodText = getPeriodText(mymarket);
        List<ResultItem> retList = new ArrayList<ResultItem>();
        ResultItem ri = new ResultItem();
        //ri.add("Id");
        String delta = "Delta";
        delta = "Δ";
        ri.add(Constants.IMG);
        ri.add("Name");
        ri.add("Date");
        try {
            for (int i = 0; i < StockUtil.PERIODS; i++) {
                if (StockUtil.hasStockPeriod(stocks, i + 1)) {
                    ri.add(periodText[i]);
                    ri.add(delta + periodText[i]);
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        
        
        try {
            if (StockUtil.hasSpecial(stocks, Constants.INDEXVALUE)) {
                ri.add("Index");
            }
            if (StockUtil.hasSpecial(stocks, Constants.PRICE)) {
                ri.add("Price");
                ri.add("Currency");
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        
        retList.add(ri);
        try {
            HashMap<String, List<Stock>> stockidmap = StockUtil.splitId(stocks);
            HashMap<String, List<Stock>> stockdatemap = StockUtil.splitDate(stocks);
            if (getdate() == null) {
                SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
                String date = null;
                TreeSet set = new TreeSet<String>(stockdatemap.keySet());
                List<String> list = new ArrayList(set);
                int size = list.size();
                date = list.get(size - 1);
                setdate(dt.parse(date));
            }

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
            
            List<Stock> datedstocklists[] = StockUtil.getDatedstocklists(stockdatemap, 2, mytableintervaldays);
            
            Map<String, Integer>[][] periodmaps = StockUtil.getListAndDiff(datedstocklists, 2, null);
            Map<String, Integer>[] periodmap = periodmaps[0];

            List<Stock> datedstocks = datedstocklists[0];
            List<Stock> datedstocksoffset = datedstocklists[1];
            if (datedstocks == null) {
                return null;
            }
            //log.info("sizes " + stocks.size() + " " + datedstocks.size() + " " + datedstocksoffset.size());
            for (Stock stock : datedstocks) {
                //System.out.println("" + mydate.getTime() + "|" + stock.getDate().getTime());
                if (mymarket == null) {
                    continue;
                }
                if (false &&  mydate != null && mydate.getTime() != stock.getDate().getTime()) {
                    continue;
                }
                ResultItem r = new ResultItem();
                r.add(stock.getId());
                r.add(stock.getName());
                SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
                r.add(dt.format(stock.getDate()));
		try {
		    for (int i = 0; i < StockUtil.PERIODS; i++) {
			if (StockUtil.hasStockPeriod(stocks, i + 1)) {
			    r.add(StockDao.getPeriod(stock, i + 1));
			    r.add(periodmap[i].get(stock.getId()));
			}
		    }
		} catch (Exception e) {
		    log.error(Constants.EXCEPTION, e);
		}

		try {
		    if (StockUtil.hasSpecial(stocks, Constants.INDEXVALUE)) {
			r.add(stock.getIndexvalue());
		    }

		    if (StockUtil.hasSpecial(stocks, Constants.PRICE)) {
			r.add(stock.getPrice());
			r.add(stock.getCurrency());
		    }
		} catch (Exception e) {
		    log.error(Constants.EXCEPTION, e);
		}
		
                //r.add(stock.get());
                retList.add(r);

            }
            log.info("retlist " +retList.size());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<List> retlistlist = new ArrayList<List>();
        retlistlist.add(retList);
        return retlistlist;
    }

    /**
     * Get the period field text based on the eventual metadata
     * 
     * @return the period text fields
     * @param market
     */
    
    private static String[] getPeriodText(String market) {
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
     * 
     * @return the image list
     */

    public static List getContentGraph() {
        List retlist = new ArrayList<>();
        try {
            List<Stock> stocks = Stock.getAll(mymarket);
            log.info("stocks " + stocks.size());
            String[] periodText = getPeriodText(mymarket);
            HashMap<String, List<Stock>> stockidmap = StockUtil.splitId(stocks);
            HashMap<String, List<Stock>> stockdatemap = StockUtil.splitDate(stocks);

            // sort based on date
            for (String key : stockidmap.keySet()) {
                List<Stock> stocklist = stockidmap.get(key);
                stocklist.sort(StockUtil.StockDateComparator);
            }

            //List<Stock> datedstocksoffset = new ArrayList<Stock>();

            Object[] arr = new Object[1];
            int days = getTableDays();
            int topbottom = getTopBottom();

            // the main list, based on freshest or specific date.

            /*
             * For all days with intervals
             * Make stock lists based on the intervals
             */
            
            List<Stock> datedstocklists[] = StockUtil.getDatedstocklists(stockdatemap, days, mytableintervaldays);
            
            Map<String, Integer>[][] periodmaps = StockUtil.getListAndDiff(datedstocklists, days, arr);
            List<Stock>[][] stocklistPeriod = (List<Stock>[][]) arr[0];
            SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
            String date0 = null;
            String date1 = null;
            for (int i = days - 1; i > 0; i--) {
                if (!stocklistPeriod[0][i].isEmpty()) {
                    date0 = dt.format(stocklistPeriod[0][i].get(0).getDate());
                }
            }
            if (!stocklistPeriod[0][0].isEmpty()) {
                date1 = dt.format(stocklistPeriod[0][0].get(0).getDate());
            }

            for (int i = 0; i < StockUtil.PERIODS; i++) {
                DefaultCategoryDataset dataset = StockUtil.getTopChart(days, topbottom,
                        stocklistPeriod, i);
                if (dataset != null) {
                    JFreeChart c = SvgUtil.getChart(dataset, "Top period " + periodText[i], "Time " + date0 + " - " + date1, "Value", days, topbottom);
                    StreamResource r = SvgUtil.chartToResource(c, "/tmp/new2"+i+".svg", days, topbottom);
                    retlist.add(r);
                }
            }

            for (int i = 0; i < StockUtil.PERIODS; i++) {
                DefaultCategoryDataset dataset = StockUtil.getBottomChart(days, topbottom,
                        stocklistPeriod, i);
                if (dataset != null) {
                    JFreeChart c = SvgUtil.getChart(dataset, "Bottom period " + periodText[i], "Time " + date0 + " - " + date1, "Value", days, topbottom);
                    StreamResource r = SvgUtil.chartToResource(c, "/tmp/new3"+i+".svg", days, topbottom);
                    retlist.add(r);
                }
            }

            /*
            DefaultCategoryDataset dataset4 = new DefaultCategoryDataset( );
           for (int j = 0; j < days - 1; j++) {
          List<Stock> list2 = stocklistPeriod[0][0];
           for (int i = 0; i < topbottom; i++) {
               if (i < list2.size()) {
               Stock stock = list2.get(i);
               dataset4.addValue(periodmaps[j][0].get(stock.getId()), stock.getName() , new Integer(j));
               //System.out.println("test " + periodmaps[j][0].get(stock.getId()) + " " + stock.getName() + " " + stock.getDate());               
               }
           }
           }
           JFreeChart c3 = SvgUtil.getChart(dataset4, "Top change", "Time", "Value", days, topbottom);
           StreamResource r4 = SvgUtil.chartToResource(c3, "/tmp/new4.svg", days, topbottom);
           retlist.add(r4);
             */

            for (int i = 0; i < StockUtil.PERIODS; i++) {
                HashMap<String, Integer> mymap = new HashMap<String, Integer>();
                for (int j = 0; j < days - 1; j++) {
                    for (String id : periodmaps[j][i].keySet()) {
                        Integer rise = mymap.get(id);
                        if (rise == null) {
                            rise = new Integer(0);
                            mymap.put(id, rise);
                        }
                        rise = rise + periodmaps[j][i].get(id);
                        mymap.put(id, rise);
                    }
                }

                DefaultCategoryDataset dataset = StockUtil.getRisingChart(days, topbottom,
                        stocklistPeriod, mymap, i);
                if (dataset != null) {
                    JFreeChart c = SvgUtil.getChart(dataset, "Top climber period " + periodText[i], "Time " + date0 + " - " + date1, "Value", days, topbottom);
                    StreamResource r = SvgUtil.chartToResource(c, "/tmp/new5" + i +".svg", days, topbottom);
                    retlist.add(r);
                }
            }

            boolean percentage = true;
            int percent = 0;
            if (percentage) {
                percent = 100;
            }
            /*
        int j = 0;
        List<Stock> datedstocks2 = new ArrayList<Stock>();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        for (String key : stockmap.keySet()) {
            List<Stock> stocklist = stockmap.get(key);
            System.out.println("key " + key + " " + stocklist.size());
            //double max = getMax(stocklist, 0, percentage);
            double max = getMax(stocklist, 0, false);
            for (int i = 0; i < stocklist.size() && i < mytabledays ; i++) {
                Stock stock = stocklist.get(i);
                if (stock.getPeriod1() != null) {
                    //dataset.addValue( 100 * (stock.getPeriod1() + percent) / max , stock.getName() , new Integer(i)  );
                    //System.out.println("test " + stock.getPeriod1() + " " + 100 * stock.getPeriod1() / max + " " + stock.getName() + " " + stock.getDate());
                        dataset.addValue(stock.getPeriod1(), stock.getName() , new Integer(i)  );
                //System.out.println("test " + stock.getPeriod1() + " " + stock.getName() + " " + stock.getDate());
                }
            }
            j++;
            if (j > mytopbottom) {
                break;
            }
        }
        JFreeChart c = SvgUtil.getChart(dataset);
        StreamResource r2 = SvgUtil.chartToResource(c, "/tmp/new.svg");
             */

        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        
        return retlist;
    }

    /**
     * Create result graphs for one
     * 
     * myid the id of the unit
     * @return the image list
     */

    public static List getContentGraph(Set<Pair> ids) {
        List retlist = new ArrayList<>();
        //List<String> ids = new ArrayList<String>();
        Set<String> markets = new HashSet<String>();
        for (Pair idpair : ids) {
            markets.add((String) idpair.getFirst());
            //ids.add((String) idpair.getSecond());
        }
        int days = getTableDays();
        /*
        String date0 = null;
        String date1 = null;
        */
        try {
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
                
                List<Stock> datedstocklists[] = StockUtil.getDatedstocklists(stockdatemap, days, mytableintervaldays);
                marketdata.datedstocklists = datedstocklists;
                
                //Object[] arr = new Object[1];
                //Map<String, Integer>[][] periodmaps = StockUtil.getListAndDiff(datedstocklists, stockidmap, stockdatemap, days, getTableIntervalDays(), arr);
                //HashMap<String, Integer>[] periodmap = periodmaps[0];
                //List<Stock>[][] stocklistPeriod = (List<Stock>[][]) arr[0];
                //marketdata.stocklistperiod = stocklistPeriod;

                marketdatamap.put(market,  marketdata);
            }
            //System.out.println("siz " + marketdatamap.size());
            Map<String, PeriodData> periodDataMap = new HashMap();
            for (String market : markets) {
                //System.out.println("market " + market);
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
            }
            int topbottom = getTopBottom();
            for (String periodText : periodDataMap.keySet()) {
                PeriodData perioddata = periodDataMap.get(periodText);
                //System.out.println("pairsize " + periodText + " " + perioddata.pairs.size());
                DefaultCategoryDataset dataset = StockUtil.getFilterChartPeriod(days, ids, marketdatamap, perioddata);
                if (dataset != null) {
                    JFreeChart c = SvgUtil.getChart(dataset, "Period " + periodText, "Time " + perioddata.date0 + " - " + perioddata.date1, "Value", days, topbottom);
                    StreamResource r = SvgUtil.chartToResource(c, "/tmp/new20"+".svg", days, topbottom);
                    retlist.add(r);
                }
            }
            if (StockUtil.hasSpecial(marketdatamap, Constants.INDEXVALUE)) {
                PeriodData perioddata = new PeriodData();
                DefaultCategoryDataset dataseteq = null;
                if (isGraphEqualize()) {    
                    dataseteq = new DefaultCategoryDataset( );
                }
                DefaultCategoryDataset dataset = StockUtil.getFilterChartDated(days, ids, marketdatamap, perioddata, Constants.INDEXVALUE, isGraphEqualize(), dataseteq);
                if (dataset != null) {
                    JFreeChart c = SvgUtil.getChart(dataset, "Index", "Time " + perioddata.date0 + " - " + perioddata.date1, "Value", days, topbottom);
                    StreamResource r = SvgUtil.chartToResource(c, "/tmp/new20"+ 1 +".svg", days, topbottom);
                    retlist.add(r);
                }
                if (dataset != null && dataseteq != null) {
                    JFreeChart c = SvgUtil.getChart(dataseteq, "Index", "Time " + perioddata.date0 + " - " + perioddata.date1, "Value", days, topbottom);
                    StreamResource r = SvgUtil.chartToResource(c, "/tmp/new20"+ 1 +".svg", days, topbottom);
                    retlist.add(r);
                }
            }
            if (StockUtil.hasSpecial(marketdatamap, Constants.PRICE)) {
                PeriodData perioddata = new PeriodData();
                DefaultCategoryDataset dataseteq = null;
                if (isGraphEqualize()) {    
                    dataseteq = new DefaultCategoryDataset( );
                }
                DefaultCategoryDataset dataset = StockUtil.getFilterChartDated(days, ids, marketdatamap, perioddata, Constants.PRICE, isGraphEqualize(), dataseteq);
                if (dataset != null) {
                    String currency = null; //datedstocklists[0].get(0).getCurrency();
                    if (currency == null) {
                        currency = "Value";
                    }
                    JFreeChart c = SvgUtil.getChart(dataset, "Price", "Time " + perioddata.date0 + " - " + perioddata.date1, currency, days, topbottom);
                    StreamResource r = SvgUtil.chartToResource(c, "/tmp/new20"+ 1 +".svg", days, topbottom);
                    retlist.add(r);
                }
                if (dataset != null && dataseteq != null) {
                    String currency = null; //datedstocklists[0].get(0).getCurrency();
                    if (currency == null) {
                        currency = "Value";
                    }
                    JFreeChart c = SvgUtil.getChart(dataseteq, "Price", "Time " + perioddata.date0 + " - " + perioddata.date1, currency, days, topbottom);
                    StreamResource r = SvgUtil.chartToResource(c, "/tmp/new20"+ 1 +".svg", days, topbottom);
                    retlist.add(r);
                }
            }
            if (isGraphEqualize()) {
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        
        return retlist;
    }

    /**
     * Create stat result lists
     * 
     * @return the tabular result lists
     */

    public static List getContentStat() {
        //mydate.setHours(0);
        //mydate.setMinutes(0);
        //mydate.setSeconds(0);
        List<ResultItem> retList = new ArrayList<ResultItem>();
        ResultItem ri = new ResultItem();
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
        retList.add(ri);
        try {
            List<Stock> stocks = Stock.getAll(mymarket);
            log.info("stocks " + stocks.size());
            HashMap<String, List<Stock>> stockidmap = StockUtil.splitId(stocks);
            HashMap<String, List<Stock>> stockdatemap = StockUtil.splitDate(stocks);

            // sort based on date
            for (String key : stockidmap.keySet()) {
                List<Stock> stocklist = stockidmap.get(key);
                stocklist.sort(StockUtil.StockDateComparator);
            }

            int days = getTableDays();

            // the main list, based on freshest or specific date.

            /*
             * For all days with intervals
             * Make stock lists based on the intervals
             */
            
            List<Stock> datedstocklists[] = StockUtil.getDatedstocklists(stockdatemap, days, mytableintervaldays);
            
            List<Stock> datedstocks = datedstocklists[0];
            if (datedstocks == null) {
                return null;
            }
            Math3Util.getStats(retList, days, stockidmap, stockdatemap);
            
            log.info("retlist " +retList.size());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<List> retlistlist = new ArrayList<List>();
        retlistlist.add(retList);
        return retlistlist;
    }

    private static void printstock(List<Stock> stocklistPeriod4Day1, int imax) {
        int i = 0;
        for (Stock s : stocklistPeriod4Day1) {
            //log.info(s.getId() + " : " + s.getName() + " : " + s.getPeriod4());
            i++;
            if (i > imax) {
                return;
            }
        }
    }

    static String mymarket = "0";

    public void setMarket(String value) {
        mymarket = value;
    }

    public static String getMarket() {
        return mymarket;
    }
    
    static Integer mydays = 5;

    public void setDays(Integer integer) {
        mydays = integer;
    }

    public int getDays() {
        return mydays;
    }

    static Integer mytopbottom = 10;

    public void setTopBottom(Integer integer) {
        mytopbottom = integer;
    }

    public static int getTopBottom() {
        return mytopbottom;
    }

    static Integer mytabledays = 10;

    public void setTableDays(Integer integer) {
        mytabledays = integer;
    }

    public static int getTableDays() {
        return mytabledays;
    }

    static Integer mytableintervaldays = 5;

    public void setTableIntervalDays(Integer integer) {
        mytableintervaldays = integer;
    }

    public static int getTableIntervalDays() {
        return mytableintervaldays;
    }

    static boolean myequalize = false;

    public void setEqualize(Boolean integer) {
        myequalize = integer;
    }

    public static boolean isEqualize() {
        return myequalize;
    }

    static boolean mygraphequalize = false;

    public void setGraphEqualize(Boolean integer) {
        mygraphequalize = integer;
    }

    public static boolean isGraphEqualize() {
        return mygraphequalize;
    }

    static boolean mygraphequalizeunify = false;

    public void setGraphEqUnify(Boolean integer) {
        mygraphequalizeunify = integer;
    }

    public static boolean isGraphEqUnify() {
        return mygraphequalizeunify;
    }

}
