package roart.common.ml;

import roart.common.config.MLConstants;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

public class PytorchMLPConfig extends PytorchFeedConfig {

    @JsonCreator
    public PytorchMLPConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("lr") double lr,
            @JsonProperty("dropout") double dropout,
            @JsonProperty("normalize") boolean normalize, 
            @JsonProperty("batchnormalize") boolean batchnormalize, 
            @JsonProperty("regularize") boolean regularize,           
            @JsonProperty("layers") int layers, 
            @JsonProperty("hidden") int hidden) {
        super(MLConstants.MLP, steps, lr, dropout, normalize, batchnormalize, regularize, layers, hidden);
    }

    public PytorchMLPConfig(PytorchMLPConfig config) {
        this(config.steps, config.lr, config.dropout, config.normalize, config.batchnormalize, config.regularize, config.layers, config.hidden);
    }

    public PytorchMLPConfig(String name) {
        super(name);
    }

    public PytorchMLPConfig() {
        super();
        // JSON
    }

}
