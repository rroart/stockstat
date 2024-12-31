package roart.talib.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jfree.data.category.DefaultCategoryDataset;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;

import roart.common.pipeline.data.SerialTA;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.stockutil.StockUtil;
import roart.talib.Talib;
import roart.talib.util.TaConstants;
import roart.talib.util.TaUtil;

public class TalibCCI extends Talib {

    @Override
    protected SerialTA getInner(double[][] arrarr, int size) {
	double[] close = arrarr[0];
        double[] low = arrarr[1];
	double[] high = arrarr[2];
        Core core = new Core();
        MInteger beg = new MInteger();
        MInteger end = new MInteger();
        double[] rsi = new double[close.length];
        core.cci (0, size - 1, low, high, close, 14, beg, end, rsi);
        Integer[] objs = new Integer[3];
        double[][] objsarr = new double[3][];
        objsarr[0] = rsi;
        objs[1] = beg.value;
        objs[2] = end.value;
        return new SerialTA(objs, objsarr, beg.value, end.value);
    }

    @Override
    protected String[] getTitles() {
	return new String[] { "cci" };
    }

    @Override
    protected int[][] getArrayMeta() {
	return TaConstants.ONE;
    }

    @Override
    protected int getInputArrays() {
	return 3;
    }
}
