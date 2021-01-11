package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

@SuppressWarnings({"squid:S00100", "squid:S00116", "squid:S00117"})
public class GemMMConfig extends GemConfig {

    @JsonCreator
    public GemMMConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("n_layers") int n_layers, 
            @JsonProperty("n_hiddens") int n_hiddens, 
            @JsonProperty("lr") double lr) {
        super(MLConstants.MM, steps, n_layers, n_hiddens, lr);
    }

    public GemMMConfig(GemMMConfig config) {
        this(config.steps, config.n_layers, config.n_hiddens, config.lr);
    }

    public GemMMConfig(String name) {
        super(name);
    }

    public GemMMConfig() {
        super();
        // JSON
    }

    @Override
    public boolean empty() {
        // TODO Auto-generated method stub
        return false;
    }

}
