package roart.talib.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jfree.data.category.DefaultCategoryDataset;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;

import roart.common.constants.Constants;
import roart.common.pipeline.data.SerialTA;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.stockutil.StockUtil;
import roart.talib.Talib;
import roart.talib.util.TaConstants;
import roart.talib.util.TaUtil;

public class TalibSTOCH extends Talib {

    @Override
    protected SerialTA getInner(double[][] arrarr, int size) {
	double[] close = arrarr[0];
        double[] low = arrarr[1];
	double[] high = arrarr[2];
        Core core = new Core();
        MInteger beg = new MInteger();
        MInteger end = new MInteger();
        // slowk, use at 20 80
        double[] rsi = new double[close.length];
        // slowd
        double[] rsi2 = new double[close.length];
        MAType optInFastD;
        optInFastD = MAType.Sma;
        if (size > 0) {
            try {
                core.stochF(0, size - 1, high, low, close, /*14, */ 5, 3, optInFastD, beg, end, rsi, rsi2);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        Integer[] objs = new Integer[4];
        double[][] objsarr = new double[4][];
        objsarr[0] = rsi;
        objsarr[1] = rsi2;
        objs[2] = beg.value;
        objs[3] = end.value;
        return new SerialTA(objs, objsarr, beg.value, end.value);
    }

    @Override
    protected String[] getTitles() {
	return new String[] { "stochk", "stochd" };
    }

    @Override
    protected int[][] getArrayMeta() {
	return TaConstants.TWO;
    }

    @Override
    protected int getInputArrays() {
	return 3;
    }
}
