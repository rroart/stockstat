package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class TensorflowConditionalGANConfig extends TensorflowGANConfig {

    @JsonCreator
    public TensorflowConditionalGANConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("lr") double lr,
            @JsonProperty("dropout") double dropout,
            @JsonProperty("normalize") boolean normalize, 
            @JsonProperty("batchnormalize") boolean batchnormalize, 
            @JsonProperty("regularize") boolean regularize) {
        super(MLConstants.DCGAN, steps, lr, dropout, normalize, batchnormalize, regularize);
    }

    public TensorflowConditionalGANConfig(String name) {
        super(name);
    }

    public TensorflowConditionalGANConfig(TensorflowConditionalGANConfig config) {
        this(config.steps, config.lr, config.dropout, config.normalize, config.batchnormalize, config.regularize);
    }

    public TensorflowConditionalGANConfig() {
        super();
        // JSON
    }

}
