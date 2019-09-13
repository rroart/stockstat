package roart.common.ml;

import roart.common.config.MLConstants;

public class TensorflowGRUConfig extends TensorflowRecurrentConfig {

    public TensorflowGRUConfig(int steps, int layers, int hidden, double lr, int slide) {
        super(MLConstants.GRU, steps, layers, hidden, lr, slide);
    }

}
