package roart.talib;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.model.StockItem;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.stockutil.StockUtil;
import roart.talib.util.TaConstants;
import roart.talib.util.TaUtil;

public abstract class Ta {
    protected static Logger log = LoggerFactory.getLogger(Ta.class);

    public Object[] calculate(double[][] array) {
        return getInner(array, array[0].length);
    }
    
    //protected abstract Object[] get(int days, String market, String id, Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr);

    private DefaultCategoryDataset getDataset(double[][] valuesArr, String[] titles, int begint, int endint) {
        int size = begint + endint;
        size--;
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        for (int i = 0; i < begint; i++) {
            for (int j = 0; j < titles.length; j++) {
                dataset.addValue(0, titles[j] , Integer.valueOf(i - size));
            }
        }
        for (int i = 0; i < endint; i++) {
            for (int j = 0; j < titles.length; j++) {
                dataset.addValue(valuesArr[j][i], titles[j] , Integer.valueOf(i + begint - size));
            }
        }
        return dataset;
    }

    public DefaultCategoryDataset getChart(int days, String market, String id, Set<Pair<String, String>> ids,
            Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr) {
        String[] titles = getTitles();
        int[][] arrarr = getArrayMeta();
        Object[] objs = get(days, market, id, ids, marketdatamap, perioddata, periodstr, getInputArrays());
        int[] array = arrarr[TaConstants.ARRAY];
        int[] range = arrarr[TaConstants.RANGE];
        int[] arrayfixed = arrarr[TaConstants.ARRAYFIXED];
        double[][] valuesArr = new double[array.length][];
        for (int i = 0; i < array.length; i++) {
            valuesArr[i] = (double[]) objs[array[i]];
            String string = Arrays.toString(valuesArr[i]);
            log.info("{} {}", titles[i], string);
       }
        int beg = (int) objs[range[0]];
        int end = (int) objs[range[1]];
        log.info("beg end {} {}", beg, end);
        
        return getDataset(valuesArr, titles, beg, end);
    }
    
    private Object[] get(int days, String market,
            String id, Set<Pair<String, String>> ids, Map<String, MarketData> marketdatamap, PeriodData perioddata, String periodstr, int arraysize) {
        Set<Pair<String, Integer>> pairs = perioddata.pairs;
        MarketData marketdata = marketdatamap.get(market);
        Integer periodInt = StockUtil.getPeriodByMarket(market, pairs);
        List<StockItem>[] datedstocklists = marketdata.datedstocklists;
        double[][] arrarr = new double[arraysize][];
        for (int i = 0; i < arraysize; i++) {
            arrarr[i] = new double[days];
        }
        int size = new TaUtil().getArr(days, market, id, ids, periodInt, datedstocklists, arrarr);
        return getInner(arrarr, size);
    }

    protected abstract Object[] getInner(double[][] arrarr, int size);

    protected abstract String[] getTitles();
    
    protected abstract int[][] getArrayMeta();
        
    protected abstract int getInputArrays();
}
