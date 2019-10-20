package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class PytorchCNNConfig extends PytorchPreFeedConfig {

    private int kernelsize;
    
    private int stride;
    
    private double lr;
    
    public int getKernelsize() {
        return kernelsize;
    }

    public void setKernelsize(int kernelsize) {
        this.kernelsize = kernelsize;
    }

    public int getStride() {
        return stride;
    }

    public void setStride(int stride) {
        this.stride = stride;
    }

    public double getLr() {
        return lr;
    }

    public void setLr(double lr) {
        this.lr = lr;
    }

    @JsonCreator
    public PytorchCNNConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("kernelsize") int kernelsize, 
            @JsonProperty("stride") int stride, 
            @JsonProperty("lr") double lr) {
        super(MLConstants.CNN, steps);
        this.kernelsize = kernelsize;
        this.stride = stride;
        this.lr = lr;
    }
    
    public PytorchCNNConfig(PytorchCNNConfig config) {
        this(config.steps, config.kernelsize, config.stride, config.lr);
    }

    public PytorchCNNConfig(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return super.toString() + " " + kernelsize + " " + stride;
    }
}
