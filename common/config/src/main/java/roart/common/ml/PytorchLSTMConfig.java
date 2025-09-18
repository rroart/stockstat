package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class PytorchLSTMConfig extends PytorchRecurrentConfig {

    @JsonCreator
    public PytorchLSTMConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("lr") double lr, 
            @JsonProperty("dropout") double dropout,
            @JsonProperty("normalize") boolean normalize, 
            @JsonProperty("batchnormalize") boolean batchnormalize, 
            @JsonProperty("regularize") boolean regularize,           
            @JsonProperty("layers") int layers, 
            @JsonProperty("hidden") int hidden, 
            @JsonProperty("slide") int slide) {
        super(MLConstants.LSTM, steps, lr, dropout, normalize, batchnormalize, regularize, layers, hidden, slide);
    }

    public PytorchLSTMConfig(PytorchLSTMConfig config) {
        this(config.steps, config.lr, config.dropout, config.normalize, config.batchnormalize, config.regularize, config.layers, config.hidden, config.slide);
    }

    public PytorchLSTMConfig(String name) {
        super(name);
    }

    public PytorchLSTMConfig() {
        super();
        // JSON
    }

}
