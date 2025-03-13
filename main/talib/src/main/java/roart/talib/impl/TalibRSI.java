package roart.talib.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jfree.data.category.DefaultCategoryDataset;

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

public class TalibRSI extends Talib {

    public static final int RSIIDXRSI = 0;
    public static final int RSIIDXBEG = 1;
    public static final int RSIIDXEND = 2;
    public static final int RSIIDXRSIFIXED = 3;

    @Override
    protected SerialTA getInner(double[][] arrarr, int size) {
        double[] values = arrarr[0];
        Core core = new Core();
        MInteger beg = new MInteger();
        MInteger end = new MInteger();
        double[] rsi = new double[values.length];
        log.debug("lookback {}", core.rsiLookback(14));
        core.rsi(0, size - 1, values, 14, beg, end, rsi);
        Integer[] objs = new Integer[4];
        double[][] objsarr = new double[4][];
        objsarr[RSIIDXRSI] = rsi;
        objs[RSIIDXBEG] = beg.value;
        objs[RSIIDXEND] = end.value;
        objsarr[RSIIDXRSIFIXED] = ArraysUtil.makeFixed(rsi, beg.value, end.value, values.length);
        log.debug("rsi beg end {} {} {}", beg.value, end.value, Arrays.toString(rsi));
        return new SerialTA(objs, objsarr, beg.value, end.value);
    }

    @Override
    protected String[] getTitles() {
	return new String[] { "rsi" };
    }

    @Override
    protected int[][] getArrayMeta() {
	return TaConstants.ONE;
    }

    @Override
    public int getInputArrays() {
	return 1;
    }
}
