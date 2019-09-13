package roart.common.ml;

import roart.common.config.MLConstants;

public class GemSConfig extends GemConfig {

    public GemSConfig(int steps, int layers, int hidden, double lr) {
        super(MLConstants.S, steps, layers, hidden, lr);
    }

    @Override
    public boolean empty() {
        // TODO Auto-generated method stub
        return false;
    }

}
