package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class PytorchLSTMConfig extends PytorchRecurrentConfig {

    @JsonCreator
    public PytorchLSTMConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("layers") int layers, 
            @JsonProperty("hidden") int hidden, 
            @JsonProperty("lr") double lr, 
            @JsonProperty("slide") int slide) {
        super(MLConstants.LSTM, steps, layers, hidden, lr, slide);
    }

    public PytorchLSTMConfig(PytorchLSTMConfig config) {
        this(config.steps, config.layers, config.hidden, config.lr, config.slide);
    }

    public PytorchLSTMConfig(String name) {
        super(name);
    }

}
