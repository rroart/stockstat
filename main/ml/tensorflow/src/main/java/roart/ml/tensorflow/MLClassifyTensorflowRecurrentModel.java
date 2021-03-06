package roart.ml.tensorflow;

import roart.common.config.MyMyConfig;

public abstract class MLClassifyTensorflowRecurrentModel extends MLClassifyTensorflowModel {

    public MLClassifyTensorflowRecurrentModel(MyMyConfig conf) {
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
