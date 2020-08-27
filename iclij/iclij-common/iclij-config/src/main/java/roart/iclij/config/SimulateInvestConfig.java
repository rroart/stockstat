package roart.iclij.config;

import java.util.List;

public class SimulateInvestConfig {
    private Boolean confidence;
    
    private Double confidenceValue;
    
    private Integer confidenceFindTimes;
    
    private Boolean stoploss;
    
    private Double stoplossValue;
    
    private Boolean indicatorPure;
    
    private Boolean indicatoreRebase;
    
    private Boolean indicatorReverse;
    
    private Boolean mldate;
    
    private Integer stocks;
    
    private Boolean buyweight;
    
    private Integer interval;
    
    private Integer adviser;
    
    private Integer period;

    private List<String> excludes;
    
    public SimulateInvestConfig() {
        super();
    }

    public Boolean getConfidence() {
        return confidence;
    }

    public void setConfidence(Boolean confidence) {
        this.confidence = confidence;
    }

    public Double getConfidenceValue() {
        return confidenceValue;
    }

    public void setConfidenceValue(Double confidenceValue) {
        this.confidenceValue = confidenceValue;
    }

    public Integer getConfidenceFindTimes() {
        return confidenceFindTimes;
    }

    public void setConfidenceFindTimes(Integer confidenceFindTimes) {
        this.confidenceFindTimes = confidenceFindTimes;
    }

    public Boolean getStoploss() {
        return stoploss;
    }

    public void setStoploss(Boolean stoploss) {
        this.stoploss = stoploss;
    }

    public Double getStoplossValue() {
        return stoplossValue;
    }

    public void setStoplossValue(Double stoplossValue) {
        this.stoplossValue = stoplossValue;
    }

    public Boolean getIndicatorPure() {
        return indicatorPure;
    }

    public void setIndicatorPure(Boolean indicatorPure) {
        this.indicatorPure = indicatorPure;
    }

    public Boolean getIndicatoreRebase() {
        return indicatoreRebase;
    }

    public void setIndicatoreRebase(Boolean indicatoreRebase) {
        this.indicatoreRebase = indicatoreRebase;
    }

    public Boolean getIndicatorReverse() {
        return indicatorReverse;
    }

    public void setIndicatorReverse(Boolean indicatorReverse) {
        this.indicatorReverse = indicatorReverse;
    }

    public Boolean getMldate() {
        return mldate;
    }

    public void setMldate(Boolean mldate) {
        this.mldate = mldate;
    }

    public Integer getStocks() {
        return stocks;
    }

    public void setStocks(Integer stocks) {
        this.stocks = stocks;
    }

    public Boolean getBuyweight() {
        return buyweight;
    }

    public void setBuyweight(Boolean buyweight) {
        this.buyweight = buyweight;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public Integer getAdviser() {
        return adviser;
    }

    public void setAdviser(Integer adviser) {
        this.adviser = adviser;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }
    
}
