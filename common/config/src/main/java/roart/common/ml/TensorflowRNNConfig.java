package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class TensorflowRNNConfig extends TensorflowRecurrentConfig {

    @JsonCreator
    public TensorflowRNNConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("layers") int layers, 
            @JsonProperty("hidden") int hidden, 
            @JsonProperty("lr") double lr, 
            @JsonProperty("slide") int slide, 
            @JsonProperty("dropout") double dropout, 
            @JsonProperty("dropoutin") double dropoutin) {
        super(MLConstants.RNN, steps, layers, hidden, lr, slide, dropout, dropoutin);
    }

    public TensorflowRNNConfig(String name) {
        super(name);
    }

    public TensorflowRNNConfig(TensorflowRNNConfig config) {
        this(config.steps, config.layers, config.hidden, config.lr, config.slide, config.getDropout(), config.dropoutin);
    }

    public TensorflowRNNConfig() {
        super();
        // JSON
    }

}
