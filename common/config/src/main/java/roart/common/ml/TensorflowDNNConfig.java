package roart.common.ml;

import java.util.Arrays;

import roart.common.config.MLConstants;

public class TensorflowDNNConfig extends TensorflowEstimatorConfig {

    private int layers;
    
    private int hidden;

    public int getLayers() {
        return layers;
    }

    public void setLayers(int layers) {
        this.layers = layers;
    }

    public int getHidden() {
        return hidden;
    }

    public void setHidden(int hidden) {
        this.hidden = hidden;
    }

    public TensorflowDNNConfig(int steps, int layers, int hidden) {
        super(MLConstants.DNN, steps);
        this.layers = layers;
        this.hidden = hidden;
    }

    @Override
    public String toString() {
        return super.toString() + " " + layers + " " + hidden;
    }
}
