package roart.common.ml;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;

public class SparkMLPCConfig extends SparkConfig {

    private Integer layers;

    private int hidden;

    public SparkMLPCConfig(Integer maxiter, double tol, int layers, int hidden) {
        super(MLConstants.MLPC, maxiter, tol);
        this.layers = layers;
        this.hidden = hidden;
    }

    public Integer getLayers() {
        return layers;
    }

    public void setLayers(Integer layers) {
        this.layers = layers;
    }

    public int getHidden() {
        return hidden;
    }

    public void setHidden(int hidden) {
        this.hidden = hidden;
    }

    @Override
    public String toString() {
        return super.toString() + " " + layers + " " + hidden;
    }

}
