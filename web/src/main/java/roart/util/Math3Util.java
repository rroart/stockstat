package roart.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.stat.inference.TTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.model.ResultItem;
import roart.model.Stock;
import roart.service.ControlService;

public class Math3Util {

    private static Logger log = LoggerFactory.getLogger(Math3Util.class);

    public static void getStats(List<ResultItem> retList, HashMap<String, Integer>[][] periodmaps, int count, HashMap<String, List<Stock>> stockidmap) {
        for (String id1 : stockidmap.keySet()) {
            List<Stock> stocks1 = stockidmap.get(id1);
            stocks1.sort(StockUtil.StockDateComparator);
            for (int i = 0; i < StockUtil.PERIODS; i++) {
                List<Stock> stockstrunc1 = listtrunc(stocks1, count, i);
                for (String id2 : stockidmap.keySet()) {
                    if (id2.equals(id1)) {
                        continue;
                    }
                    List<Stock> stocks2 = stockidmap.get(id2);
                    stocks2.sort(StockUtil.StockDateComparator);
                    List<Stock> stockstrunc2 = listtrunc(stocks2, stockstrunc1, i);
                    List<Stock> stockstrunc3 = listtrunc(stockstrunc1, stockstrunc2, i);
                    double[] sample1 = getSample(stockstrunc3, i);
                    double[] sample2 = getSample(stockstrunc2, i);
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
                                       //log.info("ttest " + stockstrunc2.get(0).getName() + " " + stockstrunc3.get(0).getName() + " " + t1 + " " + t2 + " " + sample1.length + " " + sample2.length);
                    ResultItem r = new ResultItem();
                    r.add(stocks1.get(0).getId() + "," + stocks2.get(0).getId());
                    r.add(stocks1.get(0).getName());
                    r.add(stocks2.get(0).getName());
                    //SimpleDateFormat dt = new SimpleDateFormat("yyyy.MM.dd");
                    //r.add(dt.format(stock.getDate()));
                    r.add(i);
                    r.add(sample1.length);
                    r.add(t1);
                    r.add(t2);
                    r.add("" + b);
                   //r.add(stock.get());
                    retList.add(r);
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
    
    private static double[] getSample(List<Stock> stockstrunc, int period) {
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
                if (!ControlService.isEqualize()) {
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
     * 
     * @param stocks1 input list
     * @param count return count
     * @return truncated list
     */
    
    private static List<Stock> listtrunc(List<Stock> stocks, int count, int period) {
        List<Stock> newList = new ArrayList<Stock>();
        for (int i = 0, j = 0; i < stocks.size() && j < count ; i++) {
            Double periodval = null;
            try {
                periodval = StockDao.getPeriod(stocks.get(i), period + 1);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            if (periodval != null) {
                newList.add(stocks.get(i));
                j++;
            }
        }
        return newList;
    }

}