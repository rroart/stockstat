package roart.ml.tensorflow;

import roart.common.config.MyMyConfig;

public abstract class MLClassifyTensorflowRecurrentModel extends MLClassifyTensorflowModel {

    public MLClassifyTensorflowRecurrentModel(MyMyConfig conf) {
        super(conf);
    }

    public boolean isPredictor() {
        return true;
    }

    public boolean isOneDimensional() {
        return false;
    }

}
