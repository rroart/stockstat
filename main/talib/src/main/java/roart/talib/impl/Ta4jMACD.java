package roart.talib.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jfree.data.category.DefaultCategoryDataset;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.CombineIndicator;
import org.ta4j.core.indicators.numeric.NumericIndicator;

import roart.common.pipeline.data.SerialTA;
import roart.common.util.ArraysUtil;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.stockutil.StockUtil;
import roart.talib.Ta4j;
import roart.talib.util.TaConstants;
import roart.talib.util.TaUtil;

public class Ta4jMACD extends Ta4j {
    public static final int MACDIDXHIST = 0;
    public static final int MACDIDXMACD = 1;
    private static final int MACDIDXSIGN = 2;
    public static final int MACDIDXBEG = 3;
    public static final int MACDIDXEND = 4;
    private static final int MACDIDXMACDFIXED = 5;
    private static final int MACDIDXSIGFIXED = 6;
    private static final int MACDIDXHISTFIXED = 7;

    @Override
    protected SerialTA getInner(double[][] arrarr, int size) {
        double[] values = arrarr[0];
        long time0 = System.currentTimeMillis();
        //values = ArraysUtil.getPercentizedPriceIndex(values);
        Integer[] objs = new Integer[8];
        double[][] objsarr = new double[8][];
        double[] macd = new double[values.length];
        double[] sig = new double[values.length];
        double[] hist = new double[values.length];
        double[] rsi3 = new double[values.length];
        objsarr[MACDIDXMACD] = macd;
        objsarr[MACDIDXSIGN] = sig;
        objsarr[MACDIDXHIST] = hist;
        objs[MACDIDXBEG] = 0;
        objs[MACDIDXEND] = size;
        objsarr[MACDIDXMACDFIXED] = ArraysUtil.makeFixed(macd, 0, values.length, values.length);
        objsarr[MACDIDXSIGFIXED] = ArraysUtil.makeFixed(sig, 0, values.length, values.length);
        objsarr[MACDIDXHISTFIXED] = ArraysUtil.makeFixed(hist, 0, values.length, values.length);
        if (size == 0) {
            return new SerialTA(objs, objsarr, 0, size);
        }
        BarSeries series = getClosedSeries(values, size);
        log.debug(""+series.getBarCount());
        if (series.getBarCount() == 0) {
            int jj = 0;
        }
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        EMAIndicator shortEma = new EMAIndicator(closePrice, 9);
        EMAIndicator signal = shortEma;
        EMAIndicator longEma = new EMAIndicator(closePrice, 26);
        MACDIndicator macdI = new MACDIndicator(closePrice, 12, 26);
        EMAIndicator emaMacd = new EMAIndicator(macdI, 9);
        System.out.println("unstab2 "+ macdI.getUnstableBars());
        System.out.println("unstab3 "+ signal.getUnstableBars());
        signal = emaMacd;
        //CombineIndicator macdIndicator2 = new CombineIndicator(shortEma, longEma);
        CombineIndicator histogram2 = CombineIndicator.minus(macdI, emaMacd);
        NumericIndicator histogram = NumericIndicator.of(macdI).minus(emaMacd);
        for (int j = 0; j < size; j++) {
            log.debug(""+j      );
            sig[j] = signal.getValue(j).doubleValue();
            macd[j] = macdI.getValue(j).doubleValue();
            hist[j] = histogram.getValue(j).doubleValue();
        }
        log.debug("timer {}", System.currentTimeMillis() - time0);
        return new SerialTA(objs, objsarr, 0, size);
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
