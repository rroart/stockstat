package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = TensorflowMLPConfig.class, name = "TensorflowMLPConfig"),
    @Type(value = TensorflowDNNConfig.class, name = "TensorflowDNNConfig"),
    @Type(value = TensorflowLICConfig.class, name = "TensorflowLICConfig"),
    @Type(value = TensorflowLIRConfig.class, name = "TensorflowLIRConfig"),
    @Type(value = TensorflowRecurrentConfig.class, name = "TensorflowRecurrentConfig") })  
public abstract class TensorflowFeedConfig extends TensorflowConfig {

    protected int layers;
    
    protected int hidden;
    
    public int getLayers() {
        return layers;
    }

    public void setLayers(int layers) {
        this.layers = layers;
    }

    public int getHidden() {
        return hidden;
    }

    public void setHidden(int hidden) {
        this.hidden = hidden;
    }

    public TensorflowFeedConfig(String name, TensorflowConfigCommon tensorflowConfigCommon, int layers, int hidden) {
        super(name, tensorflowConfigCommon);
        this.layers = layers;
        this.hidden = hidden;
    }

    public TensorflowFeedConfig(String name) {
        super(name);
    }

    public TensorflowFeedConfig() {
        super();
        // JSON
    }

    @Override
    public String toString() {
        return super.toString() + " " + layers + " " + hidden;
    }
}
