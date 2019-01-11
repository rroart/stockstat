package roart.common.ml;

public abstract class TensorflowConfig extends NNConfig {

    public static final int MAX_STEPS = 200;
    public static final int MAX_HIDDENLAYERS = 4;
    public static final int MIN_NODE = 3;
    public static final int MAX_NODE = 50;
    public static final int MIN_TOL = 1;
    public static final int MAX_TOL = 8;

    public TensorflowConfig(String name) {
        super(name);
    }

}
