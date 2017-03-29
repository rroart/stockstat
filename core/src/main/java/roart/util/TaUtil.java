package roart.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;

import roart.model.StockItem;

public class TaUtil {

    private static Logger log = LoggerFactory.getLogger(TaUtil.class);

    Object[] getRSI(int days, String market,
            String id, Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        MarketData marketdata = marketdatamap.get(market);
        Integer periodInt = StockUtil.getPeriodByMarket(market, pairs);
        List<StockItem> datedstocklists[] = marketdata.datedstocklists;
        double values[] = new double[days];
        int size = 0;
        int count = days - 1;
        int downcount = Math.min(days, datedstocklists.length);
        for (int j = 0; j < datedstocklists.length && downcount > 0 ; j++) {
            List<StockItem> list = datedstocklists[j];
            if (list == null) {
                log.info("listnull " + market + " " + " " + j);
                continue;
            }
            if (periodInt == null) {
                continue;
            }
            int period = periodInt;
            grr:  for (int i = 0; i < list.size(); i++) {
                StockItem stock = list.get(i);
                Pair<String, String> pair = new Pair(market, stock.getId());
                if (ids.contains(pair)) {
                    try {
                        Double value = StockDao.getValue(stock, period);
                        if (value == null) {
                            continue;
                        }
                        double val = value;
                        values[count] = value;
                        count--;
                        downcount--;
                        size++;
                        break grr;
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);
                    }
                }
            }    
        }
        Object[] objs = getInnerRSI(values, size);
        return objs;

    }

    Object[] getRSI_orig(int days, String market, String id,
            Set<Pair> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        MarketData marketdata = marketdatamap.get(market);
        Integer periodInt = StockUtil.getPeriodByMarket(market, pairs);
        List<StockItem> datedstocklists[] = marketdata.datedstocklists;
        double values[] = new double[days];
        int size = 0;
        for (int j = days - 1; j >= 0; j--) {
            List<StockItem> list = datedstocklists[j];
            if (list == null) {
                log.info("listnull " + market + " " + " " + j);
                continue;
            }
            int period = periodInt;
            for (int i = 0; i < list.size(); i++) {
                StockItem stock = list.get(i);
                Pair<String, String> pair = new Pair(market, stock.getId());
                if (ids.contains(pair)) {
                    try {
                        Double value = StockDao.getPeriod(stock, period);
                        if (value == null) {
                            continue;
                        }
                        double val = value;
                        values[size] = value;
                        size++;
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);

                    }
                }
            }    
        }
        Object[] objs = getInnerRSI(values, size);
        return objs;

    }

	private Object[] getInnerRSI(double[] values, int size) {
		Core core = new Core();
        MInteger beg = new MInteger();
        MInteger end = new MInteger();
        double rsi[] = new double[values.length];
        core.rsi(0, size - 1, values, 14, beg, end, rsi);
        Object[] objs = new Object[3];
        objs[0] = rsi;
        objs[1] = beg;
        objs[2] = end;
		return objs;
	}

    public double getRSI2(int days, String market, String id,
            Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        Object objs[] = getRSI(days, market, id, ids, marketdatamap, perioddata, periodstr);
        double rsi[] = (double[]) objs[0];
        MInteger beg = (MInteger) objs[1];
        MInteger end = (MInteger) objs[2];
        try {
            //log.info("hist " + id + " " + hist.length + " " + hist[0] + " " + hist[end.value -1 ] + " " + end.value + " " + hist[beg.value - 1] + " " + hist[hist.length -1]);
        } catch (Exception e) {

        }
        if (end.value == 0) {
            return 0;
        }
        log.info("end.value " + end.value + " " + rsi[end.value - 1]);
        return rsi[end.value - 1];
    }

    public DefaultCategoryDataset getRSIChart(int days, String market, String id,
            Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        Object objs[] = getRSI(days, market, id, ids, marketdatamap, perioddata, periodstr);
        double rsi[] = (double[]) objs[0];
        for (int i = 0; i < rsi.length; i ++) {
            //log.info("grr3 " + i + " " + macd[i]);
        }
        MInteger beg = (MInteger) objs[1];
        MInteger end = (MInteger) objs[2];
        int size = beg.value + end.value;
        size--;
        for (int i = 0; i < beg.value; i++) {
            dataset.addValue(0, "rsi" , new Integer(i - size));
        }
        for (int i = 0; i < end.value; i++) {
            dataset.addValue(rsi[i], "rsi" , new Integer(i + beg.value - size));
            if ("EUCA000699".equals(id)) {
                log.info("hist i " + i + " " + rsi[i]);
            }
        }
        return dataset;
    }

    Object[] getMACD(int days, String market,
            String id, Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        MarketData marketdata = marketdatamap.get(market);
        Integer periodInt = StockUtil.getPeriodByMarket(market, pairs);
        List<StockItem> datedstocklists[] = marketdata.datedstocklists;
        double values[] = new double[days];
        int size = getArr(days, market, id, ids, periodInt, datedstocklists, values);
        log.info("india " + java.util.Arrays.toString(values));
        Object[] objs = getInnerMACD(values, size);
        return objs;
    }

	private int getArr(int days, String market, String id, Set<Pair<String, String>> ids, Integer periodInt,
			List<StockItem>[] datedstocklists, double[] values) {
		int size = 0;
        int count = days - 1;
        int downcount = Math.min(days, datedstocklists.length);
        for (int j = 0; j < datedstocklists.length && downcount > 0 ; j++) {
            //        for (int j = datedstocklists.length - 1; j >= 0 && downcount > 0 ; j--) {
            List<StockItem> list = datedstocklists[j];
            if (list == null) {
                log.info("listnull " + market + " " + " " + j);
                continue;
            }
            if (periodInt == null) {
                //System.out.println("tata " + market + " " + periodstr);
                continue;
            }
            int period = periodInt;
            grr:  for (int i = 0; i < list.size(); i++) {
                StockItem stock = list.get(i);
                Pair<String, String> pair = new Pair(market, stock.getId());
                if (ids.contains(pair)) {
                    try {
                        Double value = StockDao.getValue(stock, period);
                        if (value == null) {
                            continue;
                        }
                        double val = value;
                        values[count] = value;
                        count--;
                        downcount--;
                        size++;
                        break grr;
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);

                    }
                }
            }    
        }
		return size;
	}

    Object[] getMACD_orig(int days, String market, String id,
            Set<Pair> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        MarketData marketdata = marketdatamap.get(market);
        Integer periodInt = StockUtil.getPeriodByMarket(market, pairs);
        List<StockItem> datedstocklists[] = marketdata.datedstocklists;
        double values[] = new double[days];
        int size = 0;
        for (int j = days - 1; j >= 0; j--) {
            List<StockItem> list = datedstocklists[j];
            if (list == null) {
                log.info("listnull " + market + " " + " " + j);
                continue;
            }
            int period = periodInt;
            for (int i = 0; i < list.size(); i++) {
                StockItem stock = list.get(i);
                Pair<String, String> pair = new Pair(market, stock.getId());
                if (ids.contains(pair)) {
                    try {
                        Double value = StockDao.getPeriod(stock, period);
                        if (value == null) {
                            continue;
                        }
                        double val = value;
                        values[size] = value;
                        size++;
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);

                    }
                }
            }    
        }
        Object[] objs = getInnerMACD(values, size);
        return objs;
    }

	private Object[] getInnerMACD(double[] values, int size) {
		Core core = new Core();
        MInteger beg = new MInteger();
        MInteger end = new MInteger();
        double macd[] = new double[values.length];
        double sig[] = new double[values.length];
        double hist[] = new double[values.length];
        core.macd(0, size - 1, values, 26, 12, 9, beg, end, macd, sig, hist);
        Object[] objs = new Object[5];
        objs[0] = macd;
        objs[1] = sig;
        objs[2] = hist;
        objs[3] = beg;
        objs[4] = end;
		return objs;
	}

    public double getMom(int days, String market,
            String id, Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        Object objs[] = getMACD(days, market, id, ids, marketdatamap, perioddata, periodstr);
        double hist[] = (double[]) objs[2];
        MInteger beg = (MInteger) objs[3];
        MInteger end = (MInteger) objs[4];
        try {
            //log.info("hist " + id + " " + hist.length + " " + hist[0] + " " + hist[end.value -1 ] + " " + end.value + " " + hist[beg.value - 1] + " " + hist[hist.length -1]);
        } catch (Exception e) {

        }
        if (end.value == 0) {
            return 0;
        }
        return hist[end.value - 1];
    }

    public DefaultCategoryDataset getMACDChart(int days, String market,
            String id, Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        System.out.println("parsize " + pairs.size());
        Object objs[] = getMACD(days, market, id, ids, marketdatamap, perioddata, periodstr);
        double macd[] = (double[]) objs[0];
        double sig[] = (double[]) objs[1];
        double hist[] = (double[]) objs[2];
        for (int i = 0; i < macd.length; i ++) {
            //log.info("grr3 " + i + " " + macd[i]);
        }
        MInteger beg = (MInteger) objs[3];
        MInteger end = (MInteger) objs[4];
        int size = beg.value + end.value;
        size--;
        for (int i = 0; i < beg.value; i++) {
            dataset.addValue(0, "macd" , new Integer(i - size));
            dataset.addValue(0, "sig" , new Integer(i - size));
            dataset.addValue(0, "hist" , new Integer(i - size));
        }
        for (int i = 0; i < end.value; i++) {
            dataset.addValue(macd[i], "macd" , new Integer(i + beg.value - size));
            dataset.addValue(sig[i], "sig" , new Integer(i + beg.value - size));
            dataset.addValue(hist[i], "hist" , new Integer(i + beg.value - size));
        }
        return dataset;
    }

	public Double[] getRSI(List<Double> list, int days, boolean wantdiff, int diffdays) {
    	int retsize = 1;
		if (wantdiff) {
    		retsize++;
    	}
		Double[] retValues = new Double[retsize];
		double values[] = new double[days];
	    int size = getArrayNonNullReverse(list, values);
	    Object[] objs = getInnerRSI(values, size);
        retValues[0] = getRSI(objs);
        if (wantdiff) {
        	retValues[1] = getRSIdiff(objs, diffdays);
        }
        return retValues;
	}

	private double getRSI(Object[] objs) {
		double rsi[] = (double[]) objs[0];
        MInteger end = (MInteger) objs[2];
        if (end.value == 0) {
            return 0;
        }
        return rsi[end.value - 1];
	}

	private double getRSIdiff(Object[] objs, int diffdays) {
		double rsi[] = (double[]) objs[0];
        MInteger end = (MInteger) objs[2];
        if (end.value == 0) {
            return 0;
        }
        double diff = 0;
        int min = Math.max(0, end.value - diffdays);
        diff = rsi[end.value - 1] - rsi[min];
        return diff/(diffdays - 1);
	}

	private int getArrayNonNullReverse(List<Double> list, double[] values) {
		int count = values.length;
		for (Double val : list) {
			// TODO bounds check
	    	if (val != null && count > 0) {
	    		values[--count] = val;
	    	}
	    }
		return values.length - count;
	}

	private int getArrayNonNull(List<Double> list, double[] values) {
		int size = 0;
		for (Double val : list) {
			// TODO bounds check
	    	if (val != null && size < values.length) {
	    		values[size++] = val;
	    	}
	    }
		return size;
	}

	public double getMomderiv(List<Double> list, int days, int diffdays) {
		double values[] = new double[days];
	    int size = getArrayNonNullReverse(list, values);
	    Object[] objs = getInnerMACD(values, size);
        return getMomDiff(objs, diffdays);
	}

	private double getMomDiff( Object[] objs, int diffdays) {
		double hist[] = (double[]) objs[2];
        MInteger end = (MInteger) objs[4];
        if (end.value == 0) {
            return 0;
        }
        double diff = 0;
        /*
        for (int i = end.value - 1; i >= end.value - derivdays && i >= 1; i--) {
        	double deriv = hist[i] - hist[i - 1];
        	if (deriv < 0 && sum >= 0) {
        		return 0;
        	}
        	if (deriv >= 0 && sum < 0) {
        		return 0;
        	}
        	sum += deriv;
        }
        */
        int min = Math.max(0, end.value - diffdays);
        diff = hist[end.value - 1] - hist[min];
        return diff/(diffdays - 1);
	}
	
	public Double[] getMomAndDiff(List<Double> list, int days, boolean wantdiff, int diffdays) {
    	int retsize = 1;
		if (wantdiff) {
    		retsize++;
    	}
		Double[] retValues = new Double[retsize];
		double values[] = new double[days];
	    int size = getArrayNonNullReverse(list, values);
	    Object[] objs = getInnerMACD(values, size);
        retValues[0] = getMom(objs);
        if (wantdiff) {
        	retValues[1] = getMomDiff(objs, diffdays);
        }
        return retValues;
	}

	private double getMom(Object[] objs) {
		double hist[] = (double[]) objs[2];
        MInteger end = (MInteger) objs[4];
        if (end.value == 0) {
            return 0;
        }
        return hist[end.value - 1];
	}
}
