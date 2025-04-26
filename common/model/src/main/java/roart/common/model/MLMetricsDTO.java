package roart.common.model;

import java.time.LocalDate;

public class MLMetricsDTO {
    private LocalDate record;

    private LocalDate date;

    private String market;

    private String component;

    private String subcomponent;

    private String localcomponent;

    private Double trainAccuracy;

    private Double testAccuracy;

    private Double loss;

    private Double threshold;

    public MLMetricsDTO() {
        super();
    }

    public LocalDate getRecord() {
        return record;
    }

    public void setRecord(LocalDate record) {
        this.record = record;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getSubcomponent() {
        return subcomponent;
    }

    public void setSubcomponent(String subcomponent) {
        this.subcomponent = subcomponent;
    }

    public String getLocalcomponent() {
        return localcomponent;
    }

    public void setLocalcomponent(String localcomponent) {
        this.localcomponent = localcomponent;
    }

    public Double getTrainAccuracy() {
        return trainAccuracy;
    }

    public void setTrainAccuracy(Double trainAccuracy) {
        this.trainAccuracy = trainAccuracy;
    }

    public Double getTestAccuracy() {
        return testAccuracy;
    }

    public void setTestAccuracy(Double testAccuracy) {
        this.testAccuracy = testAccuracy;
    }

    public Double getLoss() {
        return loss;
    }

    public void setLoss(Double loss) {
        this.loss = loss;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    @Override
    public String toString() {
        return market + " " + component + " " + subcomponent + " " + localcomponent + " " + testAccuracy + " " + loss + " " + threshold + " " + record + " " + date + "\n"; 
    }

    public Boolean moreGeneralThan(MLMetricsDTO another) {
        if (market.equals(another.market) && component.equals(another.component) && subcomponent.equals(another.subcomponent)) {
            return localcomponent == null && another.localcomponent != null;
        }
        return null;
    }

    public Boolean olderThan(MLMetricsDTO another) {
        if (market.equals(another.market) && component.equals(another.component) && subcomponent.equals(another.subcomponent)) {
            if (localcomponent == null || localcomponent.equals(another.localcomponent)) {
                return getRecord().isBefore(another.record);
            }
        }
        return null;
    }
}
