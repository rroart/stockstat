package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class TensorflowLSTMConfig extends TensorflowRecurrentConfig {

    @JsonCreator
    public TensorflowLSTMConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("layers") int layers, 
            @JsonProperty("hidden") int hidden, 
            @JsonProperty("lr") double lr, 
            @JsonProperty("slide") int slide, 
            @JsonProperty("dropout") double dropout, 
            @JsonProperty("dropoutin") double dropoutin) {
        super(MLConstants.LSTM, steps, layers, hidden, lr, slide, dropout, dropoutin);
    }

    public TensorflowLSTMConfig(String name) {
        super(name);
    }

    public TensorflowLSTMConfig(TensorflowLSTMConfig config) {
        this(config.steps, config.layers, config.hidden, config.lr, config.slide, config.dropout, config.dropoutin);
    }

}
