package roart.iclij.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimulateInvestConfig {
    private Boolean confidence;
    
    private Double confidenceValue;
    
    private Integer confidenceFindTimes;
    
    private Boolean stoploss;
    
    private Double stoplossValue;
    
    private Boolean indicatorPure;
    
    private Boolean indicatorRebase;
    
    private Boolean indicatorReverse;
    
    private Boolean mldate;
    
    private Integer stocks;
    
    private Boolean buyweight;
    
    private Integer interval;
    
    private Integer adviser;
    
    private Integer period;

    private String[] excludes;
    
    private String startdate;
    
    private String enddate;
    
    private Boolean intervalStoploss;
    
    private Double intervalStoplossValue;
    
    private Boolean interpolate;
    
    private Integer day;

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

    public Boolean getIndicatorRebase() {
        return indicatorRebase;
    }

    public void setIndicatorRebase(Boolean indicatorRebase) {
        this.indicatorRebase = indicatorRebase;
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

    public String[] getExcludes() {
        return excludes;
    }

    public void setExcludes(String[] excludes) {
        this.excludes = excludes;
    }

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }
    
    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }

    public Boolean getIntervalStoploss() {
        return intervalStoploss;
    }

    public void setIntervalStoploss(Boolean intervalStoploss) {
        this.intervalStoploss = intervalStoploss;
    }

    public Double getIntervalStoplossValue() {
        return intervalStoplossValue;
    }

    public void setIntervalStoplossValue(Double intervalStoplossValue) {
        this.intervalStoplossValue = intervalStoplossValue;
    }

    public Boolean getInterpolate() {
        return interpolate;
    }

    public void setInterpolate(Boolean interpolate) {
        this.interpolate = interpolate;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public void merge(SimulateInvestConfig other) {
        if (other == null) {
            return;
        }
        if (other.confidence != null) {
            this.confidence = other.confidence;
        }
        if (other.confidenceFindTimes != null) {
            this.confidenceFindTimes = other.confidenceFindTimes;
        }
        if (other.confidenceValue != null) {
            this.confidenceValue = other.confidenceValue;
        }
        if (other.stoplossValue != null) {
            this.stoplossValue = other.stoplossValue;
        }
        if (other.stoploss != null) {
            this.stoploss = other.stoploss;
        }
        if (other.indicatorReverse != null) {
            this.indicatorReverse = other.indicatorReverse;
        }
        if (other.indicatorRebase != null) {
            this.indicatorRebase = other.indicatorRebase;
        }
        if (other.indicatorPure != null) {
            this.indicatorPure = other.indicatorPure;
        }
        if (other.mldate != null) {
            this.mldate = other.mldate;
        }
        if (other.stocks != null) {
            this.stocks = other.stocks;
        }
        if (other.buyweight != null) {
            this.buyweight = other.buyweight;
        }
        if (other.interval != null) {
            this.interval = other.interval;
        }
        if (other.intervalStoplossValue != null) {
            this.intervalStoplossValue = other.intervalStoplossValue;
        }
        if (other.intervalStoploss != null) {
            this.intervalStoploss = other.intervalStoploss;
        }
        if (other.interpolate != null) {
            this.interpolate = other.interpolate;
        }
        if (other.adviser != null) {
            this.adviser = other.adviser;
        }
        if (other.period != null) {
            this.period = other.period;
        }
        if (other.startdate != null) {
            this.startdate = other.startdate;
        }
        if (other.enddate != null) {
            this.enddate = other.enddate;
        }
        if (other.excludes != null) {
            this.excludes = other.excludes;
        }
    }
    
    public Map<String, Object> asMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(IclijConfigConstants.SIMULATEINVESTCONFIDENCE, confidence);
        map.put(IclijConfigConstants.SIMULATEINVESTCONFIDENCEVALUE, confidenceValue);
        map.put(IclijConfigConstants.SIMULATEINVESTCONFIDENCEFINDTIMES, confidenceFindTimes);
        map.put(IclijConfigConstants.SIMULATEINVESTSTOPLOSS, stoploss);
        map.put(IclijConfigConstants.SIMULATEINVESTSTOPLOSSVALUE, stoplossValue);
        map.put(IclijConfigConstants.SIMULATEINVESTINDICATORPURE, indicatorPure);
        map.put(IclijConfigConstants.SIMULATEINVESTINDICATORREBASE, indicatorRebase);
        map.put(IclijConfigConstants.SIMULATEINVESTINDICATORREVERSE, indicatorReverse);
        map.put(IclijConfigConstants.SIMULATEINVESTMLDATE, mldate);
        map.put(IclijConfigConstants.SIMULATEINVESTSTOCKS, stocks);
        map.put(IclijConfigConstants.SIMULATEINVESTBUYWEIGHT, buyweight);
        map.put(IclijConfigConstants.SIMULATEINVESTINTERVAL, interval);
        map.put(IclijConfigConstants.SIMULATEINVESTADVISER, adviser);
        map.put(IclijConfigConstants.SIMULATEINVESTPERIOD, period);
        map.put(IclijConfigConstants.SIMULATEINVESTSTARTDATE, startdate);
        map.put(IclijConfigConstants.SIMULATEINVESTENDDATE, enddate);
        map.put(IclijConfigConstants.SIMULATEINVESTINTERVALSTOPLOSS, intervalStoploss);
        map.put(IclijConfigConstants.SIMULATEINVESTINTERVALSTOPLOSSVALUE, intervalStoplossValue);
        map.put(IclijConfigConstants.SIMULATEINVESTINTERPOLATE, interpolate);
        map.put(IclijConfigConstants.SIMULATEINVESTDAY, day);
        return map;
    }
}
