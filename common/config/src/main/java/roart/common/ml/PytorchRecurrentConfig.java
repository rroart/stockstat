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
public abstract class PytorchRecurrentConfig extends PytorchFeedConfig {

    protected int slide;

    public int getSlide() {
        return slide;
    }

    public void setSlide(int slide) {
        this.slide = slide;
    }

    public PytorchRecurrentConfig(String name, int steps, double lr, double dropout, boolean normalize, boolean batchnormalize, boolean regularize, int layers, int hidden, int slide) {
        super(name, steps, lr, dropout, normalize, batchnormalize, regularize, layers, hidden);
        this.slide = slide;
    }
    
    public PytorchRecurrentConfig(String name) {
        super(name);
    }

    public PytorchRecurrentConfig() {
        super();
        // JSON
    }

    @Override
    public String toString() {
        return super.toString() + " " + slide;
    }
}
