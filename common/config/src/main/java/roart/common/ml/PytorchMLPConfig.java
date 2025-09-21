package roart.common.ml;

import roart.common.config.MLConstants;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

public class PytorchMLPConfig extends PytorchFeedConfig {

    @JsonCreator
    public PytorchMLPConfig(
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
            @JsonProperty("lastactivation") String lastactivation,
            @JsonProperty("layers") int layers, 
            @JsonProperty("hidden") int hidden) {
        super(MLConstants.MLP, new PytorchConfigCommon(steps, lr, inputdropout, dropout, normalize, batchnormalize, regularize, batchsize, loss, optimizer, activation, lastactivation), layers, hidden);
    }

    public PytorchMLPConfig(PytorchMLPConfig config) {
        this(config.pytorchConfigCommon, config.layers, config.hidden);
    }

    public PytorchMLPConfig(String name) {
        super(name);
    }

    public PytorchMLPConfig() {
        super();
        // JSON
    }

    public PytorchMLPConfig(PytorchConfigCommon pytorchConfigCommon, int layers, int hidden) {
        super(MLConstants.MLP, pytorchConfigCommon, layers, hidden);
    }

}
