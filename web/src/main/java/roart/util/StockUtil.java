package roart.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math3.util.Pair;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.model.Stock;
import roart.service.ControlService;

public class StockUtil {

    private static Logger log = LoggerFactory.getLogger(StockUtil.class);

    public final static int PERIODS = 6;

    /**
     * Create sorted tables for all periods in a time interval
     * 
     * @param datedstocklists returned lists based on date
     * @param count number of days to measure
     * @param arr also for return sorted tables
     * @return sorted tables
     * @throws Exception
     */

    public static Map<String, Integer>[][] getListAndDiff(List<Stock> datedstocklists[], int count, Object[] arr)
                    throws Exception {
        Comparator[] comparators = { StockUtil.StockPeriod1Comparator, StockUtil.StockPeriod2Comparator, StockUtil.StockPeriod3Comparator, StockUtil.StockPeriod4Comparator, StockUtil.StockPeriod5Comparator, StockUtil.StockPeriod6Comparator };

        // make sorted period1, sorted current day
        // make sorted period1, sorted day offset
        Map<String, Integer>[][] periodmaps = new HashMap[count - 1][PERIODS];
        List<Stock>[][] stocklistPeriod = new ArrayList[PERIODS][count];
        if (arr != null) {
            arr[0] = stocklistPeriod;
        }

        // Do for all wanted days

        for (int j = 0; j < count; j++) {
            Map<String, Integer>[] periodmap = new HashMap[PERIODS];
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
     * Create an array of stocklists based on the desired date and intervals
     * 
     * @param stockdatemap a map of dates to stock lists
     * @param datedate TODO
     * @param count the number of lists to return
     * @param mytableintervaldays the interval between the dates
     * @return array of stock lists based on date
     */
    
    public static List<Stock>[] getDatedstocklists(Map<String, List<Stock>> stockdatemap, Date datedate, int count, int mytableintervaldays) {
        List<Stock>[] datedstocklists = new ArrayList[count];
        for (int i = 0; i < count; i ++) {
            datedstocklists[i] = new ArrayList();          
        }

        List<String> list = new ArrayList(stockdatemap.keySet());
        Collections.sort(list);
        String date = null;
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
        return datedstocklists;
    }

    /**
     * Make a map of differences between two days
     * 
     * @param stocklistPeriod1Day0 a stock list from the previous day
     * @param stocklistPeriod1Day1 a stock list from a day
     * @return a list of chart differences, indicating rise or decline
     */

    public static Map<String, Integer> getPeriodmap(
            List<Stock> stocklistPeriod1Day0, List<Stock> stocklistPeriod1Day1) {
        Map<String, Integer> periodmap = new HashMap<String, Integer>();
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
            Map<String, List<Stock>> stockmap, Date datedate, int mydays) {
        List<Stock> retstocklist = new ArrayList<Stock>();
        for (String key : stockmap.keySet()) {
            Stock stock = null;
            List<Stock> stocklist = stockmap.get(key);
            if (datedate == null) {
                if (stocklist.size() > mydays) {
                    stock = stocklist.get(mydays);
                } else {
                    continue;
                }
            } else {
                int i = StockUtil.getStockDate(stocklist, datedate);
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

    public static Map<String, List<Stock>> splitId(List<Stock> stocks) {
        Map<String, List<Stock>> mymap = new HashMap<String, List<Stock>>();
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

    public static Map<String, List<Stock>> splitDate(List<Stock> stocks) {
        Map<String, List<Stock>> mymap = new HashMap<String, List<Stock>>();
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

    /**
     * Find out whether the special (price/index) is present
     * 
     * @param marketdatamap a map of market names to marketdata
     * @param i the special type
     * @return boolean true if present
     * @throws Exception
     */
    
    public static boolean hasSpecial(Map<String, MarketData> marketdatamap, int i) throws Exception {
        for (String market : marketdatamap.keySet()) { 
            MarketData marketdata = marketdatamap.get(market);
            List<Stock> stocks = marketdata.stocks;
            if (hasSpecial(stocks, i)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Find out whether the special (price/index) is present
     * 
     * @param stocks list
     * @param i the period
     * @return boolean true if present
     * @throws Exception
     */
    
    public static boolean hasSpecial(List<Stock> stocks, int i) throws Exception {
        for (Stock s : stocks) {
            if (StockDao.getSpecial(s, i) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find out whether the period is present
     * 
     * @param stocks list
     * @param i the period
     * @return boolean true if present
     * @throws Exception
     */
    
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

    /**
     * Not used
     * 
     * @param stocks
     * @return
     */
    
    public static boolean hasStockPeriod1(List<Stock> stocks) {
        for (Stock s : stocks) {
            if (s.getPeriod1() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Not used
     * 
     * @param stocks
     * @return
     */
    
    public static boolean hasStockPeriod2(List<Stock> stocks) {
        for (Stock s : stocks) {
            if (s.getPeriod2() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Not used
     * 
     * @param stocks
     * @return
     */
    
    public static boolean hasStockPeriod3(List<Stock> stocks) {
        for (Stock s : stocks) {
            if (s.getPeriod3() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Not used
     * 
     * @param stocks
     * @return
     */
    
    public static boolean hasStockPeriod4(List<Stock> stocks) {
        for (Stock s : stocks) {
            if (s.getPeriod4() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Not used
     * 
     * @param stocks
     * @return
     */
    
    public static boolean hasStockPeriod5(List<Stock> stocks) {
        for (Stock s : stocks) {
            if (s.getPeriod5() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Not used
     * 
     * @param stocks
     * @return
     */
    
    public static boolean hasStockPeriod6(List<Stock> stocks) {
        for (Stock s : stocks) {
            if (s.getPeriod6() != null) {
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
    public static Comparator<Stock> StockPeriod6Comparator = new Comparator<Stock>() {

        public int compare(Stock stock1, Stock stock2) {

            Double comp1 = stock1.getPeriod6();
            Double comp2 = stock2.getPeriod6();

            return compDoubleInner(comp1, comp2);
        }

    };

    /**
     * Compare two values
     * 
     * @param comp1 value
     * @param comp2 value
     * @return compareTo result
     */
    
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

    /**
     * Not used
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
        if (true)
        return stocklist.indexOf(date);
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

    public static DefaultCategoryDataset getFilterChartPeriod(int days,
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

    /**
     * Given a market and the market set pairs, return the related period for that market
     * 
     * @param market
     * @param pairs set
     * @return period
     */
    
    private static Integer getPeriodByMarket(String market, Set<Pair<String, Integer>> pairs) {
        for (Pair<String, Integer> pair : pairs) {
            String tmpMarket = (String) pair.getFirst();
            if (market.equals(tmpMarket)) {
                return (Integer) pair.getSecond();
            }
        }
        return null;
    }
    
    /**
     * Create dataset for given ids
     * 
     * @param days number to display
     * @param ids for the dataset
     * @param marketdatamap for data related to given marked
     * @param perioddata for the given period
     * @return
     */
    
    public static DefaultCategoryDataset getFilterChartPeriod(int days,
            Set<Pair> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata) {
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        Set<String> dateset = new HashSet();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        for (String market : marketdatamap.keySet()) {
            Integer periodInt = getPeriodByMarket(market, pairs);
            if (periodInt == null) {
                continue;
            }
            int period = periodInt;
            MarketData marketdata = marketdatamap.get(market);
            for (int j = days - 1; j >= 0; j--) {
                List<Stock>[] datedstocklists = marketdata.datedstocklists;
                List<Stock> list = datedstocklists[j];
                if (list == null) {
                    System.out.println("listnull " + market + " " + " " + j);
                    continue;
                }
                for (int i = 0; i < list.size(); i++) {
                    Stock stock = list.get(i);
                    Pair<String, String> pair = new Pair(market, stock.getId());
                    if (ids.contains(pair)) {
                        try {
                            Double value = StockDao.getPeriod(stock, period + 1);
                            if (value == null) {
                                continue;
                            }
                            //log.info("info " + stock.getName() + " " + value + " " + new Integer(-j));
                            dataset.addValue(value, stock.getName() , new Integer(-j));
                            dateset.add(dt.format(stock.getDate()));
                        } catch (Exception e) {
                            log.error(Constants.EXCEPTION, e);

                        }
                    }
                }
            }
        }
        if (dataset.getColumnCount() == 0) {
            return null;
        }
        perioddata.date0 = Collections.min(dateset);
        perioddata.date1 = Collections.max(dateset);
        return dataset;
    }

    /**
     * Not in use
     * 
     * @param days
     * @param ids
     * @param datedstocklists
     * @param type
     * @return
     */
    
    public static DefaultCategoryDataset getFilterChartDated(int days,
            List<String> ids, List<Stock>[] datedstocklists, int type) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        for (int j = days - 1; j >= 0; j--) {
            List<Stock> list = datedstocklists[j];
            for (int i = 0; i < list.size(); i++) {
                Stock stock = list.get(i);
                if (ids.contains(stock.getId())) {
                    try {
                        //log.info("info " + stock.getName() + " " + StockDao.getSpecial(stock, type) + " " + new Integer(-j));
                        dataset.addValue(StockDao.getSpecial(stock, type), stock.getName() , new Integer(-j));
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

    /**
     * Create a dataset with the given ids
     * @param days to use for the graph
     * @param ids to display
     * @param marketdatamap map from market to marketdata
     * @param perioddata data for period
     * @param type period or special (price/index)
     * @param equalize to a 100% graph
     * @param dataseteq equalized data set
     * @return dataset
     */
    
    public static DefaultCategoryDataset getFilterChartDated(int days,
            Set<Pair> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, int type, boolean equalize, DefaultCategoryDataset dataseteq) {
        Map<String, Set<Pair<Double, Integer>>> map= null;
        if (equalize) {
            map = new HashMap();
        }
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        Set<String> dateset = new HashSet();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        for (int j = days - 1; j >= 0; j--) {
            for (String market : marketdatamap.keySet()) {
                MarketData marketdata = marketdatamap.get(market);
                List<Stock>[] datedstocklists = marketdata.datedstocklists;
                List<Stock> list = datedstocklists[j];
                if (list == null) {
                    continue;
                }
                for (int i = 0; i < list.size(); i++) {
                    Stock stock = list.get(i);
                    Pair<String, String> pair = new Pair(market, stock.getId());
                    if (ids.contains(pair)) {
                        try {
                            Double value = StockDao.getSpecial(stock, type);
                            if (value == null) {
                                continue;
                            }
                            String stockName = stock.getName();
                            log.info("info " + stockName + " " + value + " " + new Integer(-j));
                            dataset.addValue(value, stockName , new Integer(-j));
                            dateset.add(dt.format(stock.getDate()));
                            if (map != null) {
                                Set<Pair<Double, Integer>> set = map.get(stockName);
                                if (set == null) {
                                    set = new HashSet();
                                    map.put(stockName, set);
                                }
                                set.add(new Pair(value, -j));
                            }
                       } catch (Exception e) {
                            log.error(Constants.EXCEPTION, e);
                        }
                    }
                }
            }
        }
        if (dataset.getColumnCount() == 0) {
            return null;
        }
        perioddata.date0 = Collections.min(dateset);
        perioddata.date1 = Collections.max(dateset);
        if (map != null) {
            createEqualizedDataset(dataseteq, map);
        }
        return dataset;
    }

    /**
     * Create a dataset with stock value equalized to the 100%
     * 
     * @param dataseteq the dataset to be filled
     * @param map of stocks and their set of ordered valued
     */
    
    private static void createEqualizedDataset(DefaultCategoryDataset dataseteq,
            Map<String, Set<Pair<Double, Integer>>> map) {
        Map<Integer, Set<Pair<Double, String>>> newMap = createNewMap(map);
        List<Integer> numList = new ArrayList(newMap.keySet());
        Collections.sort(numList);
        for (Integer numOrder : numList) {
            Set<Pair<Double, String>> newSet = newMap.get(numOrder);
            for (Pair<Double, String> newPair : newSet) {
                Double value = (Double) newPair.getFirst();
                String stockName = (String) newPair.getSecond();
                dataseteq.addValue(value, stockName , numOrder);
            }
        }
    }

    /**
     * Create a new map for the chart, with stock values set for max 100%
     * 
     * @param map of the stocks with the ordered value set
     * @return a map structured for making a new chart
     */
    
    private static Map<Integer, Set<Pair<Double, String>>> createNewMap(
            Map<String, Set<Pair<Double, Integer>>> map) {
        Map<String, Double> maxMap = getMaxMap(map);
        Map<Integer, Set<Pair<Double, String>>> newMap = new HashMap();
        for (String stockName : map.keySet()) {
            Double max = maxMap.get(stockName);
            Set<Pair<Double, Integer>> set = map.get(stockName);
            for (Pair pair : set) {
                double value = (double) pair.getFirst();
                value = value * 100 / max;
                Pair<Double, String> newPair = new Pair(value, stockName);
                Integer numOrder = (Integer) pair.getSecond();
                Set<Pair<Double, String>> newSet = newMap.get(numOrder);
                if (newSet == null) {
                    newSet = new HashSet();
                    newMap.put(numOrder, newSet);
                }
                newSet.add(newPair);
            }
        }
        return newMap;
    }

    /**
     * Get a map of max values of each stock
     * 
     * @returns the map of stock to max
     * @param a map of stock to it's value set
     * 
     */
    
    private static Map<String, Double> getMaxMap(
            Map<String, Set<Pair<Double, Integer>>> map) {
        Map<String, Double> maxMap = new HashMap();
        for (String stockName : map.keySet()) {
            Set<Double> valueSet = new HashSet();
            Set<Pair<Double, Integer>> set = map.get(stockName);
            for (Pair pair : set) {
                valueSet.add((Double) pair.getFirst());
            }
            Double min = Collections.min(valueSet);
            Double max = Collections.max(valueSet);
            min = Math.abs(min);
            max = Math.abs(max);
            if (min > max) {
                max = min;
            }
            maxMap.put(stockName, max);
        }
        return maxMap;
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

    /**
     * 
     * @param days the number of days sample used to compute the rising
     * @param topbottom the number we want on our chart
     * @param stocklistPeriod
     * @param mymap a map of ids, and the rise number
     * @param period the period we want
     * @return a dataset with the best chart risers
     */
    
    public static DefaultCategoryDataset getRisingChart(int days,
            int topbottom, List<Stock>[][] stocklistPeriod,
            Map<String, Integer> mymap, int period) {
        List<String> list0 = new ArrayList<String>();
        List<Integer> list1 = new ArrayList<Integer>();
        for (String key : mymap.keySet()) {
            list0.add(key);
            list1.add(mymap.get(key));
        }
        // make sorted lists over the best risers
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
        if (dataset.getColumnCount() == 0) {
            return null;
        }
        return dataset;
    }

    /**
     * Given listmain, size and a period, return the same number of elements from list
     * 
     * @param list the list we will search for the listmain elements in
     * @param listmain the list we want to find represented in list
     * @param size of the top part of listmain
     * @param period we want
     * @return a list of size from list
     */
    
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

    /**
     * Given listmain, size and a period, return the same number of elements from list
     * 
     * @param list the list we will search for the listmain elements in
     * @param listmain the list we want to find represented in list
     * @param size of the bottom part of listmain
     * @param period we want
     * @return a list of size from list
     */
    
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

    /**
     * Get maximal value for period in stocklist
     * 
     * @param stocklist the list of stocks
     * @param period for the given period
     */
    
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

    /**
     * Get maximal value for period in stocklist
     * 
     * @param stocklist the list of stocks
     * @param period for the given period/special
     */
    
    public static double getMax2(List<Stock> stocklist, int period) {
        double max = -1000000;
        for (int i = 0; i < stocklist.size() ; i++) {
            double cur = -1000000;
            if (period == 0) {
                Double periodval = null;
                try {
                    periodval = StockDao.getValue(stocklist.get(i), period);
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

    /**
     * Get minimal value for period in stocklist
     * 
     * @param stocklist the list of stocks
     * @param period for the given period/special
     */
    
    public static double getMin(List<Stock> stocklist, int period) {
        double min = 1000000;
        for (int i = 0; i < stocklist.size() ; i++) {
            double cur = 1000000;
            if (period == 0) {
                Double periodval = null;
                try {
                    periodval = StockDao.getValue(stocklist.get(i), period);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
                if (periodval != null) {
                    cur = periodval;
                }  
            }
            if (cur < min) {
                min = cur;
            }
        }
        return min;
    }
}
