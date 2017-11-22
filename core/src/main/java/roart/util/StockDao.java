package roart.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.jfree.util.Log;

import roart.model.StockItem;
import roart.config.MyConfig;

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
    	List<Double> aList = aMap.get(id);
    	if (aList == null) {
    		aList = new ArrayList<>();
    		aMap.put(id, aList);
    	}
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
        Double[] array = aMap.get(id);
        if (array == null) {
            array = new Double[length];
            aMap.put(id, array);
        }
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
    
    /*
    public static void mapAdd(Map<String, Double[]> aMap, String id, Double value) {
        List<Double> aList = aMap.get(id);
        if (aList == null) {
            aList = new ArrayList<>();
            aList.set
            aMap.put(id, aList);
        }
        aList.add(value);
    }
    */
    
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
        //System.out.println("datstolen " + datedstocklists.length);
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

    public static Map<String, Double[][]> getArrSparse(MyConfig conf, String market, String date, Integer periodInt, int count, int mytableintervaldays,
			Map<String, MarketData> marketdataMap, boolean currentyear) throws Exception {
		Map<String, Double[][]> retMap = new HashMap<>();
		List<StockItem> datedstocklists[] = marketdataMap.get(market).datedstocklists;
        System.out.println("datstolen " + datedstocklists.length);
		int index = 0;
		int nonn = 0;
		int nu = 0;
        if (!currentyear) {
        if (index >= 0) {
            for (int i = datedstocklists.length - 1; i >= 0; i--) {
        	//for (int i = index; i < datedstocklists.length; i++) {
        		List<StockItem> stocklist = datedstocklists[i];
        		//System.out.println("datstolen2 " + stocklist.size());
        		for (StockItem stock : stocklist) {
        			String stockid = stock.getId();
        			Double[] value = StockDao.getValue(stock, periodInt);
        			if (value == null) {
        			    nu++;
        			} else {
        			    nonn++;
        			}
        			mapAdd(retMap, stockid, datedstocklists.length - 1 - i, value, datedstocklists.length);
        		}
        	}
        }
        } else {
            List<List<StockItem>> yearList = new ArrayList<>();
            System.out.println("date" + datedstocklists[datedstocklists.length -1].get(0).getDate());
            System.out.println("date" + datedstocklists[0].get(0).getDate());
            Map<String, Double> basenumberMap = new HashMap<>();
            Map<String, Double> lastnumberMap = new HashMap<>();
            Map<String, Integer> yearMap = new HashMap<>();
            for (int i = datedstocklists.length - 1; i >= 0; i--) {
                List<StockItem> stocklist = datedstocklists[i];
                //System.out.println("datstolen2 " + stocklist.size());
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
                    //Double mainValue = StockDao.getMainValue(stock, periodInt);
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
            System.out.println("base " + basenumberMap.values());
            //System.out.println("retmap " + Arrays.asList(retMap.get("0P0000A30R")));
        }
        System.out.println("nullnul" + nonn + " " + nu);
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
        List<StockItem> datedstocklists[] = marketdataMap.get(market).datedstocklists;
        int i = datedstocklists.length - 1;
        List<StockItem> list = datedstocklists[0];
        for (StockItem stock : list) {
            retList.put(stock.getId(), stock.getName());
        }
        return retList;
    }
    
    public static int maxHoleNumber() {
        return 5;
    }
    
    public static Map<String, Double[]> getReverse(Map<String, Double[]> listMap) {
        Map<String, Double[]> retMap = new HashMap<>();
        for (String id : listMap.keySet()) {
            //retMap.put(id, ArraysUtil.getArrayNonNullReverse(listMap.get(id)));
            Double[] array = listMap.get(id);
            ArrayUtils.reverse(array);
            retMap.put(id, array);
        }
        return retMap;
    }
    
    public static Map<String, Double[][]> getReverseArrSparseFillHolesArr(MyConfig conf, Map<String, Double[][]> listMap) {
        Map<String, Double[][]> retMap = /*getReverse*/(listMap);
        //System.out.println("carn " + Arrays.asList(listMap.get("F00000NMNP")));
        for (String id : listMap.keySet()) {
            Double[][] array = listMap.get(id);
            Double[][] newArray = new Double[array.length][];
            for (int i = 0; i < array.length; i ++) {
                newArray[i] = ArraysUtil.fixMapHoles(array[i], null, maxHoleNumber());
            }
            retMap.put(id, newArray);
        }      
        //System.out.println("carn " + Arrays.asList(retMap.get("F00000NMNP")));
        return retMap;
    }
    
    public static Map<String, Double[]> getReverseArrSparseFillHoles(MyConfig conf, Map<String, Double[]> listMap) {
        Map<String, Double[]> retMap = /*getReverse*/(listMap);
        //System.out.println("carn " + Arrays.asList(listMap.get("F00000NMNP")));
        for (String id : listMap.keySet()) {
            /*
            if (id.equals("F00000NMNP")) {
                int j = 1;
            }
            */
            retMap.put(id, ArraysUtil.fixMapHoles(listMap.get(id), null, maxHoleNumber()));
        }      
        //System.out.println("carn " + Arrays.asList(retMap.get("F00000NMNP")));
        return retMap;
    }
}
