package roart.simulate;

import java.util.List;

import roart.common.util.MathUtil;

public class StockHistory {
    private String date;

    private Capital capital;

    private Capital sum;

    private double resultavg;

    private String confidence;

    private List<String> stocks;

    private String trend;

    public StockHistory() {
        
    }
    
    public StockHistory(String date, Capital capital, Capital sum, double resultavg, String confidence,
            List<String> stocks, String trend) {
        super();
        this.date = date;
        this.capital = capital;
        this.sum = sum;
        this.resultavg = resultavg;
        this.confidence = confidence;
        this.stocks = stocks;
        this.trend = trend;
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

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }

    @Override
    public String toString() {
        return date + " " + capital.toString() + " " + sum.toString() + " " + new MathUtil().round(resultavg, 2) + " " + confidence + " " + stocks + " " + trend;
    }
}
