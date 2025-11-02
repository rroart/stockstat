package roart.ml.model;

import java.util.Map;

public class LearnTestClassifyResult {
    private Double accuracy;

    private Double trainaccuracy;

    private Double valaccuracy;

    private Double loss;

    private Map<String, Double[]> catMap;
    
    private Boolean exists;
    
    private Boolean classify;
    
    public LearnTestClassifyResult() {
        super();
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Double getTrainaccuracy() {
        return trainaccuracy;
    }

    public void setTrainaccuracy(Double trainaccuracy) {
        this.trainaccuracy = trainaccuracy;
    }

    public Double getValaccuracy() {
        return valaccuracy;
    }

    public void setValaccuracy(Double valaccuracy) {
        this.valaccuracy = valaccuracy;
    }

    public Double getLoss() {
        return loss;
    }

    public void setLoss(Double loss) {
        this.loss = loss;
    }

    public Map<String, Double[]> getCatMap() {
        return catMap;
    }

    public void setCatMap(Map<String, Double[]> catMap) {
        this.catMap = catMap;
    }

    public Boolean isExists() {
        return exists;
    }

    public void setExists(Boolean exists) {
        this.exists = exists;
    }

    public Boolean getClassify() {
        return classify;
    }

    public void setClassify(Boolean classify) {
        this.classify = classify;
    }

}
