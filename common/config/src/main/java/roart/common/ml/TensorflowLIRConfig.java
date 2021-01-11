package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class TensorflowLIRConfig extends TensorflowEstimatorConfig {

    @JsonCreator
    public TensorflowLIRConfig(
            @JsonProperty("steps") int steps) {
        super(MLConstants.LIR, steps);
    }

    public TensorflowLIRConfig(String name) {
        super(name);
    }

    public TensorflowLIRConfig(TensorflowLIRConfig config) {
        this(config.steps);
    }

    public TensorflowLIRConfig() {
        super();
        // JSON
    }
}
