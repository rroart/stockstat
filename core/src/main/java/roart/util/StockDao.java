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
	
    public static Double getPeriod(StockItem stock, int i) throws Exception {
    	return stock.getPeriod(i);
    }

    /**
     * Get specific period or special type from StockItem
     * 
     * @param stock the desired StockItem
     * @param i period/special
     * @return the value
     * @throws Exception
     */
    
    public static Double getValue(StockItem stock, int i) throws Exception {
        if (i >= 0) {
            return getPeriod(stock, i);
        } else {
            return getSpecial(stock, i);
        }
    }
    
     public static Double getSpecial(StockItem stock, int i) throws Exception {
        if (i == Constants.INDEXVALUECOLUMN) {
            return stock.getIndexvalue();
        }
        if (i == Constants.PRICECOLUMN) {
            return stock.getPrice();
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
    
    public static void mapAdd(Map<String, Double[]> aMap, String id, int index, Double value, int length) {
        Double[] array = aMap.get(id);
        if (array == null) {
            array = new Double[length];
            aMap.put(id, array);
        }
        array[index] = value;
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
    
    public static Map<String, List<Double>> getArr(MyConfig conf, String market, String date, Integer periodInt, int count, int mytableintervaldays,
            Map<String, MarketData> marketdataMap) throws Exception {
        Map<String, List<Double>> retMap = new HashMap<>();
        List<StockItem> datedstocklists[] = marketdataMap.get(market).datedstocklists;
        //System.out.println("datstolen " + datedstocklists.length);
        int index = 0;
        if (index >= 0) {
            for (int i = index; i < datedstocklists.length; i++) {
                List<StockItem> stocklist = datedstocklists[i];
                for (StockItem stock : stocklist) {
                    String stockid = stock.getId();
                    Double value = StockDao.getValue(stock, periodInt);
                    if (value != null) {
                        mapAdd(retMap, stockid, value);
                    }
                }
            }
        }
        return retMap;
    }

    public static Map<String, Double[]> getArrSparse(MyConfig conf, String market, String date, Integer periodInt, int count, int mytableintervaldays,
			Map<String, MarketData> marketdataMap, boolean currentyear) throws Exception {
		Map<String, Double[]> retMap = new HashMap<>();
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
        			Double value = StockDao.getValue(stock, periodInt);
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
                    Double value = StockDao.getValue(stock, periodInt);
                    if (value != null) {
                        value = 0.01 * value + 1;
                        Double basenumber = basenumberMap.get(stockid);
                        if (basenumber == null) {
                            basenumber = 1.0;
                        }
                        value = value * basenumber;
                    }
                    if (value != null) {
                        lastnumberMap.put(stockid, value);
                    }
                    mapAdd(retMap, stockid, datedstocklists.length - 1 - i, value, datedstocklists.length);
                }
            }
            System.out.println("base " + basenumberMap.values());
            System.out.println("retmap " + Arrays.asList(retMap.get("0P0000A30R")));
        }
        System.out.println("nullnul" + nonn + " " + nu);
        retMap = getReverseArrSparseFillHoles(conf, retMap);
		return retMap;
	}
    
    public static List<Date> getDateList(MyConfig conf, String market, String date, Integer periodInt, int count, int mytableintervaldays,
            Map<String, MarketData> marketdataMap, boolean currentyear) throws Exception {
        List<Date> retList = new ArrayList<>();
        List<StockItem> datedstocklists[] = marketdataMap.get(market).datedstocklists;
        for (int i = datedstocklists.length - 1; i >= 0; i--) {
            retList.add(datedstocklists[i].get(0).getDate());
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
