package roart.db.dao.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import roart.common.config.MyMyConfig;
import roart.common.model.StockItem;
import roart.common.util.TimeUtil;

// Impurity, should be in ETL, only temporarily here.

public class StockETL {
    public static List<StockItem> filterWeekend(MyMyConfig conf, List<StockItem> stocks) {
        if (!conf.wantFilterWeekend()) {
            return stocks;
        }
        Calendar calendar = Calendar.getInstance();
        List<StockItem> retList = new ArrayList<>();
        for (StockItem stock : stocks) {
            Date date = stock.getDate();
            if (date == null || stock.getDate() == null) {
                int jj = 0;
            }
            calendar.setTime(date);
            if (!(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                    calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)) {
                retList.add(stock);
            }
        }
        return retList;
    }
    
    public static List<String> filterWeekendConvert(MyMyConfig conf, List<Date> dates) {
        boolean filter = conf.wantFilterWeekend();
        Calendar calendar = Calendar.getInstance();
        List<String> retList = new ArrayList<>();
        for (Date date : dates) {
            if (date == null) {
                int jj = 0;
            }
            calendar.setTime(date);
            if (!filter || !(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                    calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)) {
                retList.add(TimeUtil.convertDate3(date));
            }
        }
        return retList;
    }
    
    public static Map<String, List<StockItem>> filterWeekend(MyMyConfig conf, Map<String, List<StockItem>> stockMap) {
        if (!conf.wantFilterWeekend()) {
            return stockMap;
        }
        Map<String, List<StockItem>> retMap = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        for (Entry<String, List<StockItem>> entry : stockMap.entrySet()) {
            List<StockItem> stocks = entry.getValue();
            Date date = stocks.get(0).getDate();
            calendar.setTime(date);
            if (!(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                    calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)) {
                retMap.put(entry.getKey(), stocks);
            }
        }
        return retMap;
    }
}
