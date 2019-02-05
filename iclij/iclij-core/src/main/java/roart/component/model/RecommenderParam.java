package roart.component.model;

import java.util.List;
import java.util.Map;

public class RecommenderParam extends ComponentParam {
    private int futuredays;
    
    private Map<String, List<Double>> recommendBuySell;

    public int getFuturedays() {
        return futuredays;
    }

    public void setFuturedays(int futuredays) {
        this.futuredays = futuredays;
    }

    public Map<String, List<Double>> getRecommendBuySell() {
        return recommendBuySell;
    }

    public void setRecommendBuySell(Map<String, List<Double>> recommendBuySell) {
        this.recommendBuySell = recommendBuySell;
    }

}
