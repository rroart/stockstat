package roart.talib.impl;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;

import roart.common.pipeline.data.SerialTA;
import roart.talib.Ta4j;
import roart.talib.util.TaConstants;

public class Ta4jSTOCH extends Ta4j {
    @Override
    protected SerialTA getInner(double[][] arrarr, int size) {
        double[] close = arrarr[0];
        double[] low = arrarr[1];
        double[] high = arrarr[2];
        double[] stochk = new double[close.length];
        double[] stochd = new double[close.length];
        Integer[] objs = new Integer[4];
        double[][] objsarr = new double[4][];
        objsarr[0] = stochk;
        objsarr[1] = stochd;
        objs[2] = 0; // beg;
        objs[3] = size; // end;
        if (size == 0) {
            return new SerialTA(objs, objsarr, 0, size);
        }
        BarSeries series = getThreeSeries(close, low, high, size);
        StochasticOscillatorKIndicator kIndicator = new StochasticOscillatorKIndicator(series, 14);
        StochasticOscillatorDIndicator dIndicator = new StochasticOscillatorDIndicator(kIndicator);
        for (int j = 0; j < size; j++) {
            stochk[j] = kIndicator.getValue(j).doubleValue();
            stochd[j] = dIndicator.getValue(j).doubleValue();
            if (Double.isNaN(stochk[j])) {
                int jj = 0;
            }
        }
        return new SerialTA(objs, objsarr, 0, size);
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
