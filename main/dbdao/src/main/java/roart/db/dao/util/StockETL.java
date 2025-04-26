package roart.db.dao.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import roart.iclij.config.IclijConfig;
import roart.common.model.StockDTO;
import roart.common.util.TimeUtil;

// Impurity, should be in ETL, only temporarily here.

public class StockETL {
    public static List<StockDTO> filterWeekend(IclijConfig conf, List<StockDTO> stocks) {
        if (!conf.wantFilterWeekend()) {
            return stocks;
        }
        Calendar calendar = Calendar.getInstance();
        List<StockDTO> retList = new ArrayList<>();
        for (StockDTO stock : stocks) {
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
    
    public static List<String> filterWeekendConvert(IclijConfig conf, List<Date> dates) {
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
    
    public static Map<String, List<StockDTO>> filterWeekend(IclijConfig conf, Map<String, List<StockDTO>> stockMap) {
        if (!conf.wantFilterWeekend()) {
            return stockMap;
        }
        Map<String, List<StockDTO>> retMap = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        for (Entry<String, List<StockDTO>> entry : stockMap.entrySet()) {
            List<StockDTO> stocks = entry.getValue();
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
