package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

import roart.common.config.MLConstants;

public class TensorflowGRUConfig extends TensorflowRecurrentConfig {

    @JsonCreator
    public TensorflowGRUConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("lr") double lr, 
            @JsonProperty("normalize") boolean normalize, 
            @JsonProperty("batchnormalize") boolean batchnormalize, 
            @JsonProperty("regularize") boolean regularize,           
            @JsonProperty("layers") int layers, 
            @JsonProperty("hidden") int hidden, 
            @JsonProperty("slide") int slide, 
            @JsonProperty("dropout") double dropout, 
            @JsonProperty("dropoutin") double dropoutin) {
        super(MLConstants.GRU, steps, lr, dropout, normalize, batchnormalize, regularize, layers, hidden, slide, dropoutin);
    }

    public TensorflowGRUConfig(String name) {
        super(name);
    }

    public TensorflowGRUConfig(TensorflowGRUConfig config) {
        this(config.steps, config.lr, config.normalize, config.batchnormalize, config.regularize, config.layers, config.hidden, config.slide, config.dropout, config.dropoutin);
    }

    public TensorflowGRUConfig() {
        super();
        // JSON
    }

}
