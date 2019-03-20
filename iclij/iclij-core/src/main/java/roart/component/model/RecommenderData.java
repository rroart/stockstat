package roart.component.model;

import java.util.List;
import java.util.Map;

public class RecommenderData extends ComponentData {
    
    private Map<String, List<Double>> recommendBuySell;

    public RecommenderData() {
        
    }
    
    public RecommenderData(ComponentData componentparam) {
        super(componentparam);
    }

    public Map<String, List<Double>> getRecommendBuySell() {
        return recommendBuySell;
    }

    public void setRecommendBuySell(Map<String, List<Double>> recommendBuySell) {
        this.recommendBuySell = recommendBuySell;
    }

}
