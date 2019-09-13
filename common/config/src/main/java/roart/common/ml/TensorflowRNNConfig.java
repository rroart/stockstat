package roart.common.ml;

import roart.common.config.MLConstants;

public class TensorflowRNNConfig extends TensorflowRecurrentConfig {

    public TensorflowRNNConfig(int steps, int layers, int hidden, double lr, int slide) {
        super(MLConstants.RNN, steps, layers, hidden, lr, slide);
    }

}
