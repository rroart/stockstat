package roart.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.model.Stock;
import roart.service.ControlService;

public class StockUtil {

    private static Logger log = LoggerFactory.getLogger(StockUtil.class);

    public final static int PERIODS = 5;

    /**
     * Create sorted tables for all periods in a time interval
     * 
     * @param datedstocklists returned lists based on date
     * @param stockidmap a map to stock lists, based on unit id
     * @param stockdatemap a map to stock lists, based on the date
     * @param count number of days to measure
     * @param mytableintervaldays interval between each measure
     * @param arr also for return sorted tables
     * @return sorted tables
     * @throws Exception
     */

    public static HashMap<String, Integer>[][] getListAndDiff(
            List<Stock> datedstocklists[], HashMap<String, List<Stock>> stockidmap, HashMap<String, List<Stock>> stockdatemap, int count, int mytableintervaldays, Object[] arr)
                    throws Exception {
        for (int i = 0; i < count; i ++) {
            datedstocklists[i] = new ArrayList<Stock>();          
        }
        if (!ControlService.isTodayZero()) {
            //List<Stock >datedstocks = datedstocklists[0];
            for (String key : stockidmap.keySet()) {
                List<Stock> stocklist = stockidmap.get(key);
                int index = getStockDate(stocklist, ControlService.getdate());
                if (index >= 0) {
                    Stock stock = stocklist.get(index);
                    datedstocklists[0].add(stock);
                    for (int j = 1; j < count; j++) {
                        index = index + mytableintervaldays;
                        if (index < stocklist.size()) {
                            stock = stocklist.get(index);
                            datedstocklists[j].add(stock);
                        }
                    }
                }
            }
        } else {
            /*
             * For all days with intervals
             * Make stock lists based on the intervals
             */

            List<String> list = new ArrayList(stockdatemap.keySet());
            Collections.sort(list);
            String date = null;
            Date datedate = ControlService.getdate();
            if (datedate != null) {
                SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
                date = dt.format(datedate);                
            }
            int index = getStockDate(list, date);
            if (index >= 0) {
                datedstocklists[0] = stockdatemap.get(date);
                for (int j = 1; j < count; j++) {
                    index = index - mytableintervaldays;
                    if (index >= 0) {
                        date = list.get(index);
                        datedstocklists[j] = stockdatemap.get(date);
                    }
                }
            }
        }
        Comparator[] comparators = { StockUtil.StockPeriod1Comparator, StockUtil.StockPeriod2Comparator, StockUtil.StockPeriod3Comparator, StockUtil.StockPeriod4Comparator, StockUtil.StockPeriod5Comparator };

        // make sorted period1, sorted current day
        // make sorted period1, sorted day offset
        HashMap<String, Integer>[][] periodmaps = new HashMap[count - 1][PERIODS];
        List<Stock>[][] stocklistPeriod = new ArrayList[PERIODS][count];
        if (arr != null) {
            arr[0] = stocklistPeriod;
        }

        // Do for all wanted days

        for (int j = 0; j < count; j++) {
            HashMap<String, Integer>[] periodmap = new HashMap[PERIODS];
            //List<Stock> datedstocksoffset = getOffsetList(stockidmap, mydays);
            //datedstocklists[j] = datedstocksoffset;
            boolean hasPeriod[] = new boolean[PERIODS];
            for (int i = 0; i < PERIODS; i++) {
                hasPeriod[i] = false;

                // Check if the period fot the wanted day has any content

                if (datedstocklists[j] != null) {
                    hasPeriod[i] = hasStockPeriod(datedstocklists[j], i + 1);
                }

                // If it has, add it to the table

                if (datedstocklists[j] != null && hasPeriod[i]) {
                    stocklistPeriod[i][j] = new ArrayList<Stock>(datedstocklists[j]);
                } else {
                    stocklistPeriod[i][j] = new ArrayList<Stock>();
                }

                /*
                 *  If it has, first sort it
                 *  If not the first, get a periodmap, which is a list of differences, indicating rise or decline
                 */

                if (hasPeriod[i]) {
                    stocklistPeriod[i][j].sort(comparators[i]);
                    if (j > 0) {
                        periodmap[i] = StockUtil.getPeriodmap(stocklistPeriod[i][j - 1], stocklistPeriod[i][j]);
                    }
                } else {
                    if (j > 0) {
                        periodmap[i] = new HashMap<String, Integer>();
                    }
                }
            }
            if (j > 0) {
                periodmaps[j - 1] = periodmap;
            }
        }
        return periodmaps;
    }

    /**
     * Make a map of differences between two days
     * 
     * @param stocklistPeriod1Day0 a stock list from the previous day
     * @param stocklistPeriod1Day1 a stock list from a day
     * @return a list of chart differences, indicating rise or decline
     */

    public static HashMap<String, Integer> getPeriodmap(
            List<Stock> stocklistPeriod1Day0, List<Stock> stocklistPeriod1Day1) {
        HashMap<String, Integer> periodmap = new HashMap<String, Integer>();
        for (int i = 0; i < stocklistPeriod1Day1.size(); i++) {
            for (int j = 0; j < stocklistPeriod1Day0.size(); j++) {
                if (stocklistPeriod1Day0.get(j).getId() == null) {
                    log.error("null0 " + stocklistPeriod1Day0.get(i));
                }
                if (stocklistPeriod1Day1.get(i).getId() == null) {
                    log.error("null0 " + stocklistPeriod1Day1.get(i));
                }
                if (stocklistPeriod1Day1.get(i).getId().equals(stocklistPeriod1Day0.get(j).getId())) {
                    periodmap.put(stocklistPeriod1Day1.get(i).getId(), i-j);
                }
            }
        }
        return periodmap;
    }

    private static List<Stock> getOffsetList(
            HashMap<String, List<Stock>> stockmap, int mydays) {
        List<Stock> retstocklist = new ArrayList<Stock>();
        for (String key : stockmap.keySet()) {
            Stock stock = null;
            List<Stock> stocklist = stockmap.get(key);
            if (ControlService.mydate == null) {
                if (stocklist.size() > mydays) {
                    stock = stocklist.get(mydays);
                } else {
                    continue;
                }
            } else {
                int i = StockUtil.getStockDate(stocklist, ControlService.mydate);
                if (i >= 0) {
                    if (stocklist.size() > (mydays + i)) {
                        stock = stocklist.get(mydays + i);
                    }
                } else {
                }
            }
            if (stock != null) {
                retstocklist.add(stock);
            }
        }
        return retstocklist;
    }

    /**
     * Split full stock list into stock id map to list
     * 
     * @param stocks The full stock list with market
     * @return the split list
     */

    public static HashMap<String, List<Stock>> splitId(List<Stock> stocks) {
        HashMap<String, List<Stock>> mymap = new HashMap<String, List<Stock>>();
        for (Stock stock : stocks) {
            List<Stock> stocklist = mymap.get(stock.getId());
            if (stocklist == null) {
                stocklist = new ArrayList<Stock>();
                mymap.put(stock.getId(), stocklist);
            }
            stocklist.add(stock);
        }
        return mymap;
    }

    /**
     * Split full stock list into stock date map to list
     * 
     * @param stocks The full stock list with market
     * @return the split list
     */

    public static HashMap<String, List<Stock>> splitDate(List<Stock> stocks) {
        HashMap<String, List<Stock>> mymap = new HashMap<String, List<Stock>>();
        for (Stock stock : stocks) {
            SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
            String date = dt.format(stock.getDate());
            List<Stock> stocklist = mymap.get(date);
            if (stocklist == null) {
                stocklist = new ArrayList<Stock>();
                mymap.put(date, stocklist);
            }
            stocklist.add(stock);
        }
        return mymap;
    }

    public static boolean hasStockPeriod(List<Stock> stocks, int i) throws Exception {
        for (Stock s : stocks) {
            if (StockDao.getPeriod(s, i) != null) {
                return true;
            }
            /*
        if (i == 1 && s.getPeriod1() != null) {
        return true;
        }
        if (i == 2 && s.getPeriod2() != null) {
        return true;
        }
        if (i == 3 && s.getPeriod3() != null) {
        return true;
        }
        if (i == 4 && s.getPeriod4() != null) {
        return true;
        }
        if (i == 5 && s.getPeriod5() != null) {
        return true;
        }
             */
        }
        return false;
    }

    public static boolean hasStockPeriod1(List<Stock> stocks) {
        for (Stock s : stocks) {
            if (s.getPeriod1() != null) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasStockPeriod2(List<Stock> stocks) {
        for (Stock s : stocks) {
            if (s.getPeriod2() != null) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasStockPeriod3(List<Stock> stocks) {
        for (Stock s : stocks) {
            if (s.getPeriod3() != null) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasStockPeriod4(List<Stock> stocks) {
        for (Stock s : stocks) {
            if (s.getPeriod4() != null) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasStockPeriod5(List<Stock> stocks) {
        for (Stock s : stocks) {
            if (s.getPeriod5() != null) {
                return true;
            }
        }
        return false;
    }

    public static Comparator<Stock> StockDateComparator = new Comparator<Stock>() {

        public int compare(Stock stock1, Stock stock2) {

            Date comp1 = stock1.getDate();
            Date comp2 = stock2.getDate();

            //descending order
            return comp2.compareTo(comp1);
        }

    };
    public static Comparator<Stock> StockPeriod1Comparator = new Comparator<Stock>() {

        public int compare(Stock stock1, Stock stock2) {

            Double comp1 = stock1.getPeriod1();
            Double comp2 = stock2.getPeriod1();

            return compDoubleInner(comp1, comp2);
        }

    };
    public static Comparator<Stock> StockPeriod2Comparator = new Comparator<Stock>() {

        public int compare(Stock stock1, Stock stock2) {

            Double comp1 = stock1.getPeriod2();
            Double comp2 = stock2.getPeriod2();

            return compDoubleInner(comp1, comp2);
        }

    };
    public static Comparator<Stock> StockPeriod3Comparator = new Comparator<Stock>() {

        public int compare(Stock stock1, Stock stock2) {

            Double comp1 = stock1.getPeriod3();
            Double comp2 = stock2.getPeriod3();

            return compDoubleInner(comp1, comp2);
        }

    };
    public static Comparator<Stock> StockPeriod4Comparator = new Comparator<Stock>() {

        public int compare(Stock stock1, Stock stock2) {

            Double comp1 = stock1.getPeriod4();
            Double comp2 = stock2.getPeriod4();

            return compDoubleInner(comp1, comp2);
        }

    };
    public static Comparator<Stock> StockPeriod5Comparator = new Comparator<Stock>() {

        public int compare(Stock stock1, Stock stock2) {

            Double comp1 = stock1.getPeriod5();
            Double comp2 = stock2.getPeriod5();

            return compDoubleInner(comp1, comp2);
        }

    };

    public static int compDoubleInner(Double comp1, Double comp2) {
        if (comp1 == null && comp2 == null) {
            return 0;
        }

        if (comp1 == null) {
            return 1;
        }

        if (comp2 == null) {
            return -1;
        }

        //descending order
        return comp2.compareTo(comp1);
    }

    /*
     * Get table id for given date
     */
    public static int getStockDate(List<Stock> stocklist, Date mydate2) {
        if (mydate2 == null) {
            return 0;
        }
        for (int i = 0; i < stocklist.size() ; i++) {
            if (mydate2.equals(stocklist.get(i).getDate())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Return the index of the specific date
     * 
     * @param stocklist a stock list
     * @param date a date
     * @return the index of the specific date
     */

    public static int getStockDate(List<String> stocklist, String date) {
        if (date == null) {
            return stocklist.size() - 1;
        }
        for (int i = 0; i < stocklist.size() ; i++) {
            if (date.equals(stocklist.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public static DefaultCategoryDataset getTopChart(int days,
            int topbottom, List<Stock>[][] stocklistPeriod, int period) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        for (int j = days - 1; j >= 0; j--) {
            List<Stock> list = stocklistPeriod[period][j];
            if (j > 0) {
                list = StockUtil.listFilterTop(stocklistPeriod[period][j], stocklistPeriod[period][0], topbottom);
            }
            for (int i = 0; i < topbottom; i++) {
                if (i < list.size()) {
                    Stock stock = list.get(i);
                    try {
                        dataset.addValue(StockDao.getPeriod(stock, period + 1), stock.getName() , new Integer(-j));
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);
                    }
                }
            }
        }
        if (dataset.getColumnCount() == 0) {
            return null;
        }
        return dataset;
    }

    public static DefaultCategoryDataset getFilterChart(int days,
            List<String> ids, List<Stock>[][] stocklistPeriod, int period) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        for (int j = days - 1; j >= 0; j--) {
            List<Stock> list = stocklistPeriod[period][j];
            for (int i = 0; i < list.size(); i++) {
                Stock stock = list.get(i);
                if (ids.contains(stock.getId())) {
                    try {
                        //log.info("info " + stock.getName() + " " + StockDao.getPeriod(stock, period + 1) + " " + new Integer(-j));
                        dataset.addValue(StockDao.getPeriod(stock, period + 1), stock.getName() , new Integer(-j));
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);
                
                    }
                }
            }
        }
        if (dataset.getColumnCount() == 0) {
            return null;
        }
        return dataset;
    }

    public static DefaultCategoryDataset getBottomChart(int periods,
            int topbottom, List<Stock>[][] stocklistPeriod, int period) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        for (int j = periods - 1; j >= 0; j--) {
            List<Stock> list = StockUtil.listFilterBottom(stocklistPeriod[period][0], stocklistPeriod[period][0], topbottom, period);
            if (list.isEmpty()) {
                continue;
            }
            if (j > 0) {
                list = StockUtil.listFilterBottom(stocklistPeriod[period][j], stocklistPeriod[period][0], topbottom, period);
            }
            for (int i = list.size() - 1; i >= (list.size() - topbottom); i--) {
                if (i >= 0) {
                    Stock stock = list.get(i);
                    try {
                        dataset.addValue(StockDao.getPeriod(stock, period + 1), stock.getName() , new Integer(-j));
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);
                    }
                }
            }
        }
        if (dataset.getColumnCount() == 0) {
            return null;
        }
        return dataset;
    }

    public static DefaultCategoryDataset getRisingChart(int days,
            int topbottom, List<Stock>[][] stocklistPeriod,
            HashMap<String, Integer> mymap, int period) {
        List<String> list0 = new ArrayList<String>();
        List<Integer> list1 = new ArrayList<Integer>();
        for (String key : mymap.keySet()) {
            list0.add(key);
            list1.add(mymap.get(key));
        }
        for(int i = 0; i < list0.size(); i++) {
            for(int j = 1; j < list0.size(); j++) {
                if (list1.get(j-1) < list1.get(j)) {
                    String tmp = list0.get(j-1);
                    Integer tmpi = list1.get(j-1);
                    list0.set(j - 1, list0.get(j));
                    list1.set(j - 1, list1.get(j));
                    list0.set(j, tmp);
                    list1.set(j, tmpi);
                }
            }
        }
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        if (false && list0.isEmpty()) {
            List<Stock> list2 = stocklistPeriod[period][9];
            int max = Math.min(topbottom, list2.size());
            for (int i = 0; i < max; i++) {
                System.out.println("test " + list2.get(i).getDbid() + " " + list2.get(i).getName());
            }              
            return null;
        }
        for (int j = days - 1; j >= 0; j--) {
            List<Stock> list2 = stocklistPeriod[period][j];
            int max = Math.min(topbottom, list2.size());
            for (int i = 0; i < max; i++) {
                String id = list0.get(i);
                Stock stock = null; //list2.get(0);
                for (int k = 0; k < list2.size(); k++) {
                    Stock tmpstock = list2.get(k); 
                    if (id.equals(tmpstock.getId())) {
                        stock = tmpstock;
                        break;
                    }
                }
                try {
                    if (stock != null) {
                        dataset.addValue(StockDao.getPeriod(stock, period + 1), stock.getName() , new Integer(-j));
                    }
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        }
        return dataset;
    }

    public static List<Stock> listFilterTop(List<Stock> list, List<Stock> listmain, int size) {
        List<Stock> retlist = new ArrayList<Stock>();
        int max = Math.min(size, listmain.size());
        for (int i = 0; i < max; i ++) {
            String id = listmain.get(i).getId();
            for (int j = 0; j < list.size(); j ++) {
                if (id.equals(list.get(j).getId())) {
                    retlist.add(list.get(j));
                    break;
                }
            }
        }
        return retlist;
    }

    public static List<Stock> listFilterBottom(List<Stock> list, List<Stock> listmain, int size, int period) {
        List<Stock> retlist = new ArrayList<Stock>();
        int max = Math.min(size, listmain.size());
        int start;
        for (start = listmain.size() -1; start >= 0; start --) {
            if (start >= 0) {
                Stock stock = listmain.get(start);
                try {
                    if (StockDao.getPeriod(stock, period + 1) != null) {
                        //start--;
                        break;
                    }
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
                //System.out.println("skipping");
            }
        }
        //System.out.println("start size " + start + " " + listmain.size());
        for (int i = start; i > start - size; i--) {
            if (i >= 0) {
                String id = listmain.get(i).getId();
                for (int j = list.size() - 1; j >= 0; j --) {
                    if (id.equals(list.get(j).getId())) {
                        retlist.add(list.get(j));
                        //System.out.println("name " + list.get(j).getName());
                    }
                }
            }
        }
        return retlist;
    }

    public static double getMax(List<Stock> stocklist, int period) {
        double max = -1000000;
        for (int i = 0; i < stocklist.size() ; i++) {
            double cur = -1000000;
            if (period == 0) {
                Double periodval = null;
                try {
                    periodval = StockDao.getPeriod(stocklist.get(i), period + 1);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
                if (periodval != null) {
                    cur = periodval;
                }  
            }
            if (cur > max) {
                max = cur;
            }
        }
        return max;
    }
}
