package roart.ml;

import roart.config.MLConstants;

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
