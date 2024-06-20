package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class TensorflowLICConfig extends TensorflowFeedConfig {

    @JsonCreator
    public TensorflowLICConfig(
            @JsonProperty("steps") Integer steps,
            @JsonProperty("lr") double lr) {
        super(MLConstants.LIC, steps, 0, 0, lr);
     }

    public TensorflowLICConfig(String name) {
        super(name);
    }

    public TensorflowLICConfig(TensorflowLICConfig config) {
        this(config.steps, config.lr);
    }


    public TensorflowLICConfig() {
        super();
        // JSON
    }
}
