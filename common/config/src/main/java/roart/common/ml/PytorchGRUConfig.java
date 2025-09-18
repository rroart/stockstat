package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class PytorchGRUConfig extends PytorchRecurrentConfig {

    @JsonCreator
    public PytorchGRUConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("lr") double lr, 
            @JsonProperty("dropout") double dropout,
            @JsonProperty("normalize") boolean normalize, 
            @JsonProperty("batchnormalize") boolean batchnormalize, 
            @JsonProperty("regularize") boolean regularize,           
            @JsonProperty("layers") int layers, 
            @JsonProperty("hidden") int hidden, 
            @JsonProperty("slide") int slide) {
        super(MLConstants.GRU, steps, lr, dropout, normalize, batchnormalize, regularize, layers, hidden, slide);
    }

    public PytorchGRUConfig(PytorchGRUConfig config) {
        this(config.steps, config.lr, config.dropout, config.normalize, config.batchnormalize, config.regularize, config.layers, config.hidden, config.slide);
    }

    public PytorchGRUConfig(String name) {
        super(name);
    }

    public PytorchGRUConfig() {
        super();
        // JSON
    }

}
