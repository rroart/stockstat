package roart.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.stat.inference.TTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.model.ResultItemTable;
import roart.model.ResultItemTableRow;
import roart.model.StockItem;

/**
 * 
 * @author roart
 *
 * Statistical utilities
 */

public class Math3Util {

    private static Logger log = LoggerFactory.getLogger(Math3Util.class);

    /**
     * Create a big table of statistics, based on pairwise statistical test between each pair of items in a market
     * 
     * @param retList the returned item list
     * @param datedate the desired date
     * @param count number of days to use in a sample for comparison
     * @param stockidmap a map of ids to stocklists
     * @param stockdatemap a map of date to stocklists
     */
    
    public static void getStats(ResultItemTable table, Date datedate, int count, Map<String, List<StockItem>> stockidmap, Map<String, List<StockItem>> stockdatemap) {
        List<String> list = new ArrayList(stockdatemap.keySet());
        Collections.sort(list);
        String date = null;
        if (datedate != null) {
            SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
            date = dt.format(datedate);                
        }
        int index = list.size() - 1 - StockUtil.getStockDate(list, date);
        System.out.println("index " + index);
        for (String id1 : stockidmap.keySet()) {
            List<StockItem> stocks1 = stockidmap.get(id1);
            stocks1.sort(StockUtil.StockDateComparator);
            for (int i = 0; i < StockUtil.PERIODS; i++) {
                List<StockItem> stockstrunc1 = listtrunc(stocks1, count, i, index);
                for (String id2 : stockidmap.keySet()) {
                    if (id2.equals(id1)) {
                        continue;
                    }
                    try {
                    List<StockItem> stocks2 = stockidmap.get(id2);
                    stocks2.sort(StockUtil.StockDateComparator);
                    List<StockItem> stockstrunc2 = listtrunc(stocks2, stockstrunc1, i);
                    List<StockItem> stockstrunc3 = listtrunc(stockstrunc1, stockstrunc2, i);
                    double[] sample1 = getSample(stockstrunc3, i, false);
                    double[] sample2 = getSample(stockstrunc2, i, false);
                    double[] sample1e = getSample(stockstrunc3, i, true);
                    double[] sample2e = getSample(stockstrunc2, i, true);
                    if (sample1.length < 2 || sample2.length < 2) {
                        log.error("sample too small " + stocks1.get(0).getName() + " " + stocks2.get(0).getName() + " " + sample1.length + " " + sample2.length);
                        continue;
                    }
                    if (sample1.length != sample2.length) {
                        log.error("diff sample " + stockstrunc2.get(0).getName() + " " + stockstrunc3.get(0).getName() + " " + sample1.length + " " + sample2.length);
                        continue;
                    }
                    TTest ttest = new TTest();
                    double t1 = ttest.pairedT(sample1, sample2);
                    double t2 = ttest.pairedTTest(sample1, sample2);
                    boolean b = ttest.pairedTTest(sample1, sample2, 0.05);
                    double t1e = ttest.pairedT(sample1e, sample2e);
                    double t2e = ttest.pairedTTest(sample1e, sample2e);
                    boolean be = ttest.pairedTTest(sample1e, sample2e, 0.05);
                    SpearmansCorrelation sc = new SpearmansCorrelation();
                    double sp = sc.correlation(sample1e, sample2e);
                    KendallsCorrelation kc = new KendallsCorrelation();
                    double ke = kc.correlation(sample1e, sample2e);
                    PearsonsCorrelation pc = new PearsonsCorrelation();
                    double pe = pc.correlation(sample1e, sample2e);
                    if (false /*sample1.length < 10*/) {
                        log.info("ttest " + stockstrunc2.get(0).getName() + " " + stockstrunc3.get(0).getName() + " " + t1 + " " + t2 + " " + sample1.length + " " + b + Arrays.toString(sample1) + " and "+ Arrays.toString(sample2) + " " + i + " " + stockstrunc2.get(0).getDate() + " " + stockstrunc3.get(0).getDate() + stockstrunc3.get(stockstrunc3.size() - 1).getDate());
                        log.info("ttest " + stockstrunc2.get(0).getName() + " " + stockstrunc3.get(0).getName() + " " + t1e + " " + t2e + " " + sample1.length + " " + be + Arrays.toString(sample1e) + " and "+ Arrays.toString(sample2e) + " " + i + " " + stockstrunc2.get(0).getDate() + " " + stockstrunc3.get(0).getDate() + stockstrunc3.get(stockstrunc3.size() - 1).getDate());
                        if (b != be) {
                            log.info("ttestdiff " + stockstrunc2.get(0).getName() + " " + stockstrunc3.get(0).getName() + " " + i); 
                        }
                    }
                    ResultItemTableRow row = new ResultItemTableRow();
                    row.add(stocks1.get(0).getId() + "," + stocks2.get(0).getId());
                    row.add(stocks1.get(0).getName());
                    row.add(stocks2.get(0).getName());
                    //SimpleDateFormat dt = new SimpleDateFormat("yyyy.MM.dd");
                    //r.add(dt.format(stock.getDate()));
                    row.add(i + 1);
                    row.add(sample1.length);
                    row.add(t1);
                    row.add(t2);
                    row.add("" + b);
                    row.add(t1e);
                    row.add(t2e);
                    row.add("" + be);
                    row.add(sp);
                    row.add(ke);
                    row.add(pe);
                   //r.add(stock.get());
                    table.add(row);
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);
                    }
                }
            }
        } 
    }

    public static void getStats2(ResultItemTable table, Date datedate, int count, Map<String, List<StockItem>> stockidmap, Map<String, List<StockItem>> stockdatemap) {
        List<String> list = new ArrayList(stockdatemap.keySet());
        Collections.sort(list);
        String date = null;
        if (datedate != null) {
            SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
            date = dt.format(datedate);                
        }
        int index = list.size() - 1 - StockUtil.getStockDate(list, date);
        System.out.println("index " + index);
        Map<Integer, Map<String, List<StockItem>>> periodmap = new HashMap<>();
        for (int i = 0; i < StockUtil.PERIODS; i++) {
            Map<String, List<StockItem>> mapSample = new HashMap<>();
            Map<String, List<StockItem>> mapSampleE = new HashMap<>();
            for (String id : stockidmap.keySet()) {
            List<StockItem> stocks = stockidmap.get(id);
            stocks.sort(StockUtil.StockDateComparator);
                List<StockItem> stockstrunc = listtrunc(stocks, count, i, index);
                for (String idnot : stockidmap.keySet()) {
                    if (idnot.equals(id)) {
                        continue;
                    }
                    try {
                    List<StockItem> stocksnot = stockidmap.get(idnot);
                    stocksnot.sort(StockUtil.StockDateComparator);
                    List<StockItem> stockstruncnot2 = listtrunc(stocksnot, stockstrunc, i);
                    List<StockItem> stockstrunc3 = listtrunc(stockstrunc, stockstruncnot2, i);
                    double[] sample1 = getSample(stockstrunc3, i, false);
                    double[] sample2 = getSample(stockstruncnot2, i, false);
                    double[] sample1e = getSample(stockstrunc3, i, true);
                    double[] sample2e = getSample(stockstruncnot2, i, true);
                    if (sample1.length < 2 || sample2.length < 2) {
                        log.error("sample too small " + stocks.get(0).getName() + " " + stocksnot.get(0).getName() + " " + sample1.length + " " + sample2.length);
                        continue;
                    }
                    if (sample1.length != sample2.length) {
                        log.error("diff sample " + stockstruncnot2.get(0).getName() + " " + stockstrunc3.get(0).getName() + " " + sample1.length + " " + sample2.length);
                        continue;
                    }
                    TTest ttest = new TTest();
                    double t1 = ttest.pairedT(sample1, sample2);
                    double t2 = ttest.pairedTTest(sample1, sample2);
                    boolean b = ttest.pairedTTest(sample1, sample2, 0.05);
                    double t1e = ttest.pairedT(sample1e, sample2e);
                    double t2e = ttest.pairedTTest(sample1e, sample2e);
                    boolean be = ttest.pairedTTest(sample1e, sample2e, 0.05);
                    SpearmansCorrelation sc = new SpearmansCorrelation();
                    double sp = sc.correlation(sample1e, sample2e);
                    KendallsCorrelation kc = new KendallsCorrelation();
                    double ke = kc.correlation(sample1e, sample2e);
                    PearsonsCorrelation pc = new PearsonsCorrelation();
                    double pe = pc.correlation(sample1e, sample2e);
                    if (false /*sample1.length < 10*/) {
                        log.info("ttest " + stockstruncnot2.get(0).getName() + " " + stockstrunc3.get(0).getName() + " " + t1 + " " + t2 + " " + sample1.length + " " + b + Arrays.toString(sample1) + " and "+ Arrays.toString(sample2) + " " + i + " " + stockstruncnot2.get(0).getDate() + " " + stockstrunc3.get(0).getDate() + stockstrunc3.get(stockstrunc3.size() - 1).getDate());
                        log.info("ttest " + stockstruncnot2.get(0).getName() + " " + stockstrunc3.get(0).getName() + " " + t1e + " " + t2e + " " + sample1.length + " " + be + Arrays.toString(sample1e) + " and "+ Arrays.toString(sample2e) + " " + i + " " + stockstruncnot2.get(0).getDate() + " " + stockstrunc3.get(0).getDate() + stockstrunc3.get(stockstrunc3.size() - 1).getDate());
                        if (b != be) {
                            log.info("ttestdiff " + stockstruncnot2.get(0).getName() + " " + stockstrunc3.get(0).getName() + " " + i); 
                        }
                    }
                    ResultItemTableRow row = new ResultItemTableRow();
                    row.add(stocks.get(0).getId() + "," + stocksnot.get(0).getId());
                    row.add(stocks.get(0).getName());
                    row.add(stocksnot.get(0).getName());
                    //SimpleDateFormat dt = new SimpleDateFormat("yyyy.MM.dd");
                    //r.add(dt.format(stock.getDate()));
                    row.add(i + 1);
                    row.add(sample1.length);
                    row.add(t1);
                    row.add(t2);
                    row.add("" + b);
                    row.add(t1e);
                    row.add(t2e);
                    row.add("" + be);
                    row.add(sp);
                    row.add(ke);
                    row.add(pe);
                   //r.add(stock.get());
                    table.add(row);
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);
                    }
                }
            }
        } 
    }

    /**
     * Create sample out a truncated stock list
     * 
     * @param stockstrunc stock list short
     * @return sample for t test
     */
    
    private static double[] getSample(List<StockItem> stockstrunc, int period, boolean equalize) {
        double[] ret = new double[stockstrunc.size()];
        double max = StockUtil.getMax(stockstrunc, period);
        for (int i = 0; i < stockstrunc.size(); i++) {
            Double periodval = null;
            try {
                periodval = StockDao.getMainPeriod(stockstrunc.get(i), period);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            if (periodval != null) {
                if (!equalize) {
                    ret[i] = periodval;
                } else {
                    ret[i] = 100*periodval/max;
                }
            } else {
                log.error("periodval null");
            }
        }
        return ret;
    }

    /**
     * Return truncated list by the same dates
     * 
     * @param stocks2 input list
     * @param stocks1 use the dates from this list
     * @return truncated list
     */
    
     private static List<StockItem> listtrunc(List<StockItem> stocks2,
            List<StockItem> stocks1, int period) {
         List<StockItem> newList = new ArrayList<StockItem>();
         for (int i = 0, j = 0; i < stocks1.size() && j < stocks1.size(); i++) {
             for (int k = 0; k < stocks2.size(); k++) {
                 if (stocks1.get(i).getDate().equals(stocks2.get(k).getDate())) {
                     Double periodval = null;
                     try {
                         periodval = StockDao.getMainPeriod(stocks2.get(k), period);
                     } catch (Exception e) {
                         log.error(Constants.EXCEPTION, e);
                     }
                     if (periodval != null) {
                         newList.add(stocks2.get(k));
                         j++;
                     }
                 }
             }
         }
         return newList;
    }

    /**
     * Return truncated list by count
     * @param count return count
     * @param startoffset for other than recent date
     * @param stocks1 input list
     * 
     * @return truncated list
     */
    
    private static List<StockItem> listtrunc(List<StockItem> stocks, int count, int period, int startoffset) {
        List<StockItem> newList = new ArrayList<StockItem>();
        for (int i = 0, j = 0; (i + startoffset) < stocks.size() && j < count ; i++) {
            Double periodval = null;
            try {
                periodval = StockDao.getMainPeriod(stocks.get(i + startoffset), period);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            if (periodval != null) {
                newList.add(stocks.get(i + startoffset));
                j++;
            }
        }
        return newList;
    }

}
