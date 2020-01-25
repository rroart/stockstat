package roart.component.model;

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
