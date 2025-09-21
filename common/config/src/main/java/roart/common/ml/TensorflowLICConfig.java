package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class TensorflowLICConfig extends TensorflowFeedConfig {

    @JsonCreator
    public TensorflowLICConfig(
            @JsonProperty("steps") Integer steps,
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
        super(MLConstants.LIC, new TensorflowConfigCommon(steps, lr, inputdropout, dropout, normalize, batchnormalize, regularize, batchsize, loss, optimizer, activation, lastactivation), 0, 0);
    }

    public TensorflowLICConfig(String name) {
        super(name);
    }

    public TensorflowLICConfig(TensorflowLICConfig config) {
        this(config.tensorflowConfigCommon);
    }


    public TensorflowLICConfig() {
        super();
        // JSON
    }

    public TensorflowLICConfig(TensorflowConfigCommon tensorflowConfigCommon) {
        super(MLConstants.LIC, tensorflowConfigCommon, 0, 0);
    }
}
