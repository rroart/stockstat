package roart.ml.tensorflow;

import roart.ml.model.MLPredictModel;

public abstract class MLPredictTensorflowModel extends MLPredictModel {
    @Override
    public String getEngineName() {
        return "Tensorflow";
    }
}
