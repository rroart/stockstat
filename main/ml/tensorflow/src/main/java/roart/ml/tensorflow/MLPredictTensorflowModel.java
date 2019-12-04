package roart.ml.tensorflow;

import roart.common.config.MyMyConfig;
import roart.ml.model.MLPredictModel;

@Deprecated
public abstract class MLPredictTensorflowModel extends MLPredictModel {
    public MLPredictTensorflowModel(MyMyConfig conf) {
        super(conf);
    }

    @Override
    public String getEngineName() {
        return "Tensorflow";
    }
}
