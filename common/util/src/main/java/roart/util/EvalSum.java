package roart.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class EvalSum extends EvalUtil {
    public double calculate(Map<String, List<Double>> list) {
        double result = 0;
        for (Entry<String, List<Double>> entry : list.entrySet()) {
            List<Double> resultList = entry.getValue();
            result += resultList.get(0);
        }
        return result;
    }

}
