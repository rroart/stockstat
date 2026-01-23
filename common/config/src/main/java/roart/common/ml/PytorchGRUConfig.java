package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class PytorchGRUConfig extends PytorchRecurrentConfig {

    @JsonCreator
    public PytorchGRUConfig(
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
            @JsonProperty("slide") int slide,
            @JsonProperty("binary") boolean binary) {
        super(MLConstants.GRU, new PytorchConfigCommon(steps, lr, inputdropout, dropout, normalize, batchnormalize, regularize, batchsize, loss, optimizer, activation, lastactivation, binary), layers, hidden, slide);
    }

    public PytorchGRUConfig(PytorchGRUConfig config) {
        this(new PytorchConfigCommon(config.pytorchConfigCommon), config.layers, config.hidden, config.slide);
    }

    public PytorchGRUConfig(String name) {
        super(name);
    }

    public PytorchGRUConfig() {
        super();
        // JSON
    }

    public PytorchGRUConfig(PytorchConfigCommon pytorchConfigCommon, int layers, int hidden, int slide) {
        super(MLConstants.GRU, pytorchConfigCommon, layers, hidden, slide);
    }

}
