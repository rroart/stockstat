package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class TensorflowMLPConfig extends TensorflowFeedConfig {

    @JsonCreator
    public TensorflowMLPConfig(
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
            @JsonProperty("hidden") int hidden) { 
        super(MLConstants.MLP, new TensorflowConfigCommon(steps, lr, inputdropout, dropout, normalize, batchnormalize, regularize, batchsize, loss, optimizer, activation, lastactivation), layers, hidden);
    }

    public TensorflowMLPConfig(String name) {
        super(name);
    }

    public TensorflowMLPConfig(TensorflowMLPConfig config) {
        this(config.tensorflowConfigCommon, config.layers, config.hidden );
    }

    public TensorflowMLPConfig() {
        super();
        // JSON
    }

    public TensorflowMLPConfig(TensorflowConfigCommon tensorflowConfigCommon, int layers, int hidden) {
        super(MLConstants.MLP, tensorflowConfigCommon, layers, hidden);
    }

}
