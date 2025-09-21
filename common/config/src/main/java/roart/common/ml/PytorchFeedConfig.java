package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = PytorchMLPConfig.class, name = "PytorchMLPConfig"),
    @Type(value = PytorchRecurrentConfig.class, name = "PytorchRecurrentConfig") })  
public abstract class PytorchFeedConfig extends PytorchConfig {

    protected int layers;

    protected int hidden;

    public PytorchFeedConfig(String name, PytorchConfigCommon pytorchConfigCommon, int layers, int hidden) {
        super(name, pytorchConfigCommon);
        this.layers = layers;
        this.hidden = hidden;
    }

    public PytorchFeedConfig() {
        super();
        // JSON
    }

    public PytorchFeedConfig(String name) {
        super(name);
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
        return super.toString() + " " + layers + " " + hidden;
    }
}
