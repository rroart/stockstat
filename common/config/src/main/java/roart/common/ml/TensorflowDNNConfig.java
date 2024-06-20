package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class TensorflowDNNConfig extends TensorflowFeedConfig {

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
            @JsonProperty("hidden") int hidden,
            @JsonProperty("lr") double lr) {
        super(MLConstants.DNN, steps, layers, hidden, lr);
        this.layers = layers;
        this.hidden = hidden;
    }

    public TensorflowDNNConfig() {
        super(MLConstants.DNN);
    }

    public TensorflowDNNConfig(TensorflowDNNConfig config) {
        this(config.steps, config.layers, config.hidden, config.lr);
    }

    @Override
    public String toString() {
        return super.toString() + " " + layers + " " + hidden;
    }
}
