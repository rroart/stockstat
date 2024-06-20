package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class TensorflowLIRConfig extends TensorflowFeedConfig {

    @JsonCreator
    public TensorflowLIRConfig(
            @JsonProperty("steps") int steps,
            @JsonProperty("lr") double lr) {
        super(MLConstants.LIR, steps, 0, 0, lr);
    }

    public TensorflowLIRConfig(String name) {
        super(name);
    }

    public TensorflowLIRConfig(TensorflowLIRConfig config) {
        this(config.steps, config.lr);
    }

    public TensorflowLIRConfig() {
        super();
        // JSON
    }
}
