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
import roart.util.TaUtil;
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

    private Date mydate = null;

    private String mymarket = "0";

    private Integer mydays = 60;

    private Integer mytopbottom = 10;

    private Integer mytabledays = 60;

    private Integer mytablemoveintervaldays = 5;

    private Integer mytableintervaldays = 1;

    private boolean myequalize = false;

    private boolean mygraphequalize = false;

    private boolean mygraphequalizeunify = false;

    private boolean macdEnabled = true;
    
    private boolean moveEnabled = true;
    
    private boolean rsiEnabled = true;
    
    /**
     * Set current date
     * 
     * @param date
     */
    
    public void setdate(Date date) {
        mydate = date;
    }

    /**
     * Get current date
     * 
     * @return date
     */
    
    public Date getdate() {
        return mydate;
    }


    public void setMarket(String value) {
        mymarket = value;
    }

    public String getMarket() {
        return mymarket;
    }
    
    public void setDays(Integer integer) {
        mydays = integer;
    }

    public int getDays() {
        return mydays;
    }

    public void setTopBottom(Integer integer) {
        mytopbottom = integer;
    }

    public int getTopBottom() {
        return mytopbottom;
    }

    public void setTableDays(Integer integer) {
        mytabledays = integer;
    }

    public int getTableDays() {
        return mytabledays;
    }

    public void setTableIntervalDays(Integer integer) {
        mytableintervaldays = integer;
    }

    public int getTableIntervalDays() {
        return mytableintervaldays;
    }

    public void setTableMoveIntervalDays(Integer integer) {
        mytablemoveintervaldays = integer;
    }

    public int getTableMoveIntervalDays() {
        return mytablemoveintervaldays;
    }

    public void setEqualize(Boolean integer) {
        myequalize = integer;
    }

    public boolean isEqualize() {
        return myequalize;
    }

    public void setMoveEnabled(Boolean bool) {
        moveEnabled = bool;
    }

    public boolean isMoveEnabled() {
        return moveEnabled;
    }

    public void setMACDenabled(Boolean bool) {
        macdEnabled = bool;
    }

    public boolean isMACDenabled() {
        return macdEnabled;
    }

    public void setRSIenabled(Boolean bool) {
        rsiEnabled = bool;
    }

    public boolean isRSIenabled() {
        return rsiEnabled;
    }

   public void setGraphEqualize(Boolean integer) {
        mygraphequalize = integer;
    }

    public boolean isGraphEqualize() {
        return mygraphequalize;
    }

    public void setGraphEqUnify(Boolean integer) {
        mygraphequalizeunify = integer;
    }

    public boolean isGraphEqUnify() {
        return mygraphequalizeunify;
    }

    /**
     * Create result lists
     * 
     * @return the tabular result lists
     */

    public List getContent() {
        log.info("mydate " + getdate());
        List<Stock> stocks = null;
        try {
            stocks = Stock.getAll(getMarket());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        if (stocks == null) {
            return null;
        }
        log.info("stocks " + stocks.size());
        String[] periodText = getPeriodText(getMarket());
        Set<String> markets = new HashSet();
        markets.add(getMarket());
        Integer days = getDays();
        List<ResultItem> retList = new ArrayList<ResultItem>();
        ResultItem ri = new ResultItem();
        //ri.add("Id");
        ri.add(Constants.IMG);
        ri.add("Name");
        ri.add("Date");
        try {
            for (int i = 0; i < StockUtil.PERIODS; i++) {
                if (StockUtil.hasStockPeriod(stocks, i)) {
                    ri.add(periodText[i]);
                    if (isMoveEnabled()) {
                        String delta = "Delta";
                        delta = "Δ";
                        ri.add(delta + periodText[i]);                        
                    }
                    if (isMACDenabled()) {
                        ri.add(periodText[i] + " mom");
                    }
                    if (isRSIenabled()) {
                        ri.add(periodText[i] + " RSI");
                    }
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
		if (isMACDenabled()) {
		    ri.add("Price" + " mom");
		}
		if (isRSIenabled()) {
		    ri.add("Price" + " RSI");
		}
                ri.add("Currency");
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }

        retList.add(ri);
        try {
            Map<String, List<Stock>> stockidmap = StockUtil.splitId(stocks);
            Map<String, List<Stock>> stockdatemap = StockUtil.splitDate(stocks);
            if (getdate() == null) {
                SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
                String date = null;
                TreeSet set = new TreeSet<String>(stockdatemap.keySet());
                List<String> list = new ArrayList(set);
                int size = list.size();
                date = list.get(size - 1);
                setdate(dt.parse(date));
                log.info("mydate2 " + getdate());
            }

            Map<String, MarketData> marketdatamap = null;
            try {
                marketdatamap = getMarketdatamap(days, markets);
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

            List<Stock> datedstocklists[] = StockUtil.getDatedstocklists(stockdatemap, getdate(), 2, getTableMoveIntervalDays());

            List<Stock>[][] stocklistPeriod = StockUtil.getListSorted(datedstocklists, 2);
            Map<String, Integer>[][] periodmaps = StockUtil.getListMove(datedstocklists, 2, stocklistPeriod);
            Map<String, Integer>[] periodmap = periodmaps[0];

            List<Stock> datedstocks = datedstocklists[0];
            List<Stock> datedstocksoffset = datedstocklists[1];
            if (datedstocks == null) {
                return null;
            }
            //log.info("sizes " + stocks.size() + " " + datedstocks.size() + " " + datedstocksoffset.size());
            for (Stock stock : datedstocks) {
                //System.out.println("" + mydate.getTime() + "|" + stock.getDate().getTime());
                if (getMarket() == null) {
                    continue;
                }
                if (false &&  getdate() != null && getdate().getTime() != stock.getDate().getTime()) {
                    continue;
                }
                ResultItem r = new ResultItem();
                r.add(stock.getId());
                r.add(stock.getName());
                SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
                r.add(dt.format(stock.getDate()));
                try {
                    for (int i = 0; i < StockUtil.PERIODS; i++) {
                        if (StockUtil.hasStockPeriod(stocks, i)) {
                            r.add(StockDao.getPeriod(stock, i));
                            if (isMoveEnabled()) {
                                r.add(periodmap[i].get(stock.getId()));
                            }
                            if (isMACDenabled()) {
                                TaUtil tu = new TaUtil();
                                String market = getMarket();
                                String id = stock.getId();
                                Pair pair = new Pair(market, id);
                                Set ids = new HashSet();
                                ids.add(pair);
                                String periodstr = periodText[i];
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
                                String periodstr = periodText[i];
                                PeriodData perioddata = periodDataMap.get(periodstr);
                                double rsi = tu.getRSI2(days, market, id, ids, marketdatamap, perioddata, periodstr);
                                r.add(rsi);
                            }
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
     * 
     * @return the image list
     */

    public List getContentGraph() {
        List retlist = new ArrayList<>();
        try {
            List<Stock> stocks = Stock.getAll(getMarket());
            log.info("stocks " + stocks.size());
            String[] periodText = getPeriodText(getMarket());
            Map<String, List<Stock>> stockidmap = StockUtil.splitId(stocks);
            Map<String, List<Stock>> stockdatemap = StockUtil.splitDate(stocks);

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
            
            List<Stock> datedstocklistsmove[] = StockUtil.getDatedstocklists(stockdatemap, getdate(), days, getTableMoveIntervalDays());
            
            List<Stock>[][] stocklistPeriod = StockUtil.getListSorted(datedstocklistsmove, days);
            Map<String, Integer>[][] periodmaps = StockUtil.getListMove(datedstocklistsmove, days, stocklistPeriod);
            SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
            String date0 = null;
            String date1 = null;
            for (int i = days - 1; i > 0; i--) {
                if (!stocklistPeriod[0][i].isEmpty()) {
                    date0 = dt.format(stocklistPeriod[0][i].get(0).getDate());
                    break;
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
                    StreamResource r = SvgUtil.chartToResource(c, "/tmp/new2"+i+".svg", days, topbottom, getTableDays(), getTopBottom());
                    retlist.add(r);
                }
            }

            for (int i = 0; i < StockUtil.PERIODS; i++) {
                DefaultCategoryDataset dataset = StockUtil.getBottomChart(days, topbottom,
                        stocklistPeriod, i);
                if (dataset != null) {
                    JFreeChart c = SvgUtil.getChart(dataset, "Bottom period " + periodText[i], "Time " + date0 + " - " + date1, "Value", days, topbottom);
                    StreamResource r = SvgUtil.chartToResource(c, "/tmp/new3"+i+".svg", days, topbottom, getTableDays(), getTopBottom());
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

            /*
             * no rising anymore
            for (int i = 0; i < StockUtil.PERIODS; i++) {
                Map<String, Integer> mymap = new HashMap<String, Integer>();
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
                    StreamResource r = SvgUtil.chartToResource(c, "/tmp/new5" + i +".svg", days, topbottom, getTableDays(), getTopBottom());
                    retlist.add(r);
                }
            }
                 */

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

    public List getContentGraph(Set<Pair> ids) {
        List retlist = new ArrayList<>();
        try {
            log.info("mydate " + getdate());
            int days = getTableDays();
            Set<String> markets = getMarkets(ids);
            Map<String, MarketData> marketdatamap = getMarketdatamap(days,
                    markets);
            Map<String, PeriodData> periodDataMap = getPerioddatamap(markets,
                    marketdatamap);
            int topbottom = getTopBottom();
            System.out.println("per2 " + periodDataMap.keySet());
            for (String periodText : periodDataMap.keySet()) {
                System.out.println("per3 " + periodText);
                PeriodData perioddata = periodDataMap.get(periodText);
                //System.out.println("pairsize " + periodText + " " + perioddata.pairs.size());
                DefaultCategoryDataset dataset = StockUtil.getFilterChartPeriod(days, ids, marketdatamap, perioddata);
                if (dataset != null) {
                    JFreeChart c = SvgUtil.getChart(dataset, "Period " + periodText, "Time " + perioddata.date0 + " - " + perioddata.date1, "Value", days, topbottom);
                    StreamResource r = SvgUtil.chartToResource(c, "/tmp/new20"+".svg", days, topbottom, getTableDays(), getTopBottom());
                    retlist.add(r);
                }
                if (isMACDenabled()) {
                    TaUtil tu = new TaUtil();
                    for (Pair id : ids) {
                        String market = (String) id.getFirst();
                        String stockid = (String) id.getSecond();
                        dataset = tu.getMACDChart(days, market, stockid, ids, marketdatamap, perioddata, periodText);
                        if (dataset != null) {
                            JFreeChart c = SvgUtil.getChart(dataset, "Period " + periodText, "Time " + perioddata.date0 + " - " + perioddata.date1, "Value", days, 1);
                            StreamResource r = SvgUtil.chartToResource(c, "/tmp/new20"+".svg", days, topbottom, getTableDays(), 1);
                            retlist.add(r);
                        }
                    }
                }
                if (isRSIenabled()) {
                    TaUtil tu = new TaUtil();
                    for (Pair id : ids) {
                        String market = (String) id.getFirst();
                        String stockid = (String) id.getSecond();
                        dataset = tu.getRSIChart(days, market, stockid, ids, marketdatamap, perioddata, periodText);
                        if (dataset != null) {
                            JFreeChart c = SvgUtil.getChart(dataset, "Period " + periodText, "Time " + perioddata.date0 + " - " + perioddata.date1, "Value", days, 1);
                            StreamResource r = SvgUtil.chartToResource(c, "/tmp/new20"+".svg", days, topbottom, getTableDays(), 1);
                            retlist.add(r);
                        }
                    }
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
                    StreamResource r = SvgUtil.chartToResource(c, "/tmp/new20"+ 1 +".svg", days, topbottom, getTableDays(), getTopBottom());
                    retlist.add(r);
                }
                if (dataset != null && dataseteq != null) {
                    JFreeChart c = SvgUtil.getChart(dataseteq, "Index", "Time " + perioddata.date0 + " - " + perioddata.date1, "Value", days, topbottom);
                    StreamResource r = SvgUtil.chartToResource(c, "/tmp/new20"+ 1 +".svg", days, topbottom, getTableDays(), getTopBottom());
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
                    StreamResource r = SvgUtil.chartToResource(c, "/tmp/new20"+ 1 +".svg", days, topbottom, getTableDays(), getTopBottom());
                    retlist.add(r);
                }
                if (dataset != null && dataseteq != null) {
                    String currency = null; //datedstocklists[0].get(0).getCurrency();
                    if (currency == null) {
                        currency = "Value";
                    }
                    JFreeChart c = SvgUtil.getChart(dataseteq, "Price", "Time " + perioddata.date0 + " - " + perioddata.date1, currency, days, topbottom);
                    StreamResource r = SvgUtil.chartToResource(c, "/tmp/new20"+ 1 +".svg", days, topbottom, getTableDays(), getTopBottom());
                    retlist.add(r);
                }
                if (isMACDenabled()) {
                    TaUtil tu = new TaUtil();
                    for (Pair id : ids) {
                        String market = (String) id.getFirst();
                        String stockid = (String) id.getSecond();
                        dataset = tu.getMACDChart(days, market, stockid, ids, marketdatamap, perioddata, "price");
                        if (dataset != null) {
                            JFreeChart c = SvgUtil.getChart(dataset, "price", "Time " + perioddata.date0 + " - " + perioddata.date1, "Value", days, 1);
                            StreamResource r = SvgUtil.chartToResource(c, "/tmp/new20"+".svg", days, topbottom, getTableDays(), 1);
                            retlist.add(r);
                        }
                    }
                }
                if (isRSIenabled()) {
                    TaUtil tu = new TaUtil();
                    for (Pair id : ids) {
                        String market = (String) id.getFirst();
                        String stockid = (String) id.getSecond();
                        dataset = tu.getRSIChart(days, market, stockid, ids, marketdatamap, perioddata, "price");
                        if (dataset != null) {
                            JFreeChart c = SvgUtil.getChart(dataset, "price", "Time " + perioddata.date0 + " - " + perioddata.date1, "Value", days, 1);
                            StreamResource r = SvgUtil.chartToResource(c, "/tmp/new20"+".svg", days, topbottom, getTableDays(), 1);
                            retlist.add(r);
                        }
                    }
                }
            }
            if (isGraphEqualize()) {
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }

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
                String text = "price";
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
        }
        System.out.println("per " + periodDataMap.keySet());
        return periodDataMap;
    }

    private Map<String, MarketData> getMarketdatamap(int days,
            Set<String> markets) throws Exception {
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
            
            List<Stock> datedstocklists[] = StockUtil.getDatedstocklists(stockdatemap, getdate(), days + 15, getTableIntervalDays());
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

    private Set<String> getMarkets(Set<Pair> ids) {
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

    public List getContentStat() {
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
            List<Stock> stocks = Stock.getAll(getMarket());
            log.info("stocks " + stocks.size());
            Map<String, List<Stock>> stockidmap = StockUtil.splitId(stocks);
            Map<String, List<Stock>> stockdatemap = StockUtil.splitDate(stocks);

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
            
            List<Stock> datedstocklists[] = StockUtil.getDatedstocklists(stockdatemap, getdate(), days, getTableIntervalDays());
            
            List<Stock> datedstocks = datedstocklists[0];
            if (datedstocks == null) {
                return null;
            }
            Math3Util.getStats(retList, getdate(), days, stockidmap, stockdatemap);
            
            log.info("retlist " +retList.size());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        List<List> retlistlist = new ArrayList<List>();
        retlistlist.add(retList);
        return retlistlist;
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
