package roart.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.apache.spark.scheduler.BeginEvent;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;

import roart.model.StockItem;

public class TaUtil {

    private static Logger log = LoggerFactory.getLogger(TaUtil.class);

    // TODO make OO version of this, when getting another stat lib
    
    Object[] getRSI(int days, String market,
            String id, Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        MarketData marketdata = marketdatamap.get(market);
        Integer periodInt = StockUtil.getPeriodByMarket(market, pairs);
        List<StockItem> datedstocklists[] = marketdata.datedstocklists;
        double values[] = new double[days];
        int size = getArr(days, market, id, ids, periodInt, datedstocklists, values);
        //int size = getArr2(days, market, ids, periodInt, datedstocklists, values);
        Object[] objs = getInnerRSI(values, size);
        return objs;

    }

    Object[] getSTOCHRSI(int days, String market,
            String id, Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        MarketData marketdata = marketdatamap.get(market);
        Integer periodInt = StockUtil.getPeriodByMarket(market, pairs);
        List<StockItem> datedstocklists[] = marketdata.datedstocklists;
        double values[] = new double[days];
        int size = getArr(days, market, id, ids, periodInt, datedstocklists, values);
        //int size = getArr2(days, market, ids, periodInt, datedstocklists, values);
        Object[] objs = getInnerSTOCHRSI(values, size);
        return objs;

    }

    Object[] getCCI(int days, String market,
            String id, Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        MarketData marketdata = marketdatamap.get(market);
        Integer periodInt = StockUtil.getPeriodByMarket(market, pairs);
        List<StockItem> datedstocklists[] = marketdata.datedstocklists;
        double low[] = new double[days];
        double high[] = new double[days];
        double close[] = new double[days];
        // TODO make new getarr for low high
        int size = getArr(days, market, id, ids, periodInt, datedstocklists, close);
        //int size = getArr2(days, market, ids, periodInt, datedstocklists, values);
        Object[] objs = getInnerCCI(low, high, close, size);
        return objs;

    }

    Object[] getATR(int days, String market,
            String id, Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        MarketData marketdata = marketdatamap.get(market);
        Integer periodInt = StockUtil.getPeriodByMarket(market, pairs);
        List<StockItem> datedstocklists[] = marketdata.datedstocklists;
        double low[] = new double[days];
        double high[] = new double[days];
        double close[] = new double[days];
        // TODO make new getarr for low high
        int size = getArr(days, market, id, ids, periodInt, datedstocklists, close);
        //int size = getArr2(days, market, ids, periodInt, datedstocklists, values);
        Object[] objs = getInnerATR(low, high, close, size);
        return objs;

    }

    Object[] getSTOCH(int days, String market,
            String id, Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        MarketData marketdata = marketdatamap.get(market);
        Integer periodInt = StockUtil.getPeriodByMarket(market, pairs);
        List<StockItem> datedstocklists[] = marketdata.datedstocklists;
        double low[] = new double[days];
        double high[] = new double[days];
        double close[] = new double[days];
        // TODO make new getarr for low high
        int size = getArr(days, market, id, ids, periodInt, datedstocklists, close);
        //int size = getArr2(days, market, ids, periodInt, datedstocklists, values);
        Object[] objs = getInnerSTOCH(low, high, close, size);
        return objs;

    }

	private int getArr2(int days, String market, Set<Pair<String, String>> ids, Integer periodInt,
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

    Object[] getRSI_orig(int days, String market, String id,
            Set<Pair> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        MarketData marketdata = marketdatamap.get(market);
        Integer periodInt = StockUtil.getPeriodByMarket(market, pairs);
        List<StockItem> datedstocklists[] = marketdata.datedstocklists;
        double values[] = new double[days];
        int size = getArrForOrig(days, market, ids, periodInt, datedstocklists, values);
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
        //log.info("rsi beg end " + beg.value + " " + end.value + Arrays.toString(rsi));
		return objs;
	}

	private Object[] getInnerCCI(double[] low, double[] high, double[] close, int size) {
		Core core = new Core();
        MInteger beg = new MInteger();
        MInteger end = new MInteger();
        double rsi[] = new double[close.length];
        core.cci (0, size - 1, low, high, close, 14, beg, end, rsi);
        //core.cci .cci(0, size - 1, values, 14, beg, end, rsi);
        Object[] objs = new Object[3];
        objs[0] = rsi;
        objs[1] = beg;
        objs[2] = end;
		return objs;
	}

	private Object[] getInnerATR(double[] low, double[] high, double[] close, int size) {
		Core core = new Core();
        MInteger beg = new MInteger();
        MInteger end = new MInteger();
        double rsi[] = new double[close.length];
        core.atr(0, size - 1, low, high, close, 14, beg, end, rsi);
        Object[] objs = new Object[3];
        objs[0] = rsi;
        objs[1] = beg;
        objs[2] = end;
		return objs;
	}

	private Object[] getInnerSTOCH(double[] low, double[] high, double[] close, int size) {
		Core core = new Core();
        MInteger beg = new MInteger();
        MInteger end = new MInteger();
        double rsi[] = new double[close.length];
        double rsi2[] = new double[close.length];
        //core.stoch .stoch(0, size - 1, close, 14, beg, end, rsi, rsi2);
        Object[] objs = new Object[3];
        objs[0] = rsi;
        objs[1] = beg;
        objs[2] = end;
		return objs;
	}

	private Object[] getInnerSTOCHRSI(double[] values, int size) {
		Core core = new Core();
        MInteger beg = new MInteger();
        MInteger end = new MInteger();
        double rsi[] = new double[values.length];
        double rsi2[] = new double[values.length];
        MAType optInFastD;
        optInFastD = MAType.Sma;
        core.stochRsi(0, size - 1, values, 14, 5, 3, optInFastD, beg, end, rsi, rsi2);
        Object[] objs = new Object[4];
        objs[0] = rsi;
        objs[1] = rsi2;
        objs[2] = beg;
        objs[3] = end;
        //log.info("srs in" + Arrays.toString(values));
        //log.info("srs beg end " + beg.value + " " + end.value);
        //log.info("srs1 " + Arrays.toString(rsi));
        //log.info("srs2 " + Arrays.toString(rsi2));
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
        //log.info("end.value " + end.value + " " + rsi[end.value - 1] + Arrays.toString(rsi));
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

    public DefaultCategoryDataset getSTOCHRSIChart(int days, String market, String id,
            Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        Object objs[] = getSTOCHRSI(days, market, id, ids, marketdatamap, perioddata, periodstr);
        double rsi[] = (double[]) objs[0];
        double rsi2[] = (double[]) objs[1];
        for (int i = 0; i < rsi.length; i ++) {
            //log.info("grr3 " + i + " " + macd[i]);
        }
        MInteger beg = (MInteger) objs[2];
        MInteger end = (MInteger) objs[3];
        int size = beg.value + end.value;
        size--;
        for (int i = 0; i < beg.value; i++) {
            dataset.addValue(0, "rsi" , new Integer(i - size));
            dataset.addValue(0, "rsi2" , new Integer(i - size));
        }
        for (int i = 0; i < end.value; i++) {
            dataset.addValue(rsi[i], "rsi" , new Integer(i + beg.value - size));
            dataset.addValue(rsi2[i], "rsi2" , new Integer(i + beg.value - size));
            if ("EUCA000699".equals(id)) {
                log.info("hist i " + i + " " + rsi[i]);
            }
        }
        return dataset;
    }

    public DefaultCategoryDataset getCCIChart(int days, String market, String id,
            Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        Object objs[] = getCCI(days, market, id, ids, marketdatamap, perioddata, periodstr);
        double cci[] = (double[]) objs[0];
        for (int i = 0; i < cci.length; i ++) {
            //log.info("grr3 " + i + " " + macd[i]);
        }
        MInteger beg = (MInteger) objs[1];
        MInteger end = (MInteger) objs[2];
        int size = beg.value + end.value;
        size--;
        for (int i = 0; i < beg.value; i++) {
            dataset.addValue(0, "cci" , new Integer(i - size));
        }
        for (int i = 0; i < end.value; i++) {
            dataset.addValue(cci[i], "cci" , new Integer(i + beg.value - size));
            if ("EUCA000699".equals(id)) {
                log.info("hist i " + i + " " + cci[i]);
            }
        }
        return dataset;
    }

    public DefaultCategoryDataset getATRChart(int days, String market, String id,
            Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        Object objs[] = getATR(days, market, id, ids, marketdatamap, perioddata, periodstr);
        double atr[] = (double[]) objs[0];
        for (int i = 0; i < atr.length; i ++) {
            //log.info("grr3 " + i + " " + macd[i]);
        }
        MInteger beg = (MInteger) objs[1];
        MInteger end = (MInteger) objs[2];
        int size = beg.value + end.value;
        size--;
        for (int i = 0; i < beg.value; i++) {
            dataset.addValue(0, "atr" , new Integer(i - size));
        }
        for (int i = 0; i < end.value; i++) {
            dataset.addValue(atr[i], "atr" , new Integer(i + beg.value - size));
            if ("EUCA000699".equals(id)) {
                log.info("hist i " + i + " " + atr[i]);
            }
        }
        return dataset;
    }

    public DefaultCategoryDataset getSTOCHChart(int days, String market, String id,
            Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        Object objs[] = getSTOCH(days, market, id, ids, marketdatamap, perioddata, periodstr);
        double stoch[] = (double[]) objs[0];
        for (int i = 0; i < stoch.length; i ++) {
            //log.info("grr3 " + i + " " + macd[i]);
        }
        MInteger beg = (MInteger) objs[1];
        MInteger end = (MInteger) objs[2];
        int size = beg.value + end.value;
        size--;
        for (int i = 0; i < beg.value; i++) {
            dataset.addValue(0, "stoch" , new Integer(i - size));
        }
        for (int i = 0; i < end.value; i++) {
            dataset.addValue(stoch[i], "stoch" , new Integer(i + beg.value - size));
            if ("EUCA000699".equals(id)) {
                log.info("hist i " + i + " " + stoch[i]);
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
        log.info("before " + size + " " + java.util.Arrays.toString(values));
        Object[] objs = getInnerMACD(values, size);
        return objs;
    }

	private int getArr(int days, String market, String id, Set<Pair<String, String>> ids, Integer periodInt,
			List<StockItem>[] datedstocklists, double[] values) {
	    int size = 0;
	    boolean display = false;
	    int count = 0;
	    int downcount = Math.min(days, datedstocklists.length);
	    System.out.println("downc " + downcount);
	    List<Double> listd = new ArrayList<>();
        for (int j = 0; j < datedstocklists.length  && downcount > 0 ; j++) {
	    //for (int j = datedstocklists.length - 1; j >= 0 && downcount > 0 ; j--) {
	        //        for (int j = datedstocklists.length - 1; j >= 0 && downcount > 0 ; j--) {
	        List<StockItem> list = datedstocklists[j];
	        if (j == 0) {
	            System.out.println("j 0");
	        }
	        if (list == null) {
	            log.info("listnull " + market + " " + " " + j);
	            continue;
	        }
	        if (periodInt == null) {
	            //System.out.println("tata " + market + " " + periodstr);
	            continue;
	        }
	        int period = periodInt;
	       //System.out.println("");
	        grr:  for (int i = 0; i < list.size(); i++) {
	            StockItem stock = list.get(i);
	            //System.out.print(" " + stock.getId());
	            Pair<String, String> pair = new Pair(market, stock.getId());
	            if (ids.contains(pair)) {
	                try {
	                    Double value = StockDao.getValue(stock, period);
	                    if (value == null) {
	                        display = true;
	                        continue;
	                    }
	                    if (j == 0) {
	                        System.out.println("jj 0");
	                    }
	                    double val = value;
	                    values[count] = value;
	                    listd.add(val);
	                    count++;
	                    downcount--;
	                    size++;
	                    break grr;
	                } catch (Exception e) {
	                    log.error(Constants.EXCEPTION, e);
System.out.println("grr " + count + " " + downcount + " " + size);
	                }
	            }
	        }    
	    }
        Collections.reverse(listd);
        Object[] newarr = listd.toArray();
        log.info("arrarr " + newarr + " " + values + " " + size + " " + newarr.length);
        for (int i = 0; i < size; i ++) {
            values[i] = (double) newarr[i];
        }
        //System.arraycopy(newarr, 0, values, 0, size);
	    //System.out.println("thearr " + Arrays.toString(values));
        if (display) {
            //log.info("mydisplay " + list);
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
        int size = getArrForOrig(days, market, ids, periodInt, datedstocklists, values);
        Object[] objs = getInnerMACD(values, size);
        return objs;
    }

	private int getArrForOrig(int days, String market, Set<Pair> ids, Integer periodInt,
			List<StockItem>[] datedstocklists, double[] values) {
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
		return size;
	}

	private Object[] getInnerEMA(double[] values, int size) {
		Core core = new Core();
        MInteger beg26 = new MInteger();
        MInteger end26 = new MInteger();
        MInteger beg12 = new MInteger();
        MInteger end12 = new MInteger();
        double ema26[] = new double[values.length];
        double ema12[] = new double[values.length];
        double hist[] = new double[values.length];
        core.ema(0, size - 1, values, 26, beg26, end26, ema26);
        core.ema(0, size - 1, values, 12, beg12, end12, ema12);
        Object[] objs = new Object[5];
        objs[0] = ema26;
        objs[1] = ema12;
        objs[2] = hist;
        objs[3] = beg26;
        objs[4] = end26;
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
        //System.out.println("outout1 " + Arrays.toString(macd));
        //System.out.println("outout2 " + Arrays.toString(sig));
        //System.out.println("outout3 " + Arrays.toString(hist));
        //log.info("mac beg end " + beg.value + " " + end.value + Arrays.toString(macd));
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
        //log.info("in " + );
        log.info("beg end " + beg.value + " " + end.value);
        log.info("macd " + Arrays.toString(macd));
        log.info("sign " + Arrays.toString(sig));
        log.info("hist " + Arrays.toString(hist));
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

	public Double[] getCCI(List<Double> low, List<Double> high, List<Double> close, int days, boolean wantdelta, int deltadays) {
    	int retsize = 1;
		if (wantdelta) {
    		retsize++;
    	}
		Double[] retValues = new Double[retsize];
		double lowArr[] = new double[days];
		double highArr[] = new double[days];
		double closeArr[] = new double[days];
	    int size = ArraysUtil.getArrayNonNullReverse(low, lowArr);
	    ArraysUtil.getArrayNonNullReverse(high, highArr);
	    ArraysUtil.getArrayNonNullReverse(close, closeArr);
	    Object[] objs = getInnerCCI(lowArr, highArr, closeArr, size);
	    // TODO works here too?
	    retValues[0] = getRSI(objs);
        if (wantdelta) {
        	// TODO check works for this too?
        	retValues[1] = getRSIdelta(objs, deltadays);
        }
        return retValues;
	}

    public Double[] getATR(List<Double> low, List<Double> high, List<Double> close, int days, boolean wantdelta, int deltadays) {
        int retsize = 1;
        if (wantdelta) {
            retsize++;
        }
        Double[] retValues = new Double[retsize];
        double lowArr[] = new double[days];
        double highArr[] = new double[days];
        double closeArr[] = new double[days];
        int size = ArraysUtil.getArrayNonNullReverse(low, lowArr);
        ArraysUtil.getArrayNonNullReverse(high, highArr);
        ArraysUtil.getArrayNonNullReverse(close, closeArr);
        Object[] objs = getInnerATR(lowArr, highArr, closeArr, size);
        // TODO works here too?
        retValues[0] = getRSI(objs);
        if (wantdelta) {
            // TODO check works for this too?
            retValues[1] = getRSIdelta(objs, deltadays);
        }
        return retValues;
    }

    public Double[] getSTOCH(List<Double> low, List<Double> high, List<Double> close, int days, boolean wantdelta, int deltadays) {
        int retsize = 1;
        if (wantdelta) {
            retsize++;
        }
        Double[] retValues = new Double[retsize];
        double lowArr[] = new double[days];
        double highArr[] = new double[days];
        double closeArr[] = new double[days];
        int size = ArraysUtil.getArrayNonNullReverse(low, lowArr);
        ArraysUtil.getArrayNonNullReverse(high, highArr);
        ArraysUtil.getArrayNonNullReverse(close, closeArr);
        Object[] objs = getInnerSTOCH(lowArr, highArr, closeArr, size);
        // TODO works here too?
        retValues[0] = getRSI(objs);
        if (wantdelta) {
            // TODO check works for this too?
            retValues[1] = getRSIdelta(objs, deltadays);
        }
        return retValues;
    }

	public Double[] getRSI(List<Double> list, int days, boolean wantdelta, int deltadays) {
    	int retsize = 1;
		if (wantdelta) {
    		retsize++;
    	}
		Double[] retValues = new Double[retsize];
		double values[] = new double[days];
	    int size = ArraysUtil.getArrayNonNullReverse(list, values);
	    Object[] objs = getInnerRSI(values, size);
        retValues[0] = getRSI(objs);
        if (wantdelta) {
        	retValues[1] = getRSIdelta(objs, deltadays);
        }
        return retValues;
	}

	public Double[] getSTOCHRSI(List<Double> list, int days, boolean wantdelta, int deltadays) {
    	int retsize = 2;
		if (wantdelta) {
    		retsize += 2;
    	}
		Double[] retValues = new Double[retsize];
		double values[] = new double[days];
	    int size = ArraysUtil.getArrayNonNullReverse(list, values);
	    Object[] objs = getInnerSTOCHRSI(values, size);
        retValues[0] = getArr(objs, 0, 3);
        retValues[1] = getArr(objs, 1, 3);
        if (wantdelta) {
        	retValues[2] = getArrDelta(objs, 0, 3, deltadays);
        	retValues[3] = getArrDelta(objs, 1, 3, deltadays);
        }
        return retValues;
	}

	private double getRSI(Object[] objs) {
		double rsi[] = (double[]) objs[0];
        MInteger beg = (MInteger) objs[1];
        MInteger end = (MInteger) objs[2];
        if (end.value == 0) {
            return 0;
        }
        return rsi[end.value - 1];
	}

    private double getArr(Object[] objs, int arrInd, int endInd) {
        double arr[] = (double[]) objs[arrInd];
        MInteger end = (MInteger) objs[endInd];
        if (end.value == 0) {
            return 0;
        }
        return arr[end.value - 1];
    }

    private double getArrDelta(Object[] objs, int arrInd, int endInd, int deltadays) {
        double rsi[] = (double[]) objs[arrInd];
        MInteger end = (MInteger) objs[endInd];
        if (end.value == 0) {
            return 0;
        }
        double delta = 0;
        int min = Math.max(0, end.value - deltadays);
        delta = rsi[end.value - 1] - rsi[min];
        return delta/(deltadays - 1);
    }

	private double getRSIdelta(Object[] objs, int deltadays) {
		double rsi[] = (double[]) objs[0];
        MInteger end = (MInteger) objs[2];
        if (end.value == 0) {
            return 0;
        }
        double delta = 0;
        int min = Math.max(0, end.value - deltadays);
        delta = rsi[end.value - 1] - rsi[min];
        return delta/(deltadays - 1);
	}

	public double getMomderiv(List<Double> list, int days, int deltadays) {
		double values[] = new double[days];
	    int size = ArraysUtil.getArrayNonNullReverse(list, values);
	    Object[] objs = getInnerMACD(values, size);
        return getHistogramDelta(objs, deltadays);
	}

	private double getHistogramDelta( Object[] objs, int deltadays) {
		double hist[] = (double[]) objs[2];
        MInteger end = (MInteger) objs[4];
        if (end.value == 0) {
            return 0;
        }
        double delta = 0;
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
        int min = Math.max(0, end.value - deltadays);
        delta = hist[end.value - 1] - hist[min];
        return delta/(deltadays - 1);
	}
	
	private double getMomentumDelta( Object[] objs, int deltadays) {
		double macd[] = (double[]) objs[0];
        MInteger end = (MInteger) objs[4];
        if (end.value == 0) {
            return 0;
        }
        double delta = 0;
        int min = Math.max(0, end.value - deltadays);
        delta = macd[end.value - 1] - macd[min];
        return delta/(deltadays - 1);
	}
	
	public int getMomAndDelta(List<Double> list, int days, boolean wantmacddelta, int macddeltadays, boolean wanthistdelta, int histdeltadays, Object[] retValues) {
	    Object[] objs = getMomAndDeltaFull(list, days, macddeltadays, histdeltadays);
        return getMomAndDelta(wantmacddelta, wanthistdelta, (Double[]) objs, retValues);
	}

    public Double[] getMomAndDelta(int macddeltadays, int histdeltadays, Object[] objs) {
        int retindex = 0;
        Double[] retValues = new Double[4];
        retValues[retindex++] = getHist(objs);
        retValues[retindex++] = getHistogramDelta(objs, histdeltadays);
        retValues[retindex++] = getMom(objs);
        retValues[retindex++] = getMomentumDelta(objs, macddeltadays);
        return retValues;
    }

    public int getMomAndDelta(boolean wantmacddelta, boolean wanthistdelta, Double[] objs, Object[] retValues) {
        int retindex = 0;
        retValues[retindex++] = objs[0];
        if (wanthistdelta) {
            retValues[retindex++] = objs[1];
        }
        retValues[retindex++] = objs[2];
        if (wantmacddelta) {
            retValues[retindex++] = objs[3];
        }
        return retindex;
    }

    public Object[] getMomAndDeltaFull(List<Double> list, int days, int macddeltadays, int histdeltadays) {
        double values[] = new double[days];
        log.info("before before " + list.size() + " " + list);
        int size = ArraysUtil.getArrayNonNullReverse(list, values);
        log.info("before size " + size + Arrays.toString(values));
        Object[] objs = getInnerMACD(values, size);
        return objs;
    }

    private double getMom(Object[] objs) {
        double macd[] = (double[]) objs[0];
        MInteger end = (MInteger) objs[4];
        if (end.value == 0) {
            return 0;
        }
        return macd[end.value - 1];
    }
    
	private double getHist(Object[] objs) {
		double hist[] = (double[]) objs[2];
        MInteger end = (MInteger) objs[4];
        if (end.value == 0) {
            return 0;
        }
        return hist[end.value - 1];
	}
}
