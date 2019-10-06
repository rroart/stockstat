package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class TensorflowDNNConfig extends TensorflowEstimatorConfig {

    private int layers;
    
    private int hidden;

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

    @JsonCreator
    public TensorflowDNNConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("layers") int layers, 
            @JsonProperty("hidden") int hidden) {
        super(MLConstants.DNN, steps);
        this.layers = layers;
        this.hidden = hidden;
    }

    public TensorflowDNNConfig() {
        super(MLConstants.DNN);
    }

    @Override
    public String toString() {
        return super.toString() + " " + layers + " " + hidden;
    }
}
