package roart.ml.model;

import java.util.List;
import java.util.Map;

public class LearnTestPredictResult {
    public Double[] predicted;
    public List<Double[]> predictedlist;
    public List<Double> accuracylist;
    public Double accuracy;
    
    public Map<String, Double[]> predictMap;
    public Map<String, Double> accuracyMap;
}
