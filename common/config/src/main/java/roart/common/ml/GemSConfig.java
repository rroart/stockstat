package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

import roart.common.config.MLConstants;

@SuppressWarnings({"squid:S00100", "squid:S00116", "squid:S00117"})
public class GemSConfig extends GemConfig {

    @JsonCreator
    public GemSConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("n_layers") int n_layers, 
            @JsonProperty("n_hiddens") int n_hiddens, 
            @JsonProperty("lr") double lr) {
        super(MLConstants.S, steps, n_layers, n_hiddens, lr);
    }

    public GemSConfig(GemSConfig config) {
        this(config.steps, config.n_layers, config.n_hiddens, config.lr);
    }

    public GemSConfig(String name) {
        super(name);
    }

    public GemSConfig() {
        super();
        // JSON
    }

    @Override
    public boolean empty() {
        // TODO Auto-generated method stub
        return false;
    }

}
