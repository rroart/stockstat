package roart.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ArrayUtils;

import roart.config.MyConfig;
import roart.config.MyMyConfig;
import roart.model.StockItem;

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
     * The id is mapped to a list, and the value is added to that list
     * 
     * @param aMap the map
     * @param id Map id
     * @param value value to map
     */

    public static void mapAdd(Map<String, List<Double>> aMap, String id, Double value) {
        List<Double> aList = aMap.computeIfAbsent(id, k -> new ArrayList<>());
        aList.add(value);
    }

    public static void mapAdd(Map<String, List<Double>[]> aMap, String id, Double[] value) {
        List<Double>[] aList = aMap.get(id);
        for (int i = 0; i < value.length; i++) {
            if (aList == null) {
                aList = new ArrayList[3];
                aMap.put(id, aList);
            }
            aList[i].add(value[i]);
        }
    }

    public static void mapAdd(Map<String, Double[]> aMap, String id, int index, Double value, int length) {
        Double[] array = aMap.computeIfAbsent(id, k -> new Double[length]);
        array[index] = value;
    }

    public static void mapAdd(Map<String, Double[][]> aMap, String id, int index, Double[] value, int length) {
        Double[][] array = aMap.get(id);
        for (int i = 0; i < value.length; i++) {
            if (array == null) {
                array = new Double[value.length][length];
                aMap.put(id, array);
            }
            array[i][index] = value[i];
        }
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
                        mapAdd(retMap, stockid, value);
                    }
                }
            }
        }
        return retMap;
    }

    public static Map<String, Double[][]> getArrSparse(MyMyConfig conf, String market, String date, Integer periodInt, int count, int mytableintervaldays,
            Map<String, MarketData> marketdataMap, boolean currentyear) throws Exception {
        Map<String, Double[][]> retMap = new HashMap<>();
        List<StockItem> datedstocklists[] = marketdataMap.get(market).datedstocklists;
        int index = 0;
        if (!currentyear) {
            if (index >= 0) {
                for (int i = datedstocklists.length - 1; i >= 0; i--) {
                    List<StockItem> stocklist = datedstocklists[i];
                    for (StockItem stock : stocklist) {
                        String stockid = stock.getId();
                        Double[] value = StockDao.getValue(stock, periodInt);
                        mapAdd(retMap, stockid, datedstocklists.length - 1 - i, value, datedstocklists.length);
                    }
                }
            }
        } else {
            Map<String, Double> basenumberMap = new HashMap<>();
            Map<String, Double> lastnumberMap = new HashMap<>();
            Map<String, Integer> yearMap = new HashMap<>();
            for (int i = datedstocklists.length - 1; i >= 0; i--) {
                List<StockItem> stocklist = datedstocklists[i];
                for (StockItem stock : stocklist) {
                    String stockid = stock.getId();
                    int curYear = stock.getDate().getYear();
                    Integer thisYear = yearMap.get(stockid);
                    if (thisYear == null) {
                        thisYear = curYear;
                        yearMap.put(stockid, thisYear);
                    }
                    if (curYear != thisYear) {
                        Double basenumber = basenumberMap.get(stockid);
                        if (basenumber == null) {
                            basenumber = 1.0;
                        }
                        Double lastnumber = lastnumberMap.get(stockid);
                        if (lastnumber != null) {
                            basenumberMap.put(stockid, lastnumber);
                        } else {
                            basenumberMap.put(stockid, basenumber);                            
                        }
                        yearMap.put(stockid, curYear);
                    }
                    Double[] value = StockDao.getValue(stock, periodInt);
                    for (int ii = 0; ii < value.length; ii++ ) {
                        if (value[ii] != null) {
                            value[ii] = 0.01 * value[ii] + 1;
                            Double basenumber = basenumberMap.get(stockid);
                            if (basenumber == null) {
                                basenumber = 1.0;
                            }
                            value[ii] = value[ii] * basenumber;
                        }
                        if (value != null && ii == 0) {
                            lastnumberMap.put(stockid, value[ii]);
                        }
                    }
                    mapAdd(retMap, stockid, datedstocklists.length - 1 - i, value, datedstocklists.length);
                }
            }
        }
        retMap = getReverseArrSparseFillHolesArr(conf, retMap);
        return retMap;
    }

    public static List<Date> getDateList(MyConfig conf, String market, String date, Integer periodInt, int count, int mytableintervaldays,
            Map<String, MarketData> marketdataMap, boolean currentyear) throws Exception {
        List<Date> retList = new ArrayList<>();
        List<StockItem> datedstocklists[] = marketdataMap.get(market).datedstocklists;
        for (int i = datedstocklists.length - 1; i >= 0; i--) {
            List<StockItem> list = datedstocklists[i];
            if (!list.isEmpty()) {
                retList.add(list.get(0).getDate());
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

    public static int maxHoleNumber(MyMyConfig conf) {
        return conf.getMaxHoles();
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

    public static Map<String, Double[][]> getReverseArrSparseFillHolesArr(MyMyConfig conf, Map<String, Double[][]> listMap) {
        Map<String, Double[][]> retMap = /*getReverse*/(listMap);
        for (Entry<String, Double[][]> entry : listMap.entrySet()) {
            Double[][] array = entry.getValue();
            Double[][] newArray = new Double[array.length][];
            for (int i = 0; i < array.length; i ++) {
                newArray[i] = ArraysUtil.fixMapHoles(array[i], null, maxHoleNumber(conf));
            }
            retMap.put(entry.getKey(), newArray);
        }      
        return retMap;
    }

    public static Map<String, Double[]> getReverseArrSparseFillHoles(MyMyConfig conf, Map<String, Double[]> listMap) {
        Map<String, Double[]> retMap = /*getReverse*/(listMap);
        for (Entry<String, Double[]> entry : listMap.entrySet()) {
            retMap.put(entry.getKey(), ArraysUtil.fixMapHoles(entry.getValue(), null, maxHoleNumber(conf)));
        }      
        return retMap;
    }
}
