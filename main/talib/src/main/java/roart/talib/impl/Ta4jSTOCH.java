package roart.talib.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jfree.data.category.DefaultCategoryDataset;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.StochasticRSIIndicator;

import roart.model.StockItem;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.stockutil.StockUtil;
import roart.talib.Ta4j;
import roart.talib.util.TaConstants;
import roart.talib.util.TaUtil;

public class Ta4jSTOCH extends Ta4j {
    @Override
    protected Object[] getInner(double[][] arrarr, int size) {
        double[] close = arrarr[0];
        double[] low = arrarr[1];
        double[] high = arrarr[2];
        double[] stochk = new double[close.length];
        double[] stochd = new double[close.length];
        Object[] objs = new Object[4];
        objs[0] = stochk;
        objs[1] = stochd;
        objs[2] = 0; // beg;
        objs[3] = close.length; // end;
        if (size == 0) {
            return objs;
        }
        TimeSeries series = getThreeSeries(close, low, high, size);
        StochasticOscillatorKIndicator kIndicator = new StochasticOscillatorKIndicator(series, 14);
        StochasticOscillatorDIndicator dIndicator = new StochasticOscillatorDIndicator(kIndicator);
        for (int j = 0; j < close.length; j++) {
            stochk[j] = kIndicator.getValue(j).doubleValue();
            stochd[j] = dIndicator.getValue(j).doubleValue();
            if (Double.isNaN(stochk[j])) {
                int jj = 0;
            }
        }
        return objs;
    }

    @Override
    protected String[] getTitles() {
        return new String[] { "stoch" };
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
