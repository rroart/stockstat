package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class TensorflowConditionalGANConfig extends TensorflowGANConfig {

    @JsonCreator
    public TensorflowConditionalGANConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("lr") double lr) {
        super(MLConstants.DCGAN, steps, lr);
    }

    public TensorflowConditionalGANConfig(String name) {
        super(name);
    }

    public TensorflowConditionalGANConfig(TensorflowConditionalGANConfig config) {
        this(config.steps, config.lr);
    }

    public TensorflowConditionalGANConfig() {
        super();
        // JSON
    }

}
