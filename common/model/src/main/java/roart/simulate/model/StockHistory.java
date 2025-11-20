package roart.simulate.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import roart.common.util.MathUtil;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "_class")
public class StockHistory {
    private String date;

    private Capital capital;

    private Capital sum;

    private double resultavg;

    private String confidence;

    private List<String> stocks;

    private List<String> buy;
    
    private List<String> sell;
    
    private String trend;

    private Integer adviser;
    
    public StockHistory() {
        
    }
    
    public StockHistory(String date, Capital capital, Capital sum, double resultavg, String confidence,
            List<String> stocks, List<String> buy, List<String> sell, String trend, Integer adv) {
        super();
        this.date = date;
        this.capital = capital;
        this.sum = sum;
        this.resultavg = resultavg;
        this.confidence = confidence;
        this.stocks = stocks;
        this.buy = buy;
        this.sell = sell;
        this.trend = trend;
        this.adviser = adv;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Capital getCapital() {
        return capital;
    }

    public void setCapital(Capital capital) {
        this.capital = capital;
    }

    public Capital getSum() {
        return sum;
    }

    public void setSum(Capital sum) {
        this.sum = sum;
    }

    public double getResultavg() {
        return resultavg;
    }

    public void setResultavg(double resultavg) {
        this.resultavg = resultavg;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public List<String> getStocks() {
        return stocks;
    }

    public void setStocks(List<String> stocks) {
        this.stocks = stocks;
    }

    public List<String> getBuy() {
        return buy;
    }

    public void setBuy(List<String> buy) {
        this.buy = buy;
    }

    public List<String> getSell() {
        return sell;
    }

    public void setSell(List<String> sell) {
        this.sell = sell;
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }

    public Integer getAdviser() {
        return adviser;
    }

    public void setAdviser(Integer adviser) {
        this.adviser = adviser;
    }

    @Override
    public String toString() {
        String adv = adviser != null ? " Adv" + adviser : "";
        return date + " " + capital.toString() + " " + sum.toString() + " " + new MathUtil().round(resultavg, 2) + " " + confidence + " " + stocks + " " + buy + " " + sell + " " + trend + adv;
    }
}
