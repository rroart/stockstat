package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

import roart.common.config.MLConstants;

public class TensorflowGRUConfig extends TensorflowRecurrentConfig {

    @JsonCreator
    public TensorflowGRUConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("layers") int layers, 
            @JsonProperty("hidden") int hidden, 
            @JsonProperty("lr") double lr, 
            @JsonProperty("slide") int slide, 
            @JsonProperty("dropout") double dropout, 
            @JsonProperty("dropoutin") double dropoutin) {
        super(MLConstants.GRU, steps, layers, hidden, lr, slide, dropout, dropoutin);
    }

    public TensorflowGRUConfig(String name) {
        super(name);
    }

    public TensorflowGRUConfig(TensorflowGRUConfig config) {
        this(config.steps, config.layers, config.hidden, config.lr, config.slide, config.dropout, config.dropoutin);
    }

}
