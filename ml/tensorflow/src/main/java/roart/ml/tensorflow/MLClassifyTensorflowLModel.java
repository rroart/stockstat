package roart.ml.tensorflow;

import roart.common.config.MLConstants;

public class MLClassifyTensorflowLModel  extends MLClassifyTensorflowModel {
    @Override
    public int getId() {
        return 2;
    }
    @Override
    public String getName() {
        return MLConstants.L;
    }
}
