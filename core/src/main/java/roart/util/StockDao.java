package roart.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roart.model.Stock;
import roart.model.StockItem;
import roart.config.MyConfig;

public class StockDao {
	
    public static Double getPeriod(Stock stock, int i) throws Exception {
        if (i == 0) {
            return stock.getPeriod1();
        }
        if (i == 1) {
            return stock.getPeriod2();
        }
        if (i == 2) {
            return stock.getPeriod3();
        }
        if (i == 3) {
            return stock.getPeriod4();
        }
        if (i == 4) {
            return stock.getPeriod5();
        }
        if (i == 5) {
            return stock.getPeriod6();
        }
        throw new Exception("Out of range " + i);
    }

    public static Double getPeriod(StockItem stock, int i) throws Exception {
    	return stock.getPeriod(i);
    }

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

    private static void mapAdd(Map<String, List<Double>> aMap, String id, Double value) {
    	List<Double> aList = aMap.get(id);
    	if (aList == null) {
    		aList = new ArrayList();
    		aMap.put(id, aList);
    	}
    	aList.add(value);
    }
    
	public static Map<String, List<Double>> getArr(MyConfig conf, String market, String date, Integer periodInt, int count, int mytableintervaldays,
			Map<String, MarketData> marketdataMap) throws Exception {
		Map<String, List<Double>> retMap = new HashMap();
		List<StockItem> datedstocklists[] = marketdataMap.get(market).datedstocklists;
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
}
