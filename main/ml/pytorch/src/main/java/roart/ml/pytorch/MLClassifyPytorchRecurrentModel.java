package roart.ml.pytorch;

import roart.common.config.MyMyConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.model.LearnTestClassify;

public abstract class MLClassifyPytorchRecurrentModel extends MLClassifyPytorchModel {

    public MLClassifyPytorchRecurrentModel(MyMyConfig conf) {
        super(conf);
    }

    public boolean isPredictor() {
        return true;
    }

    public boolean isOneDimensional() {
        return false;
    }

}
