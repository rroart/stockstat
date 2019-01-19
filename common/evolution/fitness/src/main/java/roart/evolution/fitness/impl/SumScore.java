package roart.evolution.fitness.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import roart.evolution.fitness.AbstractScore;

public class SumScore extends AbstractScore {
    public double calculateResult(Map<String, List<Double>> resultMap) {
        double result = 0;
        double[] array = calculate(resultMap);
        result += array[0] / array[1];
        return result;
    }
    
    public double[] calculate(Map<String, List<Double>> list) {
        double result = 0;
        for (Entry<String, List<Double>> entry : list.entrySet()) {
            List<Double> resultList = entry.getValue();
            result += resultList.get(0);
        }
        double[] array = new double[2];
        array[0] = result;
        array[1] = result;
        return array;
    }

    @Override
    public String name() {
        return "sum";
    }

}
