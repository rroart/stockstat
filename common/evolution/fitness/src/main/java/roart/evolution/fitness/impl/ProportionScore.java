package roart.evolution.fitness.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import roart.evolution.fitness.AbstractScore;

public class ProportionScore extends AbstractScore {
    Boolean incdec;
    
    public ProportionScore() {
        
    }
    
    public ProportionScore(Boolean incdec) {
        this.incdec = incdec;
    }
    
    public double calculateResult(Map<String, List<Double>> resultMap, Double threshold) {
        double result = 0;
        double[] array = calculate(resultMap, threshold);
        result += array[0] / array[1];
        return result;
    }

    @Override
    public double[] calculate(Map<String, List<Double>> resultMap, Double threshold) {
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
            int jj = 0;
        }
        Double buyMax = buyMaxOpt.get();
        Optional<Double> buyMinOpt = scoreSet.parallelStream().reduce(Double::min);
        if (!buyMinOpt.isPresent()) {
            int jj = 0;
        }
        Double scoreMin = buyMinOpt.get();
        Double diffScore = buyMax - scoreMin;
        //log.info("Myexpect0 " + minChange + " " + diffChange + " " + diffBuy + " " + maxChange + " " + minChange + " " + buyMax + " " + buyMin);
        for (Entry<String, List<Double>> entry : resultMap.entrySet()) {
            List<Double> resultList = entry.getValue();
            Double score = resultList.get(0);
            Double change = resultList.get(1);
            if (score != null) {
                //double delta = change / buy * (buyMax / maxChange);
                //double expectedChange = minChange + diffChange * (score - scoreMin) / diffScore;
                //log.info("Myexpect " + expectedChange + " " + change + " " + valf + " " + valn + " " + buy + " " + key);
                //double delta = expectedChange / change;
                if (incdec == null) {
                    totalBuy++;
                    double confidence = 1 - Math.abs((score - scoreMin)/ diffScore - (change - minChange) / diffChange) ;
                    goodBuy += confidence;
                } else {
                    if (incdec && change > threshold) {
                        totalBuy++;
                        double confidence = 1 - Math.abs((score - scoreMin)/ diffScore - (change - minChange) / diffChange) ;
                        goodBuy += confidence;                        
                    }
                    if (!incdec && change < threshold) {
                        totalBuy++;
                        double confidence = 1 - Math.abs((score - scoreMin)/ diffScore - (change - minChange) / diffChange) ;
                        goodBuy += confidence;                        
                    }
                }
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

