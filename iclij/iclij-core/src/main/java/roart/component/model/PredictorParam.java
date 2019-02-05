package roart.component.model;

import java.util.List;
import java.util.Map;

public class PredictorParam extends ComponentParam {
    private int futuredays;
    
    private Map<String, List<Double>> resultMap;

    public int getFuturedays() {
        return futuredays;
    }

    public void setFuturedays(int futuredays) {
        this.futuredays = futuredays;
    }

    public Map<String, List<Double>> getResultMap() {
        return resultMap;
    }

    public void setResultMap(Map<String, List<Double>> resultMap) {
        this.resultMap = resultMap;
    }

}
