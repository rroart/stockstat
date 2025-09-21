package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class TensorflowLIRConfig extends TensorflowFeedConfig {

    @JsonCreator
    public TensorflowLIRConfig(
            @JsonProperty("steps") int steps,
            @JsonProperty("lr") double lr,
            @JsonProperty("inputdropout") double inputdropout,
            @JsonProperty("dropout") double dropout,
            @JsonProperty("normalize") boolean normalize, 
            @JsonProperty("batchnormalize") boolean batchnormalize, 
            @JsonProperty("regularize") boolean regularize,
            @JsonProperty("batchsize") int batchsize,
            @JsonProperty("loss") String loss,
            @JsonProperty("optimizer") String optimizer,
            @JsonProperty("activation") String activation,
            @JsonProperty("lastactivation") String lastactivation) {
        super(MLConstants.LIR, new TensorflowConfigCommon(steps, lr, inputdropout, dropout, normalize, batchnormalize, regularize, batchsize, loss, optimizer, activation, lastactivation), 0, 0);
    }

    public TensorflowLIRConfig(String name) {
        super(name);
    }

    public TensorflowLIRConfig(TensorflowLIRConfig config) {
        this(config.tensorflowConfigCommon);
    }

    public TensorflowLIRConfig() {
        super();
        // JSON
    }

    public TensorflowLIRConfig(TensorflowConfigCommon tensorflowConfigCommon) {
        super(MLConstants.LIR, tensorflowConfigCommon, 0, 0);
    }
}
