package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class TensorflowMLPConfig extends TensorflowFeedConfig {

    @JsonCreator
    public TensorflowMLPConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("layers") int layers, 
            @JsonProperty("hidden") int hidden, 
            @JsonProperty("lr") double lr) {
        super(MLConstants.MLP, steps, layers, hidden, lr);
    }

    public TensorflowMLPConfig(String name) {
        super(name);
    }

    public TensorflowMLPConfig(TensorflowMLPConfig config) {
        this(config.steps, config.layers, config.hidden, config.lr);
    }

    public TensorflowMLPConfig() {
        super();
        // JSON
    }

}
