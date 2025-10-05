package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class TensorflowLSTMConfig extends TensorflowRecurrentConfig {

    @JsonCreator
    public TensorflowLSTMConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("lr") Double lr, 
            @JsonProperty("inputdropout") double inputdropout, 
            @JsonProperty("dropout") double dropout, 
            @JsonProperty("normalize") boolean normalize,           
            @JsonProperty("batchnormalize") boolean batchnormalize, 
            @JsonProperty("regularize") boolean regularize, 
            @JsonProperty("batchsize") int batchsize,
            @JsonProperty("loss") String loss,
            @JsonProperty("optimizer") String optimizer,
            @JsonProperty("activation") String activation,
            @JsonProperty("lastactivation") String lastactivation,
            @JsonProperty("layers") int layers, 
            @JsonProperty("hidden") int hidden, 
            @JsonProperty("slide") int slide) {
        super(MLConstants.LSTM, new TensorflowConfigCommon(steps, lr, inputdropout, dropout, normalize, batchnormalize, regularize, batchsize, loss, optimizer, activation, lastactivation), layers, hidden, slide);
    }

    public TensorflowLSTMConfig(String name) {
        super(name);
    }

    public TensorflowLSTMConfig(TensorflowLSTMConfig config) {
        this(config.tensorflowConfigCommon, config.layers, config.hidden, config.slide);
    }

    public TensorflowLSTMConfig() {
        super();
        // JSON
    }

    public TensorflowLSTMConfig(TensorflowConfigCommon tensorflowConfigCommon, int layers, int hidden, int slide) {
        super(MLConstants.LSTM, tensorflowConfigCommon, layers, hidden, slide);
    }

}
