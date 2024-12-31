package roart.talib.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jfree.data.category.DefaultCategoryDataset;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.indicators.StochasticRSIIndicator;

import roart.common.pipeline.data.SerialTA;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.stockutil.StockUtil;
import roart.talib.Ta4j;
import roart.talib.util.TaConstants;
import roart.talib.util.TaUtil;

public class Ta4jCCI extends Ta4j {
    @Override
    protected SerialTA getInner(double[][] arrarr, int size) {
        double[] close = arrarr[0];
        double[] low = arrarr[1];
        double[] high = arrarr[2];
        double[] cci = new double[close.length];
        Integer[] objs = new Integer[3];
        double[][] objsarr = new double[3][];
        objsarr[0] = cci;
        objs[1] = 0; // beg;
        objs[2] = size; // end;
        if (size == 0) {
            return new SerialTA(objs, objsarr, 0, size);
        }
        BarSeries series = getThreeSeries(close, low, high, size);
        CCIIndicator i = new CCIIndicator(series, 14);
        for (int j = 0; j < size; j++) {
            cci[j] = i.getValue(j).doubleValue();
            if (Double.isNaN(cci[j])) {
                int jj = 0;
            }
        }
        return new SerialTA(objs, objsarr, 0, size);
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
