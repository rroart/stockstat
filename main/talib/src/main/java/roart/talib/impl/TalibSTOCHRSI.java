package roart.talib.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jfree.data.category.DefaultCategoryDataset;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;

import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.stockutil.StockUtil;
import roart.talib.Talib;
import roart.talib.util.TaConstants;
import roart.talib.util.TaUtil;

public class TalibSTOCHRSI extends Talib {
    @Override
    protected Object[] getInner(double[][] arrarr, int size) {
        double[] values = arrarr[0];
        Core core = new Core();
        MInteger beg = new MInteger();
        MInteger end = new MInteger();
        // fastk, buy 20 sell 80
        double[] rsi = new double[values.length];
        // fastd
        double[] rsi2 = new double[values.length];
        MAType optInFastD;
        optInFastD = MAType.Sma;
        core.stochRsi(0, size - 1, values, 14, 5, 3, optInFastD, beg, end, rsi, rsi2);
        Object[] objs = new Object[4];
        objs[0] = rsi;
        objs[1] = rsi2;
        objs[2] = beg.value;
        objs[3] = end.value;
        return objs;
    }

    @Override
    protected String[] getTitles() {
	return new String[] { "srsik", "srsid" };
    }

    @Override
    protected int[][] getArrayMeta() {
	return TaConstants.TWO;
    }

    @Override
    protected int getInputArrays() {
	return 1;
    }
}
