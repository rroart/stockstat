package roart.stockutil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import roart.common.config.MyConfig;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.model.OHLC;
import roart.common.model.StockItem;
import roart.common.util.ArraysUtil;
import roart.common.util.MapUtil;
import roart.common.util.TimeUtil;
import roart.model.data.MarketData;

public class StockDao {

    public static Double getMainPeriod(StockItem stock, int i) throws Exception {
        return stock.getPeriod(i);
    }

    public static Double[] getPeriod(StockItem stock, int i) throws Exception {
        Double[] retValue = new Double[1];
        retValue[0] = stock.getPeriod(i);
        return retValue;
    }

    /**
     * Get specific period or special type from StockItem
     * 
     * @param stock the desired StockItem
     * @param i period/special
     * @return the value
     * @throws Exception
     */

    public static Double[] getValue(StockItem stock, int i) throws Exception {
        if (i >= 0) {
            return getPeriod(stock, i);
        } else {
            return getSpecial(stock, i);
        }
    }

    public static Double getMainValue(StockItem stock, int i) throws Exception {
        if (i >= 0) {
            return getMainPeriod(stock, i);
        } else {
            return getMainSpecial(stock, i);
        }
    }

    public static Double getMainSpecial(StockItem stock, int i) throws Exception {
        if (i == Constants.INDEXVALUECOLUMN) {
            return stock.getIndexvalue();
        }
        if (i == Constants.PRICECOLUMN) {
            return stock.getPrice();
        }
        throw new Exception("Out of range " + i);
    }

    public static OHLC getSpecialOHLC(StockItem stock, int i) throws Exception {
        if (i == Constants.INDEXVALUECOLUMN) {
            return stock.getIndexvalueOHLC();
        }
        if (i == Constants.PRICECOLUMN) {
            return stock.getPriceOHLC();
        }
        throw new Exception("Out of range " + i);
    }

    public static Double[] getSpecial(StockItem stock, int i) throws Exception {
        if (i == Constants.INDEXVALUECOLUMN) {
            return stock.getIndexvalues();
        }
        if (i == Constants.PRICECOLUMN) {
            return stock.getPrices();
        }
        throw new Exception("Out of range " + i);
    }

    /**
     * Make a map of all ids to a list of list of period values
     * The list has the newest item first
     * 
     * @param conf unused
     * @param market name
     * @param date unused
     * @param periodInt the period
     * @param count unused
     * @param mytableintervaldays unused
     * @param marketdataMap
     * @return the map
     * @throws Exception
     */

    public static Map<String, List<Double>[]> getArr(MyConfig conf, String market, String date, Integer periodInt, int count, int mytableintervaldays,
            Map<String, MarketData> marketdataMap) throws Exception {
        Map<String, List<Double>[]> retMap = new HashMap<>();
        List<StockItem> datedstocklists[] = marketdataMap.get(market).datedstocklists;
        int index = 0;
        if (index >= 0) {
            for (int i = index; i < datedstocklists.length; i++) {
                List<StockItem> stocklist = datedstocklists[i];
                for (StockItem stock : stocklist) {
                    String stockid = stock.getId();
                    Double[] value = StockDao.getValue(stock, periodInt);
                    if (value != null) {
                        MapUtil.mapAdd(retMap, stockid, value);
                    }
                }
            }
        }
        return retMap;
    }

    public static List<String> getDateList(MyConfig conf, String market, String date, Integer periodInt, int count, int mytableintervaldays,
            Map<String, MarketData> marketdataMap, boolean currentyear) throws Exception {
        List<String> retList = new ArrayList<>();
        List<StockItem> datedstocklists[] = marketdataMap.get(market).datedstocklists;
        for (int i = datedstocklists.length - 1; i >= 0; i--) {
            List<StockItem> list = datedstocklists[i];
            if (!list.isEmpty()) {
                retList.add(TimeUtil.convertDate3(list.get(0).getDate()));
            }
        }
        return retList;
    }

    public static List<String> getDateList(String market,
            Map<String, MarketData> marketdataMap) throws Exception {
        List<String> retList = new ArrayList<>();
        List<StockItem> datedstocklists[] = marketdataMap.get(market).datedstocklists;
        for (int i = datedstocklists.length - 1; i >= 0; i--) {
            List<StockItem> list = datedstocklists[i];
            if (!list.isEmpty()) {
                retList.add(TimeUtil.convertDate3(list.get(0).getDate()));
            }
        }
        return retList;
    }

    public static Map<String, String> getNameMap(MyConfig conf, String market, String date, Integer periodInt, int count, int mytableintervaldays,
            Map<String, MarketData> marketdataMap, boolean currentyear) throws Exception {
        Map<String, String> retList = new HashMap<>();
        List<StockItem>[] datedstocklists = marketdataMap.get(market).datedstocklists;
        List<StockItem> list = datedstocklists[0];
        for (StockItem stock : list) {
            retList.put(stock.getId(), stock.getName());
        }
        return retList;
    }

    public static Map<String, Double[]> getReverse(Map<String, Double[]> listMap) {
        Map<String, Double[]> retMap = new HashMap<>();
        for (Entry<String, Double[]> entry : listMap.entrySet()) {
            Double[] array = entry.getValue();
            ArrayUtils.reverse(array);
            retMap.put(entry.getKey(), array);
        }
        return retMap;
    }
    
    public static Pair<Long, String> getVolume(StockItem stock) throws Exception {
        return new ImmutablePair<Long, String>(stock.getVolume(), stock.getCurrency());   
    }

    
}
