package roart.common.ml;

import roart.common.config.MLConstants;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

public class PytorchMLPConfig extends PytorchFeedConfig {

    @JsonCreator
    public PytorchMLPConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("layers") int layers, 
            @JsonProperty("hidden") int hidden, 
            @JsonProperty("lr") double lr) {
        super(MLConstants.MLP, steps, layers, hidden, lr);
    }

    public PytorchMLPConfig(PytorchMLPConfig config) {
        this(config.steps, config.layers, config.hidden, config.lr);
    }

    public PytorchMLPConfig(String name) {
        super(name);
    }

}
