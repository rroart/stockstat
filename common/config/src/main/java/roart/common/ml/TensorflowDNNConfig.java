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
            @JsonProperty("lr") double lr,
            @JsonProperty("dropout") double dropout,
            @JsonProperty("normalize") boolean normalize, 
            @JsonProperty("batchnormalize") boolean batchnormalize, 
            @JsonProperty("regularize") boolean regularize,           
            @JsonProperty("layers") int layers, 
            @JsonProperty("hidden") int hidden) {
        super(MLConstants.DNN, steps, lr, dropout, normalize, batchnormalize, regularize, layers, hidden);
        this.layers = layers;
        this.hidden = hidden;
    }

    public TensorflowDNNConfig() {
        super(MLConstants.DNN);
    }

    public TensorflowDNNConfig(TensorflowDNNConfig config) {
        this(config.steps, config.lr, config.dropout, config.normalize, config.batchnormalize, config.regularize, config.layers, config.hidden);
    }
    
    @Override
    public String toString() {
        return super.toString() + " " + layers + " " + hidden;
    }
}
