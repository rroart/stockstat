package roart.common.ml;

import java.util.concurrent.ThreadLocalRandom;

public abstract class SparkConfig extends NeuralNetConfig {

    public static final int MAX_ITER = 200;
    public static final int MAX_LAYERS = 5;
    public static final int MIN_NODE = 3;
    public static final int MAX_NODE = 50;
    public static final int MIN_TOL = 1;
    public static final int MAX_TOL = 8;
    
    public SparkConfig(String name) {
        super(name);
    }

    public Double generateTol() {
	return Math.pow(0.1, ThreadLocalRandom.current().nextInt(MIN_TOL, MAX_TOL + 1));
    }
    
}
