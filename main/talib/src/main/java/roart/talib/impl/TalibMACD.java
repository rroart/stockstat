package roart.talib.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jfree.data.category.DefaultCategoryDataset;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;

import roart.common.pipeline.data.SerialTA;
import roart.common.util.ArraysUtil;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.stockutil.StockUtil;
import roart.talib.Talib;
import roart.talib.util.TaConstants;
import roart.talib.util.TaUtil;

public class TalibMACD extends Talib {

    public static final int MACDIDXHIST = 0;
    public static final int MACDIDXMACD = 1;
    public static final int MACDIDXSIGN = 2;
    public static final int MACDIDXBEG = 3;
    public static final int MACDIDXEND = 4;
    private static final int MACDIDXMACDFIXED = 5;
    private static final int MACDIDXSIGFIXED = 6;
    private static final int MACDIDXHISTFIXED = 7;

    @Override
    protected SerialTA getInner(double[][] arrarr, int size) {
	double[] values = arrarr[0];
        long time0 = System.currentTimeMillis();
        //values = ArraysUtil.getPercentizedPriceIndex(values);
        Core core = new Core();
        MInteger beg = new MInteger();
        MInteger end = new MInteger();
        double[] macd = new double[values.length];
        double[] sig = new double[values.length];
        double[] hist = new double[values.length];
        log.debug("lookback {}", core.macdLookback(26, 12, 9));     
        core.macd(0, size - 1, values, 26, 12, 9, beg, end, macd, sig, hist);
        Integer[] objs = new Integer[8];
        double[][] objsarr = new double[8][];
        objsarr[MACDIDXMACD] = macd;
        objsarr[MACDIDXSIGN] = sig;
        objsarr[MACDIDXHIST] = hist;
        objs[MACDIDXBEG] = beg.value;
        objs[MACDIDXEND] = end.value;
        objsarr[MACDIDXMACDFIXED] = ArraysUtil.makeFixed(macd, beg.value, end.value, values.length);
        objsarr[MACDIDXSIGFIXED] = ArraysUtil.makeFixed(sig, beg.value, end.value, values.length);
        objsarr[MACDIDXHISTFIXED] = ArraysUtil.makeFixed(hist, beg.value, end.value, values.length);
        log.debug("timer {}", System.currentTimeMillis() - time0);
        return new SerialTA(objs, objsarr, beg.value, end.value);
    }

    @Override
    protected String[] getTitles() {
        return new String []{ "macd", "sig", "hist" };
    }
    
    @Override 
    protected int[][] getArrayMeta() {
        return TaConstants.THREE;
    }

    @Override
    public int getInputArrays() {
	return 1;
    }
}
