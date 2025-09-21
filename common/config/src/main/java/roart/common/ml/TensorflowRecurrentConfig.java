package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = TensorflowGRUConfig.class, name = "TensorflowGRUConfig"),
    @Type(value = TensorflowLSTMConfig.class, name = "TensorflowLSTMConfig"),
    @Type(value = TensorflowRNNConfig.class, name = "TensorflowRNNConfig") })  
public abstract class TensorflowRecurrentConfig extends TensorflowFeedConfig {

    protected int slide;
    
    public int getSlide() {
        return slide;
    }

    public void setSlide(int slide) {
        this.slide = slide;
    }

    public TensorflowRecurrentConfig(String name, TensorflowConfigCommon tensorflowConfigCommon, int layers, int hidden, int slide) {
        super(name, tensorflowConfigCommon, layers, hidden);
        this.slide = slide;
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
        return super.toString() + " " + slide;
    }
}
