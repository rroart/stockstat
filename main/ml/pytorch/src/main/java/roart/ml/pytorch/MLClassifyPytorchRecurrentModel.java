package roart.ml.pytorch;

import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.model.LearnTestClassify;

public abstract class MLClassifyPytorchRecurrentModel extends MLClassifyPytorchModel {

    public MLClassifyPytorchRecurrentModel(IclijConfig conf) {
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
