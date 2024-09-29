package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class TensorflowDCGANConfig extends TensorflowGANConfig {

    @JsonCreator
    public TensorflowDCGANConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("lr") double lr) {
        super(MLConstants.DCGAN, steps, lr);
    }

    public TensorflowDCGANConfig(String name) {
        super(name);
    }

    public TensorflowDCGANConfig(TensorflowConditionalGANConfig config) {
        this(config.steps, config.lr);
    }

    public TensorflowDCGANConfig() {
        super();
        // JSON
    }
}
