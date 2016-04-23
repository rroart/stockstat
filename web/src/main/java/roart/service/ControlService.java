package roart.service;

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
import java.util.concurrent.TimeUnit;
import java.io.*;

import roart.util.Constants;
import roart.util.StockDao;
import roart.util.StockUtil;
import roart.util.SvgUtil;

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
        ri.add("Percent");
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

    public void setdate(Date date) {
        this.mydate = date;
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
        //mydate.setHours(0);
        //mydate.setMinutes(0);
        //mydate.setSeconds(0);
        List<ResultItem> retList = new ArrayList<ResultItem>();
        ResultItem ri = new ResultItem();
        //ri.add("Id");
        String delta = "Delta";
        delta = "Δ";
        ri.add("Name");
        ri.add("Date");
        ri.add("Period1");
        ri.add(delta + "1");
        ri.add("Period2");
        ri.add(delta + "2");
        ri.add("Period3");
        ri.add(delta + "3");
        ri.add("Period4");
        ri.add(delta + "4");
        ri.add("Period5");
        ri.add(delta + "5");
        ri.add("Price");
        ri.add("Currency");
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
            // the main list, based on freshest or specific date.
            List<Stock>[] datedstocklists = new ArrayList[2];

            //List<Stock> datedstocksoffset = new ArrayList<Stock>();

            HashMap<String, Integer>[][] periodmaps = StockUtil.getListAndDiff(datedstocklists, stockidmap, stockdatemap, 2, mydays, null);
            HashMap<String, Integer>[] periodmap = periodmaps[0];

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
                //r.add(stock.getId());
                r.add(stock.getName());
                SimpleDateFormat dt = new SimpleDateFormat("yyyy.MM.dd");
                r.add(dt.format(stock.getDate()));
                for (int i = 0; i < StockUtil.PERIODS; i++) {
                    r.add(StockDao.getPeriod(stock, i + 1));
                    r.add(periodmap[i].get(stock.getId()));
                }
                r.add(stock.getPrice());
                r.add(stock.getCurrency());
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
     * Create result graphs
     * 
     * @return the image list
     */

    public static List getContentGraph() {
        //mydate.setHours(0);
        //mydate.setMinutes(0);
        //mydate.setSeconds(0);
        List retlist = new ArrayList<>();
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
            // the main list, based on freshest or specific date.
            List<Stock>[] datedstocklists = new ArrayList[10];

            //List<Stock> datedstocksoffset = new ArrayList<Stock>();

            Object[] arr = new Object[1];
            int days = getTableDays();
            int topbottom = getTopBottom();
            HashMap<String, Integer>[][] periodmaps = StockUtil.getListAndDiff(datedstocklists, stockidmap, stockdatemap, days, getTableIntervalDays(), arr);
            HashMap<String, Integer>[] periodmap = periodmaps[0];
            List<Stock>[][] stocklistPeriod = (List<Stock>[][]) arr[0];
            SimpleDateFormat dt = new SimpleDateFormat("yyyy.MM.dd");
            String date0 = null;
            String date1 = null;
            if (!stocklistPeriod[0][days - 1].isEmpty()) {
                date0 = dt.format(stocklistPeriod[0][days - 1].get(0).getDate());
            }
            if (!stocklistPeriod[0][0].isEmpty()) {
                date1 = dt.format(stocklistPeriod[0][0].get(0).getDate());
            }

            List<Stock> datedstocks = datedstocklists[0];
            List<Stock> datedstocksoffset = datedstocklists[1];
            //log.info("sizes " + stocks.size() + " " + datedstocks.size() + " " + datedstocksoffset.size());

            for (int i = 0; i < StockUtil.PERIODS; i++) {
                DefaultCategoryDataset dataset = StockUtil.getTopChart(days, topbottom,
                        stocklistPeriod, i);
                if (dataset != null) {
                    JFreeChart c = SvgUtil.getChart(dataset, "Top period " + (i + 1), "Time " + date0 + " - " + date1, "Percent", days, topbottom);
                    StreamResource r = SvgUtil.chartToResource(c, "/tmp/new2"+i+".svg", days, topbottom);
                    retlist.add(r);
                }
            }

            for (int i = 0; i < StockUtil.PERIODS; i++) {
                DefaultCategoryDataset dataset = StockUtil.getBottomChart(days, topbottom,
                        stocklistPeriod, i);
                if (dataset != null) {
                    JFreeChart c = SvgUtil.getChart(dataset, "Bottom period " + (i + 1), "Time " + date0 + " - " + date1, "Percent", days, topbottom);
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
           JFreeChart c3 = SvgUtil.getChart(dataset4, "Top change", "Time", "Percent", days, topbottom);
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
                    JFreeChart c = SvgUtil.getChart(dataset, "Top climber period " + (i + 1), "Time " + date0 + " - " + date1, "Percent", days, topbottom);
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

    static Integer mydays = 2;

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

    static Integer mytableintervaldays = 1;

    public void setTableIntervalDays(Integer integer) {
        mytableintervaldays = integer;
    }

    public static int getTableIntervalDays() {
        return mytableintervaldays;
    }

    static Integer mytodayzero = 1;

    public void setTodayZero(Integer integer) {
        mytodayzero = integer;
    }

    public static int getTodayZero() {
        return mytodayzero;
    }

}
