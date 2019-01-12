package roart.ml.model;

import java.util.Map;

public class LearnTestClassifyResult {
    private Double accuracy;

    private Map<String, Double[]> catMap;
    
    public LearnTestClassifyResult() {
        super();
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Map<String, Double[]> getCatMap() {
        return catMap;
    }

    public void setCatMap(Map<String, Double[]> retMap) {
        this.catMap = retMap;
    }

}
