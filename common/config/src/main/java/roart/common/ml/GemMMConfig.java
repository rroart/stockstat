package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class GemMMConfig extends GemConfig {

    @JsonCreator
    public GemMMConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("layers") int layers, 
            @JsonProperty("hidden") int hidden, 
            @JsonProperty("lr") double lr) {
        super(MLConstants.MM, steps, layers, hidden, lr);
    }

    public GemMMConfig(String name) {
        super(name);
    }

    @Override
    public boolean empty() {
        // TODO Auto-generated method stub
        return false;
    }

}
