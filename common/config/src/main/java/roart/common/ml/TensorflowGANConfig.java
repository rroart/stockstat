package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = TensorflowConditionalGANConfig.class, name = "TensorflowConditionalGANConfig"),
    @Type(value = TensorflowDCGANConfig.class, name = "TensorflowDCGanConfig") })  
public abstract class TensorflowGANConfig extends TensorflowConfig {
    protected double lr;

    public TensorflowGANConfig(String name, int steps, double lr) {
        super(name, steps);
        this.lr = lr;
    }

    public TensorflowGANConfig(String name) {
        super(name);
    }

    public TensorflowGANConfig() {
        super();
    }

    public double getLr() {
        return lr;
    }

    public void setLr(double lr) {
        this.lr = lr;
    }

    @Override
    public String toString() {
        return super.toString() + " " + lr;
    }
}
