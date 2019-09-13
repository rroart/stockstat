package roart.common.ml;

import roart.common.config.MLConstants;

public class TensorflowMLPConfig extends TensorflowFeedConfig {

    public TensorflowMLPConfig(int steps, int layers, int hidden, double lr) {
        super(MLConstants.MLP, steps, layers, hidden, lr);
    }

}
