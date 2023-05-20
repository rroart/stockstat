package roart.stockutil;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.category.AbstractCategory;
import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.common.model.MetaItem;
import roart.common.model.StockItem;
import roart.common.util.TimeUtil;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;

public class StockUtil {

    private static Logger log = LoggerFactory.getLogger(StockUtil.class);

    /**
     * Create sorted tables for all periods in a time interval
     * @param count number of days to measure
     * @param arr also for return sorted tables
     * 
     * @return sorted tables
     * @throws Exception
     */

    public static Map<String, Integer>[][] getListMove(int count, List<StockItem>[][] stocklistPeriod)
            throws Exception {

        // make sorted period1, sorted current day
        // make sorted period1, sorted day offset
        Map<String, Integer>[][] periodmaps = new HashMap[count - 1][Constants.PERIODS];

        // Do for all wanted days

        for (int j = 0; j < count; j++) {
            Map<String, Integer>[] periodmap = new HashMap[Constants.PERIODS];
            for (int i = 0; i < Constants.PERIODS; i++) {
                // Check if the period for the wanted day has any content

                boolean hasPeriod = !stocklistPeriod[i][j].isEmpty();

                /*
                 *  If not the first, get a periodmap, which is a list of differences, indicating rise or decline
                 */

                if (hasPeriod) {
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

    public static Map<String, Integer>[] getListMove(int count, List<StockItem>[] stocklistPeriod, int period)
            throws Exception {

        // make sorted period1, sorted current day
        // make sorted period1, sorted day offset
        Map<String, Integer>[] periodmaps = new HashMap[count - 1];

        // Do for all wanted days

        for (int j = 0; j < count; j++) {
            Map<String, Integer> periodmap = new HashMap<>();
            // Check if the period for the wanted day has any content

            boolean hasPeriod = !stocklistPeriod[j].isEmpty();

            /*
             *  If not the first, get a periodmap, which is a list of differences, indicating rise or decline
             */

            if (hasPeriod) {
                if (j > 0) {
                    periodmap = StockUtil.getPeriodmap(stocklistPeriod[j - 1], stocklistPeriod[j]);
                }
            } else {
                if (j > 0) {
                    periodmap = new HashMap<>();
                }
            }
            if (j > 0) {
                periodmaps[j - 1] = periodmap;
            }
        }
        return periodmaps;
    }

    /**
     * Create sorted tables for all periods in a time interval
     * 
     * @param datedstocklists returned lists based on date
     * @param count number of days to measure
     * @param arr also for return sorted tables
     * @return sorted tables
     * @throws Exception
     */

    public static List<StockItem>[][] getListSorted(List<StockItem> datedstocklists[], int count)
            throws Exception {
        Comparator[] comparators = { StockUtil.StockPeriod1Comparator, StockUtil.StockPeriod2Comparator, StockUtil.StockPeriod3Comparator, StockUtil.StockPeriod4Comparator, StockUtil.StockPeriod5Comparator, StockUtil.StockPeriod6Comparator , StockUtil.StockPeriod7Comparator , StockUtil.StockPeriod8Comparator , StockUtil.StockPeriod9Comparator };

        // make sorted period1, sorted current day
        // make sorted period1, sorted day offset
        List<StockItem>[][] stocklistPeriod = new ArrayList[Constants.PERIODS][count];

        // Do for all wanted days

        for (int j = 0; j < count; j++) {
            for (int i = 0; i < Constants.PERIODS; i++) {
                boolean hasPeriod = false;

                // Check if the period fot the wanted day has any content

                if (datedstocklists[j] != null) {
                    hasPeriod = hasStockPeriod(datedstocklists[j], i);
                }

                // If it has, add it to the table

                if (datedstocklists[j] != null && hasPeriod) {
                    stocklistPeriod[i][j] = new ArrayList<StockItem>(datedstocklists[j]);
                    stocklistPeriod[i][j].sort(comparators[i]);
                } else {
                    stocklistPeriod[i][j] = new ArrayList<StockItem>();
                }

            }
        }
        return stocklistPeriod;
    }

    public static List<StockItem>[] getListSorted(List<StockItem> datedstocklists[], int count, int period)
            throws Exception {
        Comparator[] comparators = { StockUtil.StockPeriod1Comparator, StockUtil.StockPeriod2Comparator, StockUtil.StockPeriod3Comparator, StockUtil.StockPeriod4Comparator, StockUtil.StockPeriod5Comparator, StockUtil.StockPeriod6Comparator , StockUtil.StockPeriod7Comparator , StockUtil.StockPeriod8Comparator , StockUtil.StockPeriod9Comparator };

        // make sorted period1, sorted current day
        // make sorted period1, sorted day offset
        List<StockItem>[] stocklistPeriod = new ArrayList[count];

        // Do for all wanted days

        for (int j = 0; j < count; j++) {
            boolean hasPeriod = false;

            // Check if the period fot the wanted day has any content

            if (datedstocklists[j] != null) {
                hasPeriod = hasStockPeriod(datedstocklists[j], period);
            }

            // If it has, add it to the table

            if (datedstocklists[j] != null && hasPeriod) {
                stocklistPeriod[j] = new ArrayList<StockItem>(datedstocklists[j]);
                stocklistPeriod[j].sort(comparators[period]);
            } else {
                stocklistPeriod[j] = new ArrayList<StockItem>();
            }
        }
        return stocklistPeriod;
    }

    /**
     * Create an array of stocklists based on the desired date and intervals
     * 
     * @param stockdatemap a map of dates to stock lists
     * @param datedate date
     * @param count the number of lists to return
     * @param mytableintervaldays the interval between the dates
     * @return array of stock lists based on date
     */

    public static List<StockItem>[] getDatedstocklists(Map<String, List<StockItem>> stockdatemap, LocalDate datedate, int count, int mytableintervaldays) {
        List<StockItem>[] datedstocklists = null;
        datedstocklists = new ArrayList[count];
        for (int i = 0; i < count; i ++) {
            datedstocklists[i] = new ArrayList<>();          
        }
        List<String> list = new ArrayList<>(stockdatemap.keySet());
        Collections.sort(list);
        String date = null;
        if (datedate != null) {
            date = TimeUtil.format(datedate);                
        }
        log.info("datemapsize {}", list.size());
        int index = getStockDate(list, date);
        
        if (index >= 0) {
            if (date == null) {
                log.error("Date is null");
            }
            if (datedstocklists.length == 0) {
                int jj = 0;
            }
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
            List<StockItem> stocklistPeriod1Day0, List<StockItem> stocklistPeriod1Day1) {
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

    @Deprecated
    private static List<StockItem> getOffsetList(
            Map<String, List<StockItem>> stockmap, Date datedate, int mydays) {
        List<StockItem> retstocklist = new ArrayList<StockItem>();
        for (String key : stockmap.keySet()) {
            StockItem stock = null;
            List<StockItem> stocklist = stockmap.get(key);
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

    public static Map<String, List<StockItem>> splitId(List<StockItem> stocks) {
        Map<String, List<StockItem>> mymap = new HashMap<String, List<StockItem>>();
        for (StockItem stock : stocks) {
            List<StockItem> stocklist = mymap.get(stock.getId());
            if (stocklist == null) {
                stocklist = new ArrayList<StockItem>();
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

    public static Map<String, List<StockItem>> splitDate(List<StockItem> stocks) {
        Map<String, List<StockItem>> mymap = new HashMap<String, List<StockItem>>();
        for (StockItem stock : stocks) {
            try {
            String date = TimeUtil.format(stock.getDate());
            List<StockItem> stocklist = mymap.get(date);
            if (stocklist == null) {
                stocklist = new ArrayList<StockItem>();
                mymap.put(date, stocklist);
            }
            stocklist.add(stock);
            } catch (Exception e) {
                int jj = 0;
            }
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
        for (Entry<String, MarketData> entry : marketdatamap.entrySet()) { 
            MarketData marketdata = entry.getValue();
            List<StockItem> stocks = marketdata.stocks;
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

    public static boolean hasSpecial(List<StockItem> stocks, int i) throws Exception {
        for (StockItem s : stocks) {
            if (StockDao.getSpecial(s, i)[0] != null) {
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

    public static boolean hasStockPeriod(List<StockItem> stocks, int i) throws Exception {
        for (StockItem s : stocks) {
            if (StockDao.getPeriod(s, i)[0] != null) {
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

    public static boolean hasStockValue(List<StockItem> stocks, int i) throws Exception {
        if (stocks == null) {
            int jj = 0;
        }
        for (StockItem s : stocks) {
            if (StockDao.getValue(s, i)[0] != null) {
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

    public static boolean hasStockPeriod1(List<StockItem> stocks) {
        for (StockItem s : stocks) {
            if (s.getPeriod(0) != null) {
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

    public static boolean hasStockPeriod2(List<StockItem> stocks) {
        for (StockItem s : stocks) {
            if (s.getPeriod(1) != null) {
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

    public static boolean hasStockPeriod3(List<StockItem> stocks) {
        for (StockItem s : stocks) {
            if (s.getPeriod(2) != null) {
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

    public static boolean hasStockPeriod4(List<StockItem> stocks) {
        for (StockItem s : stocks) {
            if (s.getPeriod(3) != null) {
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

    public static boolean hasStockPeriod5(List<StockItem> stocks) {
        for (StockItem s : stocks) {
            if (s.getPeriod(4) != null) {
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

    public static boolean hasStockPeriod6(List<StockItem> stocks) {
        for (StockItem s : stocks) {
            if (s.getPeriod(5) != null) {
                return true;
            }
        }
        return false;
    }

    public static final Comparator<StockItem> StockDateComparator =
            (StockItem comp1, StockItem comp2) -> comp2.getDate().compareTo(comp1.getDate());

    public static final Comparator<StockItem> StockPeriod1Comparator = 
            (StockItem stock1, StockItem stock2) -> compDoubleInner(stock1.getPeriod(0), stock2.getPeriod(0));

    public static final Comparator<StockItem> StockPeriod2Comparator = 
            (StockItem stock1, StockItem stock2) -> compDoubleInner(stock1.getPeriod(1), stock2.getPeriod(1));
             
    public static final Comparator<StockItem> StockPeriod3Comparator = 
            (StockItem stock1, StockItem stock2) -> compDoubleInner(stock1.getPeriod(2), stock2.getPeriod(2));

    public static final Comparator<StockItem> StockPeriod4Comparator = 
            (StockItem stock1, StockItem stock2) -> compDoubleInner(stock1.getPeriod(3), stock2.getPeriod(3));
  
    public static final Comparator<StockItem> StockPeriod5Comparator = 
            (StockItem stock1, StockItem stock2) -> compDoubleInner(stock1.getPeriod(4), stock2.getPeriod(4));

    public static final Comparator<StockItem> StockPeriod6Comparator = 
            (StockItem stock1, StockItem stock2) -> compDoubleInner(stock1.getPeriod(5), stock2.getPeriod(5));

    public static final Comparator<StockItem> StockPeriod7Comparator = 
            (StockItem stock1, StockItem stock2) -> compDoubleInner(stock1.getPeriod(6), stock2.getPeriod(6));

    public static final Comparator<StockItem> StockPeriod8Comparator = 
            (StockItem stock1, StockItem stock2) -> compDoubleInner(stock1.getPeriod(7), stock2.getPeriod(7));
            
    public static final Comparator<StockItem> StockPeriod9Comparator = 
            (StockItem stock1, StockItem stock2) -> compDoubleInner(stock1.getPeriod(8), stock2.getPeriod(8));

    void dummy() {
        // the comparator messes up indentation
    }

    public static AbstractCategory getWantedCategory(AbstractCategory[] categories, MetaItem meta) throws Exception {
        String[] defaultPriorities = { Constants.PRICE, Constants.INDEX };
        String[] priorities;
        String priority = null;
        if (meta != null) {
            priority = meta.getPriority();
        }
        if (priority != null) {
            priorities = priority.split(",");            
        } else {
            priorities = defaultPriorities;
        }
        AbstractCategory cat = null;
        for (String aPriority : priorities) {
            for (AbstractCategory category : categories) {
                if (category.getTitle().equals(aPriority) && category.hasContent()) {
                    return category;
                }
            }
        }
        return cat;
    }

    public static AbstractCategory getWantedCategory(AbstractCategory[] categories, int cat) throws Exception {
        for (AbstractCategory category : categories) {
            if (cat == category.getPeriod()) {
                return category;
            }
        }
        return null;
    }

    public static Integer getWantedCategory(List<StockItem> stocks, PeriodData periodData) throws Exception {
        if (hasStockValue(stocks, Constants.PRICECOLUMN)) {
            return Constants.PRICECOLUMN;
        }
        if (hasStockValue(stocks, Constants.INDEXVALUECOLUMN)) {
            return Constants.INDEXVALUECOLUMN;
        }
        if (periodData == null) {
            return null;
        }
        Set<Pair<String, Integer>> pairs = periodData.pairs;
        for (Pair<String, Integer> pair : pairs) {
            int cat = (int) pair.getRight();
            if (hasStockValue(stocks, cat)) {
                return cat;
            }
        }
        return null;
    }

    public static Integer getWantedCategory(List<StockItem> stocks, MetaItem meta) throws Exception {
        Integer[] defaultPris = { Constants.PRICECOLUMN, Constants.INDEXVALUECOLUMN };
        String[] defaultPriorities = { Constants.PRICE, Constants.INDEX };
        String[] priorities;
        String priority = null;
        if (meta != null) {
            priority = meta.getPriority();
        }
        if (priority != null) {
            priorities = priority.split(",");            
        } else {
            priorities = defaultPriorities;
        }
        for (String aPriority : priorities) {
            for (int i = 0; i < defaultPriorities.length; i++) {
                if (defaultPriorities[i].equals(aPriority)) {
                    if (hasStockValue(stocks, defaultPris[i])) {
                        return defaultPris[i];
                    }
                }
            }
            String[] periods = new String[0];
            if (meta != null) {
                periods = meta.getPeriod();
            }
            for (int i = 0; i < periods.length; i++) {
                if (aPriority.equals(periods[i])) {
                    if (hasStockValue(stocks, i)) {
                        return i;
                    }                    
                }
            }
        }
        return null;
    }

    public static String getCatName(Integer cat, String[] periodText) {
        String catName = null;
        if (cat >= 0) {
            catName = periodText[cat];
        }
        if (cat == Constants.INDEXVALUECOLUMN) {
            catName = Constants.INDEX;
        }
        if (cat == Constants.PRICECOLUMN) {
            catName = Constants.PRICE;
        }
        return catName;
    }

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

    public static int getStockDate(List<StockItem> stocklist, Date mydate2) {
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
        return stocklist.indexOf(date);
    }

    public static DefaultCategoryDataset getTopChart(int days,
            int topbottom, List<StockItem>[][] stocklistPeriod, int period) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        for (int j = days - 1; j >= 0; j--) {
            List<StockItem> list = stocklistPeriod[period][j];
            if (j > 0) {
                list = StockUtil.listFilterTop(stocklistPeriod[period][j], stocklistPeriod[period][0], topbottom);
            }
            for (int i = 0; i < topbottom; i++) {
                if (i < list.size()) {
                    StockItem stock = list.get(i);
                    try {
                        dataset.addValue(StockDao.getMainPeriod(stock, period), stock.getName() , new Integer(-j));
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
            List<String> ids, List<StockItem>[][] stocklistPeriod, int period) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        for (int j = days - 1; j >= 0; j--) {
            List<StockItem> list = stocklistPeriod[period][j];
            for (int i = 0; i < list.size(); i++) {
                StockItem stock = list.get(i);
                if (ids.contains(stock.getId())) {
                    try {
                        dataset.addValue(StockDao.getMainValue(stock, period), stock.getName() , new Integer(-j));
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

    public static Integer getPeriodByMarket(String market, Set<Pair<String, Integer>> pairs) {
        for (Pair<String, Integer> pair : pairs) {
            String tmpMarket = pair.getLeft();
            if (market.equals(tmpMarket)) {
                return pair.getRight();
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
            Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata) {
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        Set<String> dateset = new HashSet<>();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        for (Entry<String, MarketData> entry : marketdatamap.entrySet()) {
            String market = entry.getKey();
            Integer periodInt = getPeriodByMarket(market, pairs);
            if (periodInt == null) {
                continue;
            }
            int period = periodInt;
            MarketData marketdata = entry.getValue();
            for (int j = days - 1; j >= 0; j--) {
                List<StockItem>[] datedstocklists = marketdata.datedstocklists;
                List<StockItem> list = datedstocklists[j];
                if (list == null) {
                    log.info("listnull {} {}", market, j);
                    continue;
                }
                for (int i = 0; i < list.size(); i++) {
                    StockItem stock = list.get(i);
                    Pair<String, String> pair = new ImmutablePair(market, stock.getId());
                    if (ids.contains(pair)) {
                        try {
                            Double value = StockDao.getMainValue(stock, period);
                            if (value == null) {
                                continue;
                            }
                            dataset.addValue(value, stock.getName() , new Integer(-j));
                            dateset.add(TimeUtil.format(stock.getDate()));
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
            List<String> ids, List<StockItem>[] datedstocklists, int type) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        for (int j = days - 1; j >= 0; j--) {
            List<StockItem> list = datedstocklists[j];
            for (int i = 0; i < list.size(); i++) {
                StockItem stock = list.get(i);
                if (ids.contains(stock.getId())) {
                    try {
                        dataset.addValue(StockDao.getMainSpecial(stock, type), stock.getName() , new Integer(-j));
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
            Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, int type, boolean equalize, DefaultCategoryDataset dataseteq) {
        Map<String, Set<Pair<Double, Integer>>> map= null;
        if (equalize) {
            map = new HashMap<>();
        }
        Set<String> dateset = new HashSet<>();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        for (int j = days - 1; j >= 0; j--) {
            for (Entry<String, MarketData> entry : marketdatamap.entrySet()) {
                MarketData marketdata = entry.getValue();
                List<StockItem>[] datedstocklists = marketdata.datedstocklists;
                List<StockItem> list = datedstocklists[j];
                if (list == null) {
                    continue;
                }
                for (int i = 0; i < list.size(); i++) {
                    StockItem stock = list.get(i);
                    Pair<String, String> pair = new ImmutablePair(entry.getKey(), stock.getId());
                    if (ids.contains(pair)) {
                        try {
                            Double value = StockDao.getMainSpecial(stock, type);
                            if (value == null) {
                                continue;
                            }
                            String stockName = stock.getName();
                            log.info("info {} {} {}", stockName, value, Integer.valueOf(-j));
                            dataset.addValue(value, stockName , Integer.valueOf(-j));
                            dateset.add(TimeUtil.format(stock.getDate()));
                            if (map != null) {
                                Set<Pair<Double, Integer>> set = map.get(stockName);
                                if (set == null) {
                                    set = new HashSet<>();
                                    map.put(stockName, set);
                                }
                                set.add(new ImmutablePair(value, -j));
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
                Double value = newPair.getLeft();
                String stockName = newPair.getRight();
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
        for (Entry<String, Set<Pair<Double, Integer>>> entry : map.entrySet()) {
            String stockName = entry.getKey();
            Double max = maxMap.get(stockName);
            Set<Pair<Double, Integer>> set = entry.getValue();
            for (Pair<Double, Integer> pair : set) {
                double value = (double) pair.getLeft();
                value = value * 100 / max;
                Pair<Double, String> newPair = new ImmutablePair(value, stockName);
                Integer numOrder = pair.getRight();
                Set<Pair<Double, String>> newSet = newMap.computeIfAbsent(numOrder, k -> new HashSet<>());
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
        Map<String, Double> maxMap = new HashMap<>();
        for (Entry<String, Set<Pair<Double, Integer>>> entry : map.entrySet()) {
            String stockName = entry.getKey();
            Set<Double> valueSet = new HashSet<>();
            Set<Pair<Double, Integer>> set = entry.getValue();
            for (Pair<Double, Integer> pair : set) {
                valueSet.add((Double) pair.getLeft());
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
            int topbottom, List<StockItem>[][] stocklistPeriod, int period) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        for (int j = periods - 1; j >= 0; j--) {
            List<StockItem> list = StockUtil.listFilterBottom(stocklistPeriod[period][0], stocklistPeriod[period][0], topbottom, period);
            if (list.isEmpty()) {
                continue;
            }
            if (j > 0) {
                list = StockUtil.listFilterBottom(stocklistPeriod[period][j], stocklistPeriod[period][0], topbottom, period);
            }
            for (int i = list.size() - 1; i >= (list.size() - topbottom); i--) {
                if (i >= 0) {
                    StockItem stock = list.get(i);
                    try {
                        dataset.addValue(StockDao.getMainPeriod(stock, period ), stock.getName() , new Integer(-j));
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
            int topbottom, List<StockItem>[][] stocklistPeriod,
            Map<String, Integer> mymap, int period) {
        List<String> list0 = new ArrayList<>();
        List<Integer> list1 = new ArrayList<>();
        for (Entry<String, Integer> entry : mymap.entrySet()) {
            list0.add(entry.getKey());
            list1.add(entry.getValue());
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
            List<StockItem> list2 = stocklistPeriod[period][9];
            int max = Math.min(topbottom, list2.size());
            for (int i = 0; i < max; i++) {
                System.out.println("test " + list2.get(i).getDbid() + " " + list2.get(i).getName());
            }              
            return null;
        }
        for (int j = days - 1; j >= 0; j--) {
            List<StockItem> list2 = stocklistPeriod[period][j];
            int max = Math.min(topbottom, list2.size());
            for (int i = 0; i < max; i++) {
                String id = list0.get(i);
                StockItem stock = null;
                for (int k = 0; k < list2.size(); k++) {
                    StockItem tmpstock = list2.get(k); 
                    if (id.equals(tmpstock.getId())) {
                        stock = tmpstock;
                        break;
                    }
                }
                try {
                    if (stock != null) {
                        dataset.addValue(StockDao.getMainPeriod(stock, period), stock.getName() , new Integer(-j));
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
     * @param category we want
     * @return a list of size from list
     */

    public static List<StockItem> listFilterTop(List<StockItem> list, List<StockItem> listmain, int size) {
        List<StockItem> retlist = new ArrayList<StockItem>();
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

    public static List<StockItem> listFilterBottom(List<StockItem> list, List<StockItem> listmain, int size, int period) {
        List<StockItem> retlist = new ArrayList<>();
        int start;
        for (start = listmain.size() -1; start >= 0; start --) {
            if (start >= 0) {
                StockItem stock = listmain.get(start);
                try {
                    if (StockDao.getPeriod(stock, period)[0] != null) {
                        break;
                    }
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        }
        for (int i = start; i > start - size; i--) {
            if (i >= 0) {
                String id = listmain.get(i).getId();
                for (int j = list.size() - 1; j >= 0; j --) {
                    if (id.equals(list.get(j).getId())) {
                        retlist.add(list.get(j));
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

    public static double getMax(List<StockItem> stocklist, int period) {
        double max = -1000000;
        for (int i = 0; i < stocklist.size() ; i++) {
            double cur = -1000000;
            if (period == 0) {
                Double periodval = null;
                try {
                    periodval = StockDao.getMainPeriod(stocklist.get(i), period);
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

    public static double getMax2(List<StockItem> stocklist, int period) {
        double max = -1000000;
        for (int i = 0; i < stocklist.size() ; i++) {
            double cur = -1000000;
            if (period == 0) {
                Double periodval = null;
                try {
                    periodval = StockDao.getMainValue(stocklist.get(i), period);
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

    public static double getMin(List<StockItem> stocklist, int period) {
        double min = 1000000;
        for (int i = 0; i < stocklist.size() ; i++) {
            double cur = 1000000;
            if (period == 0) {
                Double periodval = null;
                try {
                    periodval = StockDao.getMainValue(stocklist.get(i), period);
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
    
    public static Map<String, List<StockItem>> filterFew(Map<String, List<StockItem>> stockdatemap, double filter) {
        double mysum = stockdatemap.values().stream().mapToInt(List::size).sum();
        double avg = mysum / stockdatemap.size();
        double limit = avg * filter;
        List<Integer> removedsizes = stockdatemap.entrySet().stream().filter(e -> e.getValue().size() < limit).map(e -> e.getValue().size()).toList();
        log.info("Removed {} {} {} {}", removedsizes.size(), avg, limit, removedsizes);
        stockdatemap = stockdatemap.entrySet().stream().filter(e -> e.getValue().size() >= limit).collect(Collectors.toMap(Entry::getKey,Entry::getValue));
        return stockdatemap;
    }
    
}
