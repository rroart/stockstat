package roart.ml.pytorch;

import roart.common.config.MyMyConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.model.LearnTestClassify;

public abstract class MLClassifyPytorchRecurrentModel extends MLClassifyPytorchModel {

    public MLClassifyPytorchRecurrentModel(MyMyConfig conf) {
        super(conf);
    }

    public boolean isPredictorOnly() {
        return true;
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
