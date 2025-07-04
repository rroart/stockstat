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

import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.common.model.OHLC;
import roart.common.model.StockDTO;
import roart.common.util.ArraysUtil;
import roart.common.util.MapUtil;
import roart.common.util.TimeUtil;
import roart.model.data.MarketData;

public class StockDao {

    public static Double getMainPeriod(StockDTO stock, int i) throws Exception {
        return stock.getPeriod(i);
    }

    public static Double[] getPeriod(StockDTO stock, int i) throws Exception {
        Double[] retValue = new Double[1];
        retValue[0] = stock.getPeriod(i);
        return retValue;
    }

    /**
     * Get specific period or special type from StockDTO
     * 
     * @param stock the desired StockDTO
     * @param i period/special
     * @return the value
     * @throws Exception
     */

    public static Double[] getValue(StockDTO stock, int i) throws Exception {
        if (i >= 0) {
            return getPeriod(stock, i);
        } else {
            return getSpecial(stock, i);
        }
    }

    public static Double getMainValue(StockDTO stock, int i) throws Exception {
        if (i >= 0) {
            return getMainPeriod(stock, i);
        } else {
            return getMainSpecial(stock, i);
        }
    }

    public static Double getMainSpecial(StockDTO stock, int i) throws Exception {
        if (i == Constants.INDEXVALUECOLUMN) {
            return stock.getIndexvalue();
        }
        if (i == Constants.PRICECOLUMN) {
            return stock.getPrice();
        }
        throw new Exception("Out of range " + i);
    }

    public static OHLC getSpecialOHLC(StockDTO stock, int i) throws Exception {
        if (i == Constants.INDEXVALUECOLUMN) {
            return stock.getIndexvalueOHLC();
        }
        if (i == Constants.PRICECOLUMN) {
            return stock.getPriceOHLC();
        }
        throw new Exception("Out of range " + i);
    }

    public static Double[] getSpecial(StockDTO stock, int i) throws Exception {
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

    public static Map<String, List<Double>[]> getArr(IclijConfig conf, String market, String date, Integer periodInt, int count, int mytableintervaldays,
            Map<String, MarketData> marketdataMap) throws Exception {
        Map<String, List<Double>[]> retMap = new HashMap<>();
        List<StockDTO> datedstocklists[] = marketdataMap.get(market).datedstocklists;
        int index = 0;
        if (index >= 0) {
            for (int i = index; i < datedstocklists.length; i++) {
                List<StockDTO> stocklist = datedstocklists[i];
                for (StockDTO stock : stocklist) {
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

    public static List<String> getDateList(String market, Map<String, MarketData> marketdataMap) throws Exception {
        List<String> retList = new ArrayList<>();
        List<StockDTO> datedstocklists[] = marketdataMap.get(market).datedstocklists;
        for (int i = datedstocklists.length - 1; i >= 0; i--) {
            List<StockDTO> list = datedstocklists[i];
            if (!list.isEmpty()) {
                retList.add(TimeUtil.convertDate3(list.get(0).getDate()));
            } else {
                int jj = 0;
            }
        }
        return retList;
    }

    public static Map<String, String> getNameMap(IclijConfig conf, String market, String date, Integer periodInt, int count, int mytableintervaldays,
            Map<String, MarketData> marketdataMap, boolean currentyear) throws Exception {
        Map<String, String> retList = new HashMap<>();
        List<StockDTO>[] datedstocklists = marketdataMap.get(market).datedstocklists;
        List<StockDTO> list = datedstocklists[0];
        for (StockDTO stock : list) {
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
    
    public static Pair<Long, String> getVolume(StockDTO stock) throws Exception {
        return new ImmutablePair<Long, String>(stock.getVolume(), stock.getCurrency());   
    }

    
}
