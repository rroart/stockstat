package roart.common.ml;

import roart.common.config.MLConstants;

public class PytorchGRUConfig extends PytorchRecurrentConfig {

    public PytorchGRUConfig(int steps, int layers, int hidden, double lr, int slide) {
        super(MLConstants.GRU, steps, layers, hidden, lr, slide);
    }

}
