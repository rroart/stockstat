package roart.ml.tensorflow;

import roart.iclij.config.IclijConfig;

public abstract class MLClassifyTensorflowRecurrentModel extends MLClassifyTensorflowModel {

    public MLClassifyTensorflowRecurrentModel(IclijConfig conf) {
        super(conf);
    }

    @Override
    public boolean isTwoDimensional() {
        return false;
    }

    @Override
    public boolean isThreeDimensional() {
        return true;
    }

}
