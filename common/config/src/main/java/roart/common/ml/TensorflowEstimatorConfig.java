package roart.common.ml;

public abstract class TensorflowEstimatorConfig extends TensorflowConfig {

    public TensorflowEstimatorConfig(String name, int steps) {
        super(name, steps);
    }

    public TensorflowEstimatorConfig(String name) {
        super(name);
    }

}
