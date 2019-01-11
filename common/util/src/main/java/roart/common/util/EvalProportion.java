package roart.common.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map.Entry;

public class EvalProportion extends EvalUtil {
    public double calculateResult(Map<String, List<Double>> resultMap) {
        double result = 0;
        double[] array = calculate(resultMap);
        result += array[0] / array[1];
        return result;
    }

    public double[] calculate(Map<String, List<Double>> resultMap) {
        double goodBuy = 0;
        long totalBuy = 0;
        List<Double> scoreSet = resultMap.values().stream().map(e -> e.get(0)).collect(Collectors.toList());
        List<Double> changeSet = resultMap.values().stream().map(e -> e.get(1)).collect(Collectors.toList());
        Optional<Double> minOpt = changeSet.parallelStream().reduce(Double::min);
        Double minChange = 0.0;
        if (minOpt.isPresent()) {
            minChange = minOpt.get();
        }
        Optional<Double> maxOpt = changeSet.parallelStream().reduce(Double::max);
        Double maxChange = 0.0;
        if (maxOpt.isPresent()) {
            maxChange = maxOpt.get();
        }
        Double diffChange = maxChange - minChange;
        Optional<Double> buyMaxOpt = scoreSet.parallelStream().reduce(Double::max);
        if (!buyMaxOpt.isPresent()) {
        }
        Double buyMax = buyMaxOpt.get();
        Optional<Double> buyMinOpt = scoreSet.parallelStream().reduce(Double::min);
        if (!buyMinOpt.isPresent()) {
        }
        Double scoreMin = buyMinOpt.get();
        Double diffScore = buyMax - scoreMin;
        //log.info("Myexpect0 " + minChange + " " + diffChange + " " + diffBuy + " " + maxChange + " " + minChange + " " + buyMax + " " + buyMin);
        for (Entry<String, List<Double>> entry : resultMap.entrySet()) {
            List<Double> resultList = entry.getValue();
            Double score = resultList.get(0);
            Double change = resultList.get(1);
            if (score != null) {
                totalBuy++;
                //double delta = change / buy * (buyMax / maxChange);
                //double expectedChange = minChange + diffChange * (score - scoreMin) / diffScore;
                //log.info("Myexpect " + expectedChange + " " + change + " " + valf + " " + valn + " " + buy + " " + key);
                //double delta = expectedChange / change;
                double confidence = 1 - Math.abs((score - scoreMin)/ diffScore - (change - minChange) / diffChange) ;
                goodBuy += confidence;
                //log.info("Eval {} {} {}", entry.getKey(), (score - scoreMin)/ diffScore, (change - minChange) / diffChange);
            }
        }      
        double[] array = new double[2];
        array[0] = goodBuy;
        array[1] = totalBuy;
        return array;
    }

    @Override
    public String name() {
        return "prop";
    }

}

