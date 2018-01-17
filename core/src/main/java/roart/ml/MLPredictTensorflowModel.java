package roart.ml;

public abstract class MLPredictTensorflowModel extends MLPredictModel {
    @Override
    public String getEngineName() {
        return "Tensorflow";
    }
}
