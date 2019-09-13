package roart.common.ml;

import roart.common.config.MLConstants;

public class PytorchMLPConfig extends PytorchFeedConfig {

    public PytorchMLPConfig(int steps, int layers, int hidden, double lr) {
        super(MLConstants.MLP, steps, layers, hidden, lr);
    }

}
