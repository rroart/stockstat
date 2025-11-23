package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = PytorchCNNConfig.class, name = "PytorchCNNConfig"),
    @Type(value = PytorchCNN2Config.class, name = "PytorchCNN2Config") })  
public abstract class PytorchPreFeedConfig extends PytorchConfig {

    protected int convlayers;

    protected int layers;

    protected int hidden;
    
    public PytorchPreFeedConfig(String name, PytorchConfigCommon pytorchConfigCommon, int convlayers, int layers, int hidden) {
        super(name, pytorchConfigCommon);
        this.convlayers = convlayers;
        this.layers = layers;
        this.hidden = hidden;
    }

    public PytorchPreFeedConfig(String name) {
        super(name);
    }

    public PytorchPreFeedConfig() {
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
