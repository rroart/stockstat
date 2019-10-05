package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class PytorchGRUConfig extends PytorchRecurrentConfig {

    @JsonCreator
    public PytorchGRUConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("layers") int layers, 
            @JsonProperty("hidden") int hidden, 
            @JsonProperty("lr") double lr, 
            @JsonProperty("slide") int slide) {
        super(MLConstants.GRU, steps, layers, hidden, lr, slide);
    }

    public PytorchGRUConfig(String name) {
        super(name);
    }

}
