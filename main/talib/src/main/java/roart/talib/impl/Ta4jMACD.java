package roart.talib.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.jfree.data.category.DefaultCategoryDataset;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.DifferenceIndicator;

import roart.common.util.ArraysUtil;
import roart.model.StockItem;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.stockutil.StockUtil;
import roart.talib.Ta4j;
import roart.talib.util.TaConstants;
import roart.talib.util.TaUtil;

public class Ta4jMACD extends Ta4j {
    public static final int MACDIDXMACD = 0;
    private static final int MACDIDXSIGN = 1;
    public static final int MACDIDXHIST = 2;
    public static final int MACDIDXBEG = 3;
    public static final int MACDIDXEND = 4;
    private static final int MACDIDXMACDFIXED = 5;
    private static final int MACDIDXSIGFIXED = 6;
    private static final int MACDIDXHISTFIXED = 7;

    @Override
    protected Object[] getInner(double[][] arrarr, int size) {
        double[] values = arrarr[0];
        long time0 = System.currentTimeMillis();
        values = ArraysUtil.getPercentizedPriceIndex(values);
        Object[] objs = new Object[8];
        double[] macd = new double[values.length];
        double[] sig = new double[values.length];
        double[] hist = new double[values.length];
        double[] rsi3 = new double[values.length];
        objs[MACDIDXMACD] = macd;
        objs[MACDIDXSIGN] = sig;
        objs[MACDIDXHIST] = hist;
        objs[MACDIDXBEG] = 0;
        objs[MACDIDXEND] = values.length;
        objs[MACDIDXMACDFIXED] = ArraysUtil.makeFixed(macd, 0, values.length, values.length);
        objs[MACDIDXSIGFIXED] = ArraysUtil.makeFixed(sig, 0, values.length, values.length);
        objs[MACDIDXHISTFIXED] = ArraysUtil.makeFixed(hist, 0, values.length, values.length);
        if (size == 0) {
            return objs;
        }
        TimeSeries series = getClosedSeries(values, size);
        log.info(""+series.getBarCount());
        if (series.getBarCount() == 0) {
            int jj = 0;
        }
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        EMAIndicator shortEma = new EMAIndicator(closePrice, 9);
        EMAIndicator signal = shortEma;
        EMAIndicator longEma = new EMAIndicator(closePrice, 26);
        MACDIndicator macdI = new MACDIndicator(closePrice, 12, 26);
        EMAIndicator emaMacd = new EMAIndicator(macdI, 9);
        signal = emaMacd;
        DifferenceIndicator macdIndicator2 = new DifferenceIndicator(shortEma, longEma);
        DifferenceIndicator macdIndicator = new DifferenceIndicator(macdI, signal);
        for (int j = 0; j < values.length; j++) {
            log.info(""+j      );
            sig[j] = signal.getValue(j).doubleValue();
            macd[j] = macdI.getValue(j).doubleValue();
            hist[j] = macdIndicator.getValue(j).doubleValue();
        }
        log.info("timer {}", System.currentTimeMillis() - time0);
        return objs;
    }

    @Override
    protected String[] getTitles() {
        return new String[] { "macd", "sig", "hist" };
    }
    
    @Override 
    protected int[][] getArrayMeta() {
        return TaConstants.THREE;
    }

    @Override
    protected int getInputArrays() {
	return 1;
    }
}
