package roart.component.model;

import java.util.List;
import java.util.Map;

import roart.result.model.ResultMeta;

public class MLIndicatorParam extends ComponentParam {
    private int futuredays;
    
    private double threshold;
    
    private Map<String, List<Object>> resultMap;
    
    private List<ResultMeta> resultMeta;

    public int getFuturedays() {
        return futuredays;
    }

    public void setFuturedays(int futuredays) {
        this.futuredays = futuredays;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public Map<String, List<Object>> getResultMap() {
        return resultMap;
    }

    public void setResultMap(Map<String, List<Object>> resultMap) {
        this.resultMap = resultMap;
    }

    public List<ResultMeta> getResultMeta() {
        return resultMeta;
    }

    public void setResultMeta(List<ResultMeta> resultMeta) {
        this.resultMeta = resultMeta;
    }

}
