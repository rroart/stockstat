package roart.action;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import roart.iclij.model.IncDecItem;
import roart.iclij.model.MapList;

public class VerifyProfit {

    public void doVerify(List<IncDecItem> list, int days, boolean increase, Map<String, List<List<Double>>> categoryValueMap, LocalDate date) {
        if (days <= 0) {
            return;
        }
        for (IncDecItem item : list) {
            String id = item.getId();
            List<List<Double>> resultList = categoryValueMap.get(id);
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            List<Double> mainList = resultList.get(0);
            if (mainList != null) {
                Double valFuture = mainList.get(mainList.size() - 1);
                Double valNow = mainList.get(mainList.size() - 1 - days);
                if (valFuture != null && valNow != null) {
                    boolean verified = (increase && valFuture > valNow) ||
                            (!increase && valFuture < valNow);
                    item.setDate(date);
                    item.setVerified(verified);
                    item.setVerificationComment("Change: " + valFuture / valNow + " Old: " + valNow + " New: " + valFuture);
                }
            }
        }
    }

    public double getTrend(int days, Map<String, List<List<Double>>> categoryValueMap) {
        if (days <= 0) {
            return 0;
        }
        int count = 0;
        int inc = 0;
        for (Entry<String, List<List<Double>>> entry : categoryValueMap.entrySet()) {
            List<List<Double>> resultList = entry.getValue();
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            List<Double> mainList = resultList.get(0);
            if (mainList != null) {
                Double valFuture = mainList.get(mainList.size() - 1);
                Double valNow = mainList.get(mainList.size() - 1 - days);
                if (valFuture != null && valNow != null) {
                    if (valFuture > valNow) {
                        inc++;
                    }
                    count++;
                }
            }
        }
        if (count == 0) {
            return 0;
        }
        return ( (double) inc) / count;
    }
}
