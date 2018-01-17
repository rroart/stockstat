package roart.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;

import roart.model.StockItem;

public class TaUtil {

    private static Logger log = LoggerFactory.getLogger(TaUtil.class);

    public static final int MACDIDXMACD = 0;
    public static final int MACDIDXSIGN = 1;
    public static final int MACDIDXHIST = 2;
    public static final int MACDIDXBEG = 3;
    public static final int MACDIDXEND = 4;
    public static final int MACDIDXMACDFIXED = 5;
    public static final int MACDIDXSIGFIXED = 6;
    public static final int MACDIDXHISTFIXED = 7;

    public static final int RSIIDXRSI = 0;
    public static final int RSIIDXBEG = 1;
    public static final int RSIIDXEND = 2;
    public static final int RSIIDXRSIFIXED = 3;

    public static final int ONEIDXARRONE = 0;
    public static final int ONEIDXBEG = 1;
    public static final int ONEIDXEND = 2;
    public static final int ONEIDXARRONEFIXED = 3;

    public static final int TWOIDXARRONE = 0;
    public static final int TWOIDXARRTWO = 1;
    public static final int TWOIDXBEG = 2;
    public static final int TWOIDXEND = 3;
    public static final int TWOIDXARRONEFIXED = 4;
    public static final int TWOIDXARRTWOFIXED = 5;

    public static final int THREEIDXARRONE = 0;
    public static final int THREEIDXARRTWO = 1;
    public static final int THREEIDXARRTHREE = 2;
    public static final int THREEIDXBEG = 3;
    public static final int THREEIDXEND = 4;
    public static final int THREEIDXARRONEFIXED = 5;
    public static final int THREEIDXARRTWOFIXED = 6;
    public static final int THREEIDXARRTHREEFIXED = 7;

    // TODO make OO version of this, when getting another stat lib

    Object[] getRSI(int days, String market,
            String id, Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        MarketData marketdata = marketdatamap.get(market);
        Integer periodInt = StockUtil.getPeriodByMarket(market, pairs);
        List<StockItem>[] datedstocklists = marketdata.datedstocklists;
        double[] values = new double[days];
        int size = getArr(days, market, id, ids, periodInt, datedstocklists, values, null, null);
        return getInnerRSI(values, size);
    }

    public Object[] getRsiAndDeltaFull(double[] values, int days, int rsideltadays) {
        return getInnerRSI(values, values.length);
    }

    Object[] getSTOCHRSI(int days, String market,
            String id, Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        MarketData marketdata = marketdatamap.get(market);
        Integer periodInt = StockUtil.getPeriodByMarket(market, pairs);
        List<StockItem>[] datedstocklists = marketdata.datedstocklists;
        double[] values = new double[days];
        int size = getArr(days, market, id, ids, periodInt, datedstocklists, values, null, null);
        return getInnerSTOCHRSI(values, size);
    }

    Object[] getCCI(int days, String market,
            String id, Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        MarketData marketdata = marketdatamap.get(market);
        Integer periodInt = StockUtil.getPeriodByMarket(market, pairs);
        List<StockItem>[] datedstocklists = marketdata.datedstocklists;
        double[] low = new double[days];
        double[] high = new double[days];
        double[] close = new double[days];
        // TODO make new getarr for low high
        int size = getArr(days, market, id, ids, periodInt, datedstocklists, close, low, high);
        return getInnerCCI(low, high, close, size);
    }

    Object[] getATR(int days, String market,
            String id, Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        MarketData marketdata = marketdatamap.get(market);
        Integer periodInt = StockUtil.getPeriodByMarket(market, pairs);
        List<StockItem>[] datedstocklists = marketdata.datedstocklists;
        double[] low = new double[days];
        double[] high = new double[days];
        double[] close = new double[days];
        // TODO make new getarr for low high
        int size = getArr(days, market, id, ids, periodInt, datedstocklists, close, low, high);
        return getInnerATR(low, high, close, size);
    }

    Object[] getSTOCH(int days, String market,
            String id, Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        MarketData marketdata = marketdatamap.get(market);
        Integer periodInt = StockUtil.getPeriodByMarket(market, pairs);
        List<StockItem>[] datedstocklists = marketdata.datedstocklists;
        double[] low = new double[days];
        double[] high = new double[days];
        double[] close = new double[days];
        // TODO make new getarr for low high
        int size = getArr(days, market, id, ids, periodInt, datedstocklists, close, low, high);
        return getInnerSTOCH(low, high, close, size);
    }

    @Deprecated
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
                        Double value = StockDao.getMainValue(stock, period);
                        if (value == null) {
                            continue;
                        }
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

    Object[] getRSIorig(int days, String market, String id,
            Set<Pair> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        MarketData marketdata = marketdatamap.get(market);
        Integer periodInt = StockUtil.getPeriodByMarket(market, pairs);
        List<StockItem>[] datedstocklists = marketdata.datedstocklists;
        double[] values = new double[days];
        int size = getArrForOrig(days, market, ids, periodInt, datedstocklists, values);
        return getInnerRSI(values, size);
    }

    private Object[] getInnerRSI(double[] values, int size) {
        Core core = new Core();
        MInteger beg = new MInteger();
        MInteger end = new MInteger();
        double[] rsi = new double[values.length];
        core.rsi(0, size - 1, values, 14, beg, end, rsi);
        Object[] objs = new Object[4];
        objs[RSIIDXRSI] = rsi;
        objs[RSIIDXBEG] = beg;
        objs[RSIIDXEND] = end;
        objs[RSIIDXRSIFIXED] = ArraysUtil.makeFixed(rsi, beg.value, end.value, values.length);
        log.info("rsi beg end {} {} {}", beg.value, end.value, Arrays.toString(rsi));
        return objs;
    }

    private Object[] getInnerCCI(double[] low, double[] high, double[] close, int size) {
        Core core = new Core();
        MInteger beg = new MInteger();
        MInteger end = new MInteger();
        double[] rsi = new double[close.length];
        core.cci (0, size - 1, low, high, close, 14, beg, end, rsi);
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
        double[] rsi = new double[close.length];
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
        double[] rsi = new double[close.length];
        double[] rsi2 = new double[close.length];
        MAType optInFastD;
        optInFastD = MAType.Sma;
        core.stochF(0, size - 1, high, low, close, /*14, */ 5, 3, optInFastD, beg, end, rsi, rsi2);
        Object[] objs = new Object[4];
        objs[0] = rsi;
        objs[1] = rsi2;
        objs[2] = beg;
        objs[3] = end;
        return objs;
    }

    private Object[] getInnerSTOCHRSI(double[] values, int size) {
        Core core = new Core();
        MInteger beg = new MInteger();
        MInteger end = new MInteger();
        double[] rsi = new double[values.length];
        double[] rsi2 = new double[values.length];
        MAType optInFastD;
        optInFastD = MAType.Sma;
        core.stochRsi(0, size - 1, values, 14, 5, 3, optInFastD, beg, end, rsi, rsi2);
        Object[] objs = new Object[4];
        objs[0] = rsi;
        objs[1] = rsi2;
        objs[2] = beg;
        objs[3] = end;
        return objs;
    }

    public double getRSI2(int days, String market, String id,
            Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        Object[] objs = getRSI(days, market, id, ids, marketdatamap, perioddata, periodstr);
        double[] rsi = (double[]) objs[0];
        MInteger beg = (MInteger) objs[1];
        MInteger end = (MInteger) objs[2];
        if (end.value == 0) {
            return 0;
        }
        return rsi[end.value - 1];
    }

    public DefaultCategoryDataset getRSIChart(int days, String market, String id,
            Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        Object[] objs = getRSI(days, market, id, ids, marketdatamap, perioddata, periodstr);
        double[] rsi = (double[]) objs[0];
        MInteger beg = (MInteger) objs[1];
        MInteger end = (MInteger) objs[2];
        int size = beg.value + end.value;
        size--;
        for (int i = 0; i < beg.value; i++) {
            dataset.addValue(0, "rsi" , Integer.valueOf(i - size));
        }
        for (int i = 0; i < end.value; i++) {
            dataset.addValue(rsi[i], "rsi" , Integer.valueOf(i + beg.value - size));
        }
        return dataset;
    }

    public DefaultCategoryDataset getSTOCHRSIChart(int days, String market, String id,
            Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        Object[] objs = getSTOCHRSI(days, market, id, ids, marketdatamap, perioddata, periodstr);
        double[] rsi = (double[]) objs[0];
        double[] rsi2 = (double[]) objs[1];
        MInteger beg = (MInteger) objs[2];
        MInteger end = (MInteger) objs[3];
        int size = beg.value + end.value;
        size--;
        for (int i = 0; i < beg.value; i++) {
            dataset.addValue(0, "srsi" , Integer.valueOf(i - size));
            dataset.addValue(0, "srsi2" , Integer.valueOf(i - size));
        }
        for (int i = 0; i < end.value; i++) {
            dataset.addValue(rsi[i], "srsi" , Integer.valueOf(i + beg.value - size));
            dataset.addValue(rsi2[i], "srsi2" , Integer.valueOf(i + beg.value - size));
        }
        return dataset;
    }

    public DefaultCategoryDataset getCCIChart(int days, String market, String id,
            Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        Object[] objs = getCCI(days, market, id, ids, marketdatamap, perioddata, periodstr);
        double[] cci = (double[]) objs[0];
        MInteger beg = (MInteger) objs[1];
        MInteger end = (MInteger) objs[2];
        int size = beg.value + end.value;
        size--;
        for (int i = 0; i < beg.value; i++) {
            dataset.addValue(0, "cci" , Integer.valueOf(i - size));
        }
        for (int i = 0; i < end.value; i++) {
            dataset.addValue(cci[i], "cci" , Integer.valueOf(i + beg.value - size));
        }
        return dataset;
    }

    public DefaultCategoryDataset getATRChart(int days, String market, String id,
            Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        Object[] objs = getATR(days, market, id, ids, marketdatamap, perioddata, periodstr);
        double[] atr = (double[]) objs[0];
        MInteger beg = (MInteger) objs[1];
        MInteger end = (MInteger) objs[2];
        int size = beg.value + end.value;
        size--;
        for (int i = 0; i < beg.value; i++) {
            dataset.addValue(0, "atr" , Integer.valueOf(i - size));
        }
        for (int i = 0; i < end.value; i++) {
            dataset.addValue(atr[i], "atr" , Integer.valueOf(i + beg.value - size));
        }
        return dataset;
    }

    public DefaultCategoryDataset getSTOCHChart(int days, String market, String id,
            Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        Object[] objs = getSTOCH(days, market, id, ids, marketdatamap, perioddata, periodstr);
        double[] stoch = (double[]) objs[0];
        double[] stoch2 = (double[]) objs[1];
        MInteger beg = (MInteger) objs[2];
        MInteger end = (MInteger) objs[3];
        int size = beg.value + end.value;
        size--;
        for (int i = 0; i < beg.value; i++) {
            dataset.addValue(0, "stoch" , Integer.valueOf(i - size));
            dataset.addValue(0, "stoch2" , Integer.valueOf(i - size));
        }
        for (int i = 0; i < end.value; i++) {
            dataset.addValue(stoch[i], "stoch" , Integer.valueOf(i + beg.value - size));
            dataset.addValue(stoch2[i], "stoch2" , Integer.valueOf(i + beg.value - size));
        }
        return dataset;
    }

    Object[] getMACD(int days, String market,
            String id, Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        MarketData marketdata = marketdatamap.get(market);
        Integer periodInt = StockUtil.getPeriodByMarket(market, pairs);
        List<StockItem>[] datedstocklists = marketdata.datedstocklists;
        double[] values = new double[days];
        int size = getArr(days, market, id, ids, periodInt, datedstocklists, values, null, null);
        return getInnerMACD(values, size);
    }

    private int getArr(int days, String market, String id, Set<Pair<String, String>> ids, Integer periodInt,
            List<StockItem>[] datedstocklists, double[] values, double[] low, double[] high) {
        int size = 0;
        boolean display = false;
        int count = 0;
        int downcount = Math.min(days, datedstocklists.length);
        List<Double> listd = new ArrayList<>();
        List<Double> liste = new ArrayList<>();
        List<Double> listf = new ArrayList<>();
        for (int j = 0; j < datedstocklists.length  && downcount > 0 ; j++) {
            List<StockItem> list = datedstocklists[j];
            if (list == null) {
                log.info("listnull {} {}", market, j);
                continue;
            }
            if (periodInt == null) {
                continue;
            }
            int period = periodInt;
            grr:  for (int i = 0; i < list.size(); i++) {
                StockItem stock = list.get(i);
                //System.out.print(" " + stock.getId());
                Pair<String, String> pair = new Pair(market, stock.getId());
                if (ids.contains(pair)) {
                    try {
                        Double[] allValues = StockDao.getValue(stock, period);
                        Double value = allValues[0];
                        if (value == null) {
                            display = true;
                            continue;
                        }
                        Double alow = null;
                        Double ahigh = null;
                        if (allValues.length > 1) {
                            alow = allValues[1];
                            ahigh = allValues[2];
                        }
                        double val = value;
                        values[count] = value;
                        if (low != null) {
                            low[count] = alow;
                        }
                        if (high != null) {
                            high[count] = ahigh;
                        }
                        listd.add(val);
                        liste.add(alow);
                        listf.add(ahigh);
                        count++;
                        downcount--;
                        size++;
                        break grr;
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);
                    }
                }
            }    
        }
        Collections.reverse(listd);
        Collections.reverse(liste);
        Collections.reverse(listf);
        Object[] newarr = listd.toArray();
        Object[] newars = liste.toArray();
        Object[] newart = listf.toArray();
        log.info("arrarr {} {} {} {}", newarr, values, size, newarr.length);
        for (int i = 0; i < size; i ++) {
            values[i] = (double) newarr[i];
        }
        if (low != null) {
            for (int i = 0; i < size; i ++) {
                low[i] = (double) newars[i];
            }
        }
        if (high != null) {
            for (int i = 0; i < size; i ++) {
                high[i] = (double) newart[i];
            }
        }
        //System.arraycopy(newarr, 0, values, 0, size);
        //System.out.println("thearr " + Arrays.toString(values));
        if (display) {
            //log.info("mydisplay " + list);
        }
        return size;
    }

    @Deprecated
    Object[] getMACD_orig(int days, String market, String id,
            Set<Pair> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        MarketData marketdata = marketdatamap.get(market);
        Integer periodInt = StockUtil.getPeriodByMarket(market, pairs);
        List<StockItem>[] datedstocklists = marketdata.datedstocklists;
        double[] values = new double[days];
        int size = getArrForOrig(days, market, ids, periodInt, datedstocklists, values);
        return getInnerMACD(values, size);
    }

    @Deprecated
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
                        Double value = StockDao.getMainPeriod(stock, period);
                        if (value == null) {
                            continue;
                        }
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
        double[] ema26 = new double[values.length];
        double[] ema12 = new double[values.length];
        double[] hist = new double[values.length];
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
        double[] macd = new double[values.length];
        double[] sig = new double[values.length];
        double[] hist = new double[values.length];
        core.macd(0, size - 1, values, 26, 12, 9, beg, end, macd, sig, hist);
        Object[] objs = new Object[8];
        objs[MACDIDXMACD] = macd;
        objs[MACDIDXSIGN] = sig;
        objs[MACDIDXHIST] = hist;
        objs[MACDIDXBEG] = beg;
        objs[MACDIDXEND] = end;
        objs[MACDIDXMACDFIXED] = ArraysUtil.makeFixed(macd, beg.value, end.value, values.length);
        objs[MACDIDXSIGFIXED] = ArraysUtil.makeFixed(sig, beg.value, end.value, values.length);
        objs[MACDIDXHISTFIXED] = ArraysUtil.makeFixed(hist, beg.value, end.value, values.length);
        return objs;
    }

    public double getMom(int days, String market,
            String id, Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        Object[] objs = getMACD(days, market, id, ids, marketdatamap, perioddata, periodstr);
        double[] hist = (double[]) objs[MACDIDXHIST];
        MInteger beg = (MInteger) objs[MACDIDXBEG];
        MInteger end = (MInteger) objs[MACDIDXEND];
        if (end.value == 0) {
            return 0;
        }
        return hist[end.value - 1];
    }

    public DefaultCategoryDataset getMACDChart(int days, String market,
            String id, Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        Object[] objs = getMACD(days, market, id, ids, marketdatamap, perioddata, periodstr);
        double[] macd = (double[]) objs[MACDIDXMACD];
        double[] sig = (double[]) objs[MACDIDXSIGN];
        double[] hist = (double[]) objs[MACDIDXHIST];
        MInteger beg = (MInteger) objs[MACDIDXBEG];
        MInteger end = (MInteger) objs[MACDIDXEND];
        int size = beg.value + end.value;
        size--;
        log.info("beg end {} {}", beg.value, end.value);
        String macdString = Arrays.toString(macd);
        String sigString = Arrays.toString(sig);
        String histString = Arrays.toString(hist);
        log.info("macd {}", macdString);
        log.info("sign {}", sigString);
        log.info("hist {}", histString);
        for (int i = 0; i < beg.value; i++) {
            dataset.addValue(0, "macd" , Integer.valueOf(i - size));
            dataset.addValue(0, "sig" , Integer.valueOf(i - size));
            dataset.addValue(0, "hist" , Integer.valueOf(i - size));
        }
        for (int i = 0; i < end.value; i++) {
            dataset.addValue(macd[i], "macd" , Integer.valueOf(i + beg.value - size));
            dataset.addValue(sig[i], "sig" , Integer.valueOf(i + beg.value - size));
            dataset.addValue(hist[i], "hist" , Integer.valueOf(i + beg.value - size));
        }
        return dataset;
    }

    public Object[] getCCI(double[] low, double[] high, double[] close, int days, boolean wantdelta, int deltadays) {
        return getInnerCCI(low, high, close, close.length);
    }

    public Object[] getATR(double[] low, double[] high, double[] close, int days, boolean wantdelta, int deltadays) {
        return getInnerATR(low, high, close, close.length);
    }

    public Object[] getSTOCH(double[] low, double[] high, double[] close, int days, boolean wantdelta, int deltadays) {
        return getInnerSTOCH(low, high, close, close.length);
    }

    public Double[] getRSI(List<Double> list, int days, boolean wantdelta, int deltadays) {
        int retsize = 1;
        if (wantdelta) {
            retsize++;
        }
        Double[] retValues = new Double[retsize];
        double[] values = new double[days];
        int size = ArraysUtil.getArrayNonNullReverse(list, values);
        Object[] objs = getInnerRSI(values, size);
        retValues[0] = getRSI(objs);
        if (wantdelta) {
            retValues[1] = getRSIdelta(objs, deltadays);
        }
        return retValues;
    }

    public Object[] getSTOCHRSI(double[][] list, int days, int deltadays) {
        return getInnerSTOCHRSI(list[0], list[0].length);
    }

    private double getRSI(Object[] objs) {
        double[] rsi = (double[]) objs[0];
        MInteger beg = (MInteger) objs[1];
        MInteger end = (MInteger) objs[2];
        if (end.value == 0) {
            return 0;
        }
        return rsi[end.value - 1];
    }

    private double getArr(Object[] objs, int arrInd, int endInd) {
        double[] arr = (double[]) objs[arrInd];
        MInteger end = (MInteger) objs[endInd];
        if (end.value == 0) {
            return 0;
        }
        return arr[end.value - 1];
    }

    private double getArrDelta(Object[] objs, int arrInd, int endInd, int deltadays) {
        double[] rsi = (double[]) objs[arrInd];
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
        double[] rsi = (double[]) objs[0];
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
        double[] values = new double[days];
        int size = ArraysUtil.getArrayNonNullReverse(list, values);
        Object[] objs = getInnerMACD(values, size);
        return getHistogramDelta(objs, deltadays, 0);
    }

    private Double getHistogramDelta( Object[] objs, int deltadays, int offset) {
        double[] hist = (double[]) objs[MACDIDXHIST];
        MInteger end = (MInteger) objs[MACDIDXEND];
        if (end.value == 0) {
            return null;
        }
        double delta = 0;
        int min = Math.max(0, end.value - offset - deltadays);
        delta = hist[end.value - offset - 1] - hist[min];
        return delta/(deltadays - 1);
    }

    private Double getRsiDelta( Object[] objs, int deltadays, int offset) {
        double[] rsi = (double[]) objs[RSIIDXRSI];
        MInteger end = (MInteger) objs[RSIIDXEND];
        if (end.value == 0) {
            return null;
        }
        double delta = 0;
        int min = Math.max(0, end.value - offset - deltadays);
        delta = rsi[end.value - offset - 1] - rsi[min];
        return delta/(deltadays - 1);
    }

    private Double getMomentumDelta( Object[] objs, int deltadays, int offset ) {
        double[] macd = (double[]) objs[MACDIDXMACD];
        MInteger end = (MInteger) objs[MACDIDXEND];
        if (end.value == 0) {
            return null;
        }
        double delta = 0;
        int min = Math.max(0, end.value - offset - deltadays);
        delta = macd[end.value - offset - 1] - macd[min];
        return delta / (deltadays - 1);
    }

    public Double[] getMomAndDelta(int macddeltadays, int histdeltadays, Object[] objs, int offset) {
        MInteger end = (MInteger) objs[MACDIDXEND];
        if (end.value < offset + Math.max(macddeltadays, histdeltadays)) {
            return null;
        }
        int retindex = 0;
        Double[] retValues = new Double[4];
        retValues[retindex++] = getHist(objs, offset);
        retValues[retindex++] = getHistogramDelta(objs, histdeltadays, offset);
        retValues[retindex++] = getMom(objs, offset);
        retValues[retindex] = getMomentumDelta(objs, macddeltadays, offset);
        return retValues;
    }

    public Double[] getRsiAndDelta(int rsideltadays, Object[] objs, int offset) {
        MInteger end = (MInteger) objs[RSIIDXEND];
        if (end.value < offset + Math.max(rsideltadays, rsideltadays)) {
            return null;
        }
        int retindex = 0;
        Double[] retValues = new Double[2];
        retValues[retindex++] = getRsi(objs, offset);
        retValues[retindex] = getRsiDelta(objs, rsideltadays, offset);
        return retValues;
    }

    public Double[] getSRSIAndDelta(int onedeltadays, int twodeltadays, Object[] objs, int offset) {
        MInteger end = (MInteger) objs[TWOIDXEND];
        if (end.value < offset + Math.max(onedeltadays, twodeltadays)) {
            return null;
        }
        int retindex = 0;
        Double[] retValues = new Double[4];
        retValues[retindex++] = getArrayValueAtOffset(objs, TWOIDXARRONE, TWOIDXEND, offset);
        retValues[retindex++] = getArrayValueAtOffsetDelta(objs, TWOIDXARRONE, TWOIDXEND, twodeltadays, offset);
        retValues[retindex++] = getArrayValueAtOffset(objs, TWOIDXARRTWO, TWOIDXEND, offset);
        retValues[retindex] = getArrayValueAtOffsetDelta(objs, TWOIDXARRTWO, TWOIDXEND, twodeltadays, offset);
        return retValues;
    }

    public Double[] getRsiAndDelta(int rsideltadays, Object[] objs) {
        return getRsiAndDelta(rsideltadays, objs, 0);
    }

    public Double[] getMomAndDelta(int macddeltadays, int histdeltadays, Object[] objs) {
        return getMomAndDelta(macddeltadays, histdeltadays, objs, 0);
    }

    public int getMomAndDelta(boolean wantmacddelta, boolean wanthistdelta, Double[] objs, Object[] retValues) {
        if (objs == null) {
            int retindex = 2;
            if (wanthistdelta) {
                retindex++;
            }
            if (wantmacddelta) {
                retindex++;
            }
            return retindex;
        }
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

    public Double[] getSRSIAndDelta(int onedeltadays, int twodeltadays, Object[] objs) {
        return getSRSIAndDelta(onedeltadays, twodeltadays, objs, 0);
    }

    public int getSRSIAndDelta(boolean wantonedelta, boolean wanttwodelta, Double[] objs, Object[] retValues) {
        if (objs == null) {
            int retindex = 2;
            if (wanttwodelta) {
                retindex++;
            }
            if (wantonedelta) {
                retindex++;
            }
            return retindex;
        }
        int retindex = 0;
        retValues[retindex++] = objs[0];
        if (wanttwodelta) {
            retValues[retindex++] = objs[1];
        }
        retValues[retindex++] = objs[2];
        if (wantonedelta) {
            retValues[retindex++] = objs[3];
        }
        return retindex;
    }

    public int getRSIAndDelta(boolean wantrsidelta, Double[] objs, Object[] retValues) {
        if (objs == null) {
            int retindex = 1;
            if (wantrsidelta) {
                retindex++;
            }
            return retindex;
        }
        int retindex = 0;
        retValues[retindex++] = objs[0];
        if (wantrsidelta) {
            retValues[retindex++] = objs[1];
        }
        return retindex;
    }

    public Object[] getMomAndDeltaFull(double[] values, int days, int macddeltadays, int histdeltadays) {
        return getInnerMACD(values, values.length);
    }

    private Double getMom(Object[] objs, int offset) {
        double[] macd = (double[]) objs[MACDIDXMACD];
        MInteger end = (MInteger) objs[MACDIDXEND];
        if (end.value == 0) {
            return null;
        }
        return macd[end.value - offset - 1];
    }

    private Double getHist(Object[] objs, int offset) {
        double[] hist = (double[]) objs[MACDIDXHIST];
        MInteger end = (MInteger) objs[MACDIDXEND];
        if (end.value == 0) {
            return null;
        }
        return hist[end.value - offset - 1];
    }

    private Double getRsi(Object[] objs, int offset) {
        double[] rsi = (double[]) objs[RSIIDXRSI];
        MInteger end = (MInteger) objs[RSIIDXEND];
        if (end.value == 0) {
            return null;
        }
        return rsi[end.value - offset - 1];
    }

    private Double getArrayValueAtOffset(Object[] objs, int arrayindex, int endvalueindex, int offset) {
        double[] hist = (double[]) objs[arrayindex];
        MInteger end = (MInteger) objs[endvalueindex];
        if (end.value == 0) {
            return null;
        }
        return hist[end.value - offset - 1];
    }
    
    private Double getArrayValueAtOffsetDelta(Object[] objs, int arrayindex, int endvalueindex, int deltadays, int offset) {
        double[] hist = (double[]) objs[arrayindex];
        MInteger end = (MInteger) objs[endvalueindex];
        if (end.value == 0) {
            return null;
        }
        double delta = 0;
        int min = Math.max(0, end.value - offset - deltadays);
        delta = hist[end.value - offset - 1] - hist[min];
        return delta/(deltadays - 1);
    }


}
