package roart.common.ml;

import roart.common.config.MLConstants;

public class TensorflowLIRConfig extends TensorflowEstimatorConfig {

    public TensorflowLIRConfig(int steps) {
        super(MLConstants.LIR, steps);
    }

}
