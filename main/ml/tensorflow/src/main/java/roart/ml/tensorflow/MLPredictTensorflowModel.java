package roart.ml.tensorflow;

import roart.iclij.config.IclijConfig;
import roart.ml.model.MLPredictModel;

@Deprecated
public abstract class MLPredictTensorflowModel extends MLPredictModel {
    public MLPredictTensorflowModel(IclijConfig conf) {
        super(conf);
    }

    @Override
    public String getEngineName() {
        return "Tensorflow";
    }
}
