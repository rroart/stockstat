package roart.common.ml;

import roart.common.config.MLConstants;

public class TensorflowLSTMConfig extends TensorflowRecurrentConfig {

    public TensorflowLSTMConfig(int steps, int layers, int hidden, double lr, int slide) {
        super(MLConstants.LSTM, steps, layers, hidden, lr, slide);
    }

}
