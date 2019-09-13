package roart.common.ml;

import roart.common.config.MLConstants;

public class PytorchLSTMConfig extends PytorchRecurrentConfig {

    public PytorchLSTMConfig(int steps, int layers, int hidden, double lr, int slide) {
        super(MLConstants.LSTM, steps, layers, hidden, lr, slide);
    }

}
