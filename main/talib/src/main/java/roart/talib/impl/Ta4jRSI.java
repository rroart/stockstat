package roart.talib.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jfree.data.category.DefaultCategoryDataset;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.StochasticRSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import roart.common.pipeline.data.SerialTA;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.stockutil.StockUtil;
import roart.talib.Ta4j;
import roart.talib.util.TaConstants;
import roart.talib.util.TaUtil;

public class Ta4jRSI extends Ta4j {
    @Override
    protected SerialTA getInner(double[][] arrarr, int size) {
        double[] values = arrarr[0];
        double[] rsi = new double[values.length];
        Integer[] objs = new Integer[4];
        double[][] objsarr = new double[4][];
        objsarr[0] = rsi;
        objs[1] = 0; // beg;
        objs[2] = size; // end;
        if (size == 0) {
            return new SerialTA(objs, objsarr, 0, size);
        }
        BarSeries series = getClosedSeries(values, size);
        ClosePriceIndicator c = new ClosePriceIndicator(series);
        System.out.println("sta " + c.getValue(0).doubleValue());
        RSIIndicator i = new RSIIndicator(new ClosePriceIndicator(series), 14);
        int beg = i.getUnstableBars();
        objs[1] = beg;
        objs[2] = size - beg;
        for (int j = 0; j < size - beg; j++) {
            rsi[j] = i.getValue(j + beg).doubleValue();
            if (Double.isNaN(rsi[j])) {
                int jj = 0;
            }
        }
        return new SerialTA(objs, objsarr, beg, size - beg);
    }

    @Override
    protected String[] getTitles() {
        return new String[] { "srsi" };
    }

    @Override
    protected int[][] getArrayMeta() {
        return TaConstants.ONE;
    }

    @Override
    protected int getInputArrays() {
	return 1;
    }
}
