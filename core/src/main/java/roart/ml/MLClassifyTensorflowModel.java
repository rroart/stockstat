package roart.ml;

public abstract class MLClassifyTensorflowModel extends MLClassifyModel {
    @Override
    public String getEngineName() {
        return "Tensorflow";
    }
}
