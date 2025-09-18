package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class TensorflowLICConfig extends TensorflowFeedConfig {

    @JsonCreator
    public TensorflowLICConfig(
            @JsonProperty("steps") Integer steps,
            @JsonProperty("lr") double lr,
            @JsonProperty("dropout") double dropout,
            @JsonProperty("normalize") boolean normalize, 
            @JsonProperty("batchnormalize") boolean batchnormalize, 
            @JsonProperty("regularize") boolean regularize) {
        super(MLConstants.LIC, steps, lr, dropout, normalize, batchnormalize, regularize, 0, 0);
    }

    public TensorflowLICConfig(String name) {
        super(name);
    }

    public TensorflowLICConfig(TensorflowLICConfig config) {
        this(config.steps, config.lr, config.dropout, config.normalize, config.batchnormalize, config.regularize);
    }


    public TensorflowLICConfig() {
        super();
        // JSON
    }
}
