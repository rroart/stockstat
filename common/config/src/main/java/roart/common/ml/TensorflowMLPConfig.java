package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class TensorflowMLPConfig extends TensorflowFeedConfig {

    @JsonCreator
    public TensorflowMLPConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("lr") double lr,
            @JsonProperty("dropout") double dropout,
            @JsonProperty("normalize") boolean normalize, 
            @JsonProperty("batchnormalize") boolean batchnormalize, 
            @JsonProperty("regularize") boolean regularize,           
            @JsonProperty("layers") int layers, 
            @JsonProperty("hidden") int hidden) { 
        super(MLConstants.MLP, steps, lr, dropout, normalize, batchnormalize, regularize, layers, hidden);
    }

    public TensorflowMLPConfig(String name) {
        super(name);
    }

    public TensorflowMLPConfig(TensorflowMLPConfig config) {
        this(config.steps, config.lr, config.dropout, config.normalize, config.batchnormalize, config.regularize, config.layers, config.hidden );
    }

    public TensorflowMLPConfig() {
        super();
        // JSON
    }

}
