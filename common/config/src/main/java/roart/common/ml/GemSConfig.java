package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

import roart.common.config.MLConstants;

public class GemSConfig extends GemConfig {

    @JsonCreator
    public GemSConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("layers") int layers, 
            @JsonProperty("hidden") int hidden, 
            @JsonProperty("lr") double lr) {
        super(MLConstants.S, steps, layers, hidden, lr);
    }

    public GemSConfig(String name) {
        super(name);
    }

    @Override
    public boolean empty() {
        // TODO Auto-generated method stub
        return false;
    }

}
