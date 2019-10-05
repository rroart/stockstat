package roart.common.ml;

public abstract class PytorchPreFeedConfig extends PytorchConfig {

    public PytorchPreFeedConfig(String name, int steps) {
        super(name, steps);
    }

    public PytorchPreFeedConfig(String name) {
        super(name);
    }

}
