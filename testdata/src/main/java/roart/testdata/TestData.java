package roart.testdata;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roart.common.util.TimeUtil;

public class TestData {
    public Map<String, List<List<Double>>> getAbnormCatValMap() {
        Map<String, List<List<Double>>> map = new HashMap<>();
        List<Double> l = Arrays.asList(new Double[] { 1.0, 10.0, 1.0 } );
        List<List<Double>> l2 = new ArrayList<>();
        l2.add(l);
        map.put("1", l2);
        return map;
    }
    
    public Map<String, List<List<Double>>> getAbnormWithHolesCatValMap() {
        Map<String, List<List<Double>>> map = new HashMap<>();
        List<Double> l = Arrays.asList(new Double[] { 1.0, null, 10.0, 1.0 } );
        List<List<Double>> l2 = new ArrayList<>();
        l2.add(l);
        map.put("1", l2);
        return map;
    }
    
    public List<String> getStockDates(LocalDate startdate, int num, boolean forward) {
        List<String> list = new ArrayList<>();
        // TODO weekday setting from config
        LocalDate date = startdate;
        while (num > 0) {
            if (TimeUtil.isWeekday(TimeUtil.convertDate(date))) {
                list.add(TimeUtil.convertDate2(date));
                num--;
            }
            if (forward) {
                date = date.plusDays(1);
            } else {
                date = date.minusDays(1);
            }
        }
        Collections.sort(list);
        return list;
    }
    
    public List<String> getStockDates(LocalDate startdate, LocalDate enddate) {
        List<String> list = new ArrayList<>();
        // TODO weekday setting from config
        for (LocalDate date = startdate; date.isBefore(enddate); date = date.plusDays(1)) {
            if (TimeUtil.isWeekday(TimeUtil.convertDate(date))) {
                list.add(TimeUtil.convertDate2(date));
            }
        }
        Collections.sort(list);
        return list;
    }
}
