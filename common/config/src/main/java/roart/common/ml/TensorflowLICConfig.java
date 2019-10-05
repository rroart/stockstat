package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class TensorflowLICConfig extends TensorflowEstimatorConfig {

    @JsonCreator
    public TensorflowLICConfig(
            @JsonProperty("steps") Integer steps) {
        super(MLConstants.LIC, steps);
     }

    public TensorflowLICConfig(String name) {
        super(name);
    }

}
