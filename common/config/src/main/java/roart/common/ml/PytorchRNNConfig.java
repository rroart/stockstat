package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class PytorchRNNConfig extends PytorchRecurrentConfig {

    @JsonCreator
    public PytorchRNNConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("layers") int layers, 
            @JsonProperty("hidden") int hidden, 
            @JsonProperty("lr") double lr, 
            @JsonProperty("slide") int slide) {
        super(MLConstants.RNN, steps, layers, hidden, lr, slide);
    }

    public PytorchRNNConfig(PytorchRNNConfig config) {
        this(config.steps, config.layers, config.hidden, config.lr, config.slide);
    }

    public PytorchRNNConfig(String name) {
        super(name);
    }

    public PytorchRNNConfig() {
        super();
        // JSON
    }

}
