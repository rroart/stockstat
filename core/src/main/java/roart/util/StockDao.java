package roart.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			Map<String, MarketData> marketdataMap) throws Exception {
		Map<String, Double[]> retMap = new HashMap<>();
		List<StockItem> datedstocklists[] = marketdataMap.get(market).datedstocklists;
        //System.out.println("datstolen " + datedstocklists.length);
		int index = 0;
        if (index >= 0) {
        	for (int i = index; i < datedstocklists.length; i++) {
        		List<StockItem> stocklist = datedstocklists[i];
        		for (StockItem stock : stocklist) {
        			String stockid = stock.getId();
        			Double value = StockDao.getValue(stock, periodInt);
        			mapAdd(retMap, stockid, i, value, datedstocklists.length);
        		}
        	}
        }
		return retMap;
	}
}
