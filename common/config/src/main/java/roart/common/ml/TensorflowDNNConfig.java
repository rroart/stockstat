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
            @JsonProperty("lr") Double lr,
            @JsonProperty("inputdropout") double inputdropout,
            @JsonProperty("dropout") double dropout,
            @JsonProperty("normalize") boolean normalize, 
            @JsonProperty("batchnormalize") boolean batchnormalize, 
            @JsonProperty("regularize") boolean regularize,           
            @JsonProperty("batchsize") int batchsize,
            @JsonProperty("loss") String loss,
            @JsonProperty("optimizer") String optimizer,
            @JsonProperty("activation") String activation,
            @JsonProperty("lastactivation") String lastactivation,
            @JsonProperty("layers") int layers, 
            @JsonProperty("hidden") int hidden,
            @JsonProperty("binary") boolean binary) {
        super(MLConstants.DNN, new TensorflowConfigCommon(steps, lr, inputdropout, dropout, normalize, batchnormalize, regularize, batchsize, loss, optimizer, activation, lastactivation, binary), 0, 0);
        this.layers = layers;
        this.hidden = hidden;
    }

    public TensorflowDNNConfig() {
        super(MLConstants.DNN);
    }

    public TensorflowDNNConfig(TensorflowDNNConfig config) {
        super(MLConstants.DNN, config.tensorflowConfigCommon, 0, 0);
    }
    
    @Override
    public String toString() {
        return super.toString() + " " + layers + " " + hidden;
    }
}
