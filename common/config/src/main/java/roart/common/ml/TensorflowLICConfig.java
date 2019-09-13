package roart.common.ml;

import roart.common.config.MLConstants;

public class TensorflowLICConfig extends TensorflowEstimatorConfig {

    public TensorflowLICConfig(Integer steps) {
        super(MLConstants.LIC, steps);
     }

}
