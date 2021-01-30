package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = PytorchGRUConfig.class, name = "PytorchGRUConfig"),
    @Type(value = PytorchLSTMConfig.class, name = "PytorchLSTMConfig"),
    @Type(value = PytorchRNNConfig.class, name = "PytorchRNNConfig") })  
public abstract class TensorflowRecurrentConfig extends TensorflowFeedConfig {

    protected int slide;
    
    protected double dropoutin;
    
    protected double dropout;
    
    public int getSlide() {
        return slide;
    }

    public void setSlide(int slide) {
        this.slide = slide;
    }

    public double getDropoutin() {
        return dropoutin;
    }

    public void setDropoutin(double dropoutin) {
        this.dropoutin = dropoutin;
    }

    public double getDropout() {
        return dropout;
    }

    public void setDropout(double dropout) {
        this.dropout = dropout;
    }

    public TensorflowRecurrentConfig(String name, int steps, int layers, int hidden, double lr, int slide, double dropout, double dropoutin) {
        super(name, steps, layers, hidden, lr);
        this.slide = slide;
        this.dropout = dropout;
        this.dropoutin = dropoutin;
    }

    public TensorflowRecurrentConfig(String name) {
        super(name);
    }

    public TensorflowRecurrentConfig() {
        super();
        // JSON
    }

    @Override
    public String toString() {
        return super.toString() + " " + slide + " " + dropout + " " + dropoutin;
    }
}
