package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class TensorflowLIRConfig extends TensorflowFeedConfig {

    @JsonCreator
    public TensorflowLIRConfig(
            @JsonProperty("steps") int steps,
            @JsonProperty("lr") double lr,
            @JsonProperty("dropout") double dropout,
            @JsonProperty("normalize") boolean normalize, 
            @JsonProperty("batchnormalize") boolean batchnormalize, 
            @JsonProperty("regularize") boolean regularize) {
        super(MLConstants.LIR, steps, lr, dropout, normalize, batchnormalize, regularize, 0, 0);
    }

    public TensorflowLIRConfig(String name) {
        super(name);
    }

    public TensorflowLIRConfig(TensorflowLIRConfig config) {
        this(config.steps, config.lr, config.dropout, config.normalize, config.batchnormalize, config.regularize);
    }

    public TensorflowLIRConfig() {
        super();
        // JSON
    }
}
