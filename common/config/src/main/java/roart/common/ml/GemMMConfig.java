package roart.common.ml;

import roart.common.config.MLConstants;

public class GemMMConfig extends GemConfig {

    public GemMMConfig(int steps, int layers, int hidden, double lr) {
        super(MLConstants.MM, steps, layers, hidden, lr);
    }

    @Override
    public boolean empty() {
        // TODO Auto-generated method stub
        return false;
    }

}
