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

import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.stockutil.StockUtil;
import roart.talib.Ta4j;
import roart.talib.util.TaConstants;
import roart.talib.util.TaUtil;

public class Ta4jRSI extends Ta4j {
    @Override
    protected Object[] getInner(double[][] arrarr, int size) {
        double[] values = arrarr[0];
        double[] rsi = new double[values.length];
        Object[] objs = new Object[4];
        objs[0] = rsi;
        objs[1] = 0; // beg;
        objs[2] = size; // end;
        if (size == 0) {
            return objs;
        }
        BarSeries series = getClosedSeries(values, size);
        RSIIndicator i = new RSIIndicator(new ClosePriceIndicator(series), 14);
        for (int j = 0; j < size; j++) {
            rsi[j] = i.getValue(j).doubleValue();
            if (Double.isNaN(rsi[j])) {
                int jj = 0;
            }
        }
        return objs;
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
