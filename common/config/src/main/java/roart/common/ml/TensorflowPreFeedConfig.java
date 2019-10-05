package roart.common.ml;

public abstract class TensorflowPreFeedConfig extends TensorflowConfig {

    public TensorflowPreFeedConfig(String name, int steps) {
        super(name, steps);
    }

    public TensorflowPreFeedConfig(String name) {
        super(name);
    }

}
