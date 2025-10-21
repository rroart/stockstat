package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = TensorflowCNNConfig.class, name = "TensorflowCNNConfig"),
    @Type(value = TensorflowCNN2Config.class, name = "TensorflowCNN2Config") })  
public abstract class TensorflowPreFeedConfig extends TensorflowConfig {

    protected int convlayers;

    protected int layers;

    protected int hidden;
    
    public TensorflowPreFeedConfig(String name, TensorflowConfigCommon tensorflowConfigCommon, int convlayers, int layers, int hidden) {
        super(name, tensorflowConfigCommon);
    }

    public TensorflowPreFeedConfig(String name) {
        super(name);
    }

    public TensorflowPreFeedConfig() {
        super();
        // JSON
    }

    public int getConvlayers() {
        return convlayers;
    }

    public void setConvlayers(int convlayers) {
        this.convlayers = convlayers;
    }

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

    @Override
    public String toString() {
        return super.toString() + " " + convlayers + " " + layers + " " + hidden;
    }
}
