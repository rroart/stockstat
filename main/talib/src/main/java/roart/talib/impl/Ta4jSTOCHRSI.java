package roart.talib.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jfree.data.category.DefaultCategoryDataset;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.StochasticRSIIndicator;

import roart.model.StockItem;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.stockutil.StockUtil;
import roart.talib.Ta4j;
import roart.talib.util.TaConstants;
import roart.talib.util.TaUtil;

public class Ta4jSTOCHRSI extends Ta4j {
    @Override
    protected Object[] getInner(double[][] arrarr, int size) {
        double[] values = arrarr[0];
        double[] rsi = new double[values.length];
        double[] rsi2 = new double[values.length];
        Object[] objs = new Object[4];
        objs[0] = rsi;
        //objs[1] = rsi2;
        objs[1] = 0; // beg;
        objs[2] = values.length; // end;
        if (size == 0) {
            return objs;
        }
        TimeSeries series = getClosedSeries(values, size);
        StochasticRSIIndicator i = new StochasticRSIIndicator(series, 14);
        double[] rsi3 = new double[values.length];
        for (int j = 0; j < values.length; j++) {
            rsi3[j] = i.getValue(j).doubleValue();
            if (Double.isNaN(rsi3[j])) {
                int jj = 0;
            }
        }
        objs[0] = rsi3;
        //objs[1] = rsi3;
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
