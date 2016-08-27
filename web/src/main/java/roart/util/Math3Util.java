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

import roart.model.ResultItem;
import roart.model.Stock;
import roart.service.ControlService;

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
    
    public static void getStats(List<ResultItem> retList, Date datedate, int count, Map<String, List<Stock>> stockidmap, Map<String, List<Stock>> stockdatemap) {
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
            List<Stock> stocks1 = stockidmap.get(id1);
            stocks1.sort(StockUtil.StockDateComparator);
            for (int i = 0; i < StockUtil.PERIODS; i++) {
                List<Stock> stockstrunc1 = listtrunc(stocks1, count, i, index);
                for (String id2 : stockidmap.keySet()) {
                    if (id2.equals(id1)) {
                        continue;
                    }
                    try {
                    List<Stock> stocks2 = stockidmap.get(id2);
                    stocks2.sort(StockUtil.StockDateComparator);
                    List<Stock> stockstrunc2 = listtrunc(stocks2, stockstrunc1, i);
                    List<Stock> stockstrunc3 = listtrunc(stockstrunc1, stockstrunc2, i);
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
                    ResultItem r = new ResultItem();
                    r.add(stocks1.get(0).getId() + "," + stocks2.get(0).getId());
                    r.add(stocks1.get(0).getName());
                    r.add(stocks2.get(0).getName());
                    //SimpleDateFormat dt = new SimpleDateFormat("yyyy.MM.dd");
                    //r.add(dt.format(stock.getDate()));
                    r.add(i + 1);
                    r.add(sample1.length);
                    r.add(t1);
                    r.add(t2);
                    r.add("" + b);
                    r.add(t1e);
                    r.add(t2e);
                    r.add("" + be);
                    r.add(sp);
                    r.add(ke);
                    r.add(pe);
                   //r.add(stock.get());
                    retList.add(r);
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
    
    private static double[] getSample(List<Stock> stockstrunc, int period, boolean equalize) {
        double[] ret = new double[stockstrunc.size()];
        double max = StockUtil.getMax(stockstrunc, period);
        for (int i = 0; i < stockstrunc.size(); i++) {
            Double periodval = null;
            try {
                periodval = StockDao.getPeriod(stockstrunc.get(i), period + 1);
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
    
     private static List<Stock> listtrunc(List<Stock> stocks2,
            List<Stock> stocks1, int period) {
         List<Stock> newList = new ArrayList<Stock>();
         for (int i = 0, j = 0; i < stocks1.size() && j < stocks1.size(); i++) {
             for (int k = 0; k < stocks2.size(); k++) {
                 if (stocks1.get(i).getDate().equals(stocks2.get(k).getDate())) {
                     Double periodval = null;
                     try {
                         periodval = StockDao.getPeriod(stocks2.get(k), period + 1);
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
    
    private static List<Stock> listtrunc(List<Stock> stocks, int count, int period, int startoffset) {
        List<Stock> newList = new ArrayList<Stock>();
        for (int i = 0, j = 0; (i + startoffset) < stocks.size() && j < count ; i++) {
            Double periodval = null;
            try {
                periodval = StockDao.getPeriod(stocks.get(i + startoffset), period + 1);
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