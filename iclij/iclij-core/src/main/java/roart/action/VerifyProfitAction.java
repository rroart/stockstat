package roart.action;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import roart.model.IncDecItem;
import roart.model.MapList;

public class VerifyProfitAction extends Action {

    @Override
    public void goal(Action parent) throws InterruptedException {
    }

    public List<MapList> doVerify(List<IncDecItem> list, int days, boolean increase, Map<String, List<List<Double>>> categoryValueMap, LocalDate record) {
        List<MapList> mapList = new ArrayList<>();
        if (days <= 0) {
            return mapList;
        }
        for (IncDecItem item : list) {
            String id = item.getId();
            List<List<Double>> resultList = categoryValueMap.get(id);
            List<Double> mainList = resultList.get(0);
            if (mainList != null) {
                Double valFuture = mainList.get(mainList.size() - 1);
                Double valNow = mainList.get(mainList.size() - 1 - days);
                if (valFuture != null && valNow != null) {
                    boolean verified = (increase && valFuture > valNow) ||
                            (!increase && valFuture < valNow);
                    item.setRecord(record);
                    item.setVerified(verified);
                    item.setVerificationComment("Change: " + valFuture / valNow + " Old: " + valNow + " New: " + valFuture);
                }
            }
        }
        return mapList;
    }
}
