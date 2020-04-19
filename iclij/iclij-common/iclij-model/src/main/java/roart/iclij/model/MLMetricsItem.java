package roart.iclij.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import roart.common.util.TimeUtil;
import roart.db.model.MLMetrics;

public class MLMetricsItem {
    private LocalDate record;

    private LocalDate date;

    private String market;

    private String component;

    private String subcomponent;

    private String localcomponent;

    private Double testAccuracy;

    private Double loss;

    private Double threshold;

    public MLMetricsItem() {
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

    public void save() throws Exception {
        MLMetrics mltest = new MLMetrics();
        mltest.setComponent(getComponent());
        mltest.setDate(TimeUtil.convertDate(getDate()));
        mltest.setMarket(getMarket());
        mltest.setRecord(TimeUtil.convertDate(getRecord()));
        mltest.setSubcomponent(getSubcomponent());
        mltest.setLocalcomponent(getLocalcomponent());
        mltest.setThreshold(getThreshold());
        mltest.setLoss(getLoss());
        mltest.setTestAccuracy(getTestAccuracy());
        mltest.save();
    }

    public static List<MLMetricsItem> getAll(String market, Date startDate, Date endDate) throws Exception {
        List<MLMetrics> configs = MLMetrics.getAll(market, startDate, endDate);
        List<MLMetricsItem> configItems = new ArrayList<>();
        for (MLMetrics config : configs) {
            MLMetricsItem memoryItem = getMLMetricsItem(config);
            configItems.add(memoryItem);
        }
        return configItems;
    }

    public static List<MLMetricsItem> getAll() throws Exception {
        List<MLMetrics> configs = MLMetrics.getAll();
        List<MLMetricsItem> configItems = new ArrayList<>();
        for (MLMetrics config : configs) {
            MLMetricsItem memoryItem = getMLMetricsItem(config);
            configItems.add(memoryItem);
        }
        return configItems;
    }

    private static MLMetricsItem getMLMetricsItem(MLMetrics mltest) {
        MLMetricsItem mltestItem = new MLMetricsItem();
        mltestItem.setDate(TimeUtil.convertDate(mltest.getDate()));
        mltestItem.setComponent(mltest.getComponent());
        mltestItem.setMarket(mltest.getMarket());
        mltestItem.setRecord(TimeUtil.convertDate(mltest.getRecord()));
        mltestItem.setSubcomponent(mltest.getSubcomponent());
        mltestItem.setLocalcomponent(mltest.getLocalcomponent());
        mltestItem.setThreshold(mltest.getThreshold());
        mltestItem.setLoss(mltest.getLoss());
        mltestItem.setTestAccuracy(mltest.getTestAccuracy());
        return mltestItem;
    }
    
    public Boolean moreGeneralThan(MLMetricsItem another) {
        if (market.equals(another.market) && component.equals(another.component) && subcomponent.equals(another.subcomponent)) {
            return localcomponent == null && another.localcomponent != null;
        }
        return null;
    }

    public Boolean olderThan(MLMetricsItem another) {
        if (market.equals(another.market) && component.equals(another.component) && subcomponent.equals(another.subcomponent)) {
            if (localcomponent == null || localcomponent.equals(another.localcomponent)) {
                return getRecord().isBefore(another.record);
            }
        }
        return null;
    }
}
