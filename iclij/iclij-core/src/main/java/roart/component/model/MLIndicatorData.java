package roart.component.model;

import java.util.List;
import java.util.Map;

import roart.result.model.ResultMeta;

public class MLIndicatorData extends ComponentMLData {
    
    private double threshold;
    
    public MLIndicatorData(ComponentData componentparam) {
        super(componentparam);
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

}
