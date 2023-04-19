package roart.common.springdata.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import roart.common.util.TimeUtil;

@Table("mlmetrics")
public class MLMetrics {
    @Id
    private Long dbid;

    private Date record;

    private Date date;

    private String market;

    private String component;

    private String subcomponent;

    private String localcomponent;

    private Double trainaccuracy;

    private Double testaccuracy;

    private Double loss;

    private Double threshold;

    public MLMetrics() {
        super();
    }

    public Date getRecord() {
        return record;
    }

    public void setRecord(Date record) {
        this.record = record;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
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
        return trainaccuracy;
    }

    public void setTrainAccuracy(Double trainAccuracy) {
        this.trainaccuracy = trainAccuracy;
    }

    public Double getTestAccuracy() {
        return testaccuracy;
    }

    public void setTestAccuracy(Double testAccuracy) {
        this.testaccuracy = testAccuracy;
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
        return market + " " + component + " " + subcomponent + " " + localcomponent + " " + testaccuracy + " " + loss + " " + threshold + " " + record + " " + date + "\n"; 
    }
}
