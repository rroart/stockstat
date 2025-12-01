package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class PytorchLSTMConfig extends PytorchRecurrentConfig {

    @JsonCreator
    public PytorchLSTMConfig(
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
        super(MLConstants.LSTM, new PytorchConfigCommon(steps, lr, inputdropout, dropout, normalize, batchnormalize, regularize, batchsize, loss, optimizer, activation, lastactivation, binary), layers, hidden, slide);
    }

    public PytorchLSTMConfig(PytorchLSTMConfig config) {
        this(MLConstants.LSTM, config.pytorchConfigCommon, config.layers, config.hidden, config.slide);
    }

    public PytorchLSTMConfig(String name) {
        super(name);
    }

    public PytorchLSTMConfig() {
        super();
        // JSON
    }

    public PytorchLSTMConfig(String lstm, PytorchConfigCommon pytorchConfigCommon, int layers, int hidden, int slide) {
        super(lstm, pytorchConfigCommon, layers, hidden, slide);
    }

}
