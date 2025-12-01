package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class TensorflowConditionalGANConfig extends TensorflowGANConfig {

    @JsonCreator
    public TensorflowConditionalGANConfig(
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
            @JsonProperty("binary") boolean binary) {
        super(MLConstants.DCGAN, new TensorflowConfigCommon(steps, lr, inputdropout, dropout, normalize, batchnormalize, regularize, batchsize, loss, optimizer, activation, lastactivation, binary));
    }

    public TensorflowConditionalGANConfig(String name) {
        super(name);
    }

    public TensorflowConditionalGANConfig(TensorflowConditionalGANConfig config) {
        super(MLConstants.DCGAN, config.tensorflowConfigCommon);
    }

    public TensorflowConditionalGANConfig() {
        super();
        // JSON
    }

}
