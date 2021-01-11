package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class PytorchCNN2Config extends PytorchPreFeedConfig {
    private int kernelsize;
    
    private int maxpool;
    
    private int stride;
    
    private double dropout1;
    
    private double dropout2;
    
    private double lr;
    
    @JsonCreator
    public PytorchCNN2Config(
            @JsonProperty("steps") int steps, 
            @JsonProperty("kernelsize") int kernelsize,
            @JsonProperty("maxpool") int maxpool,
            @JsonProperty("stride") int stride, 
            @JsonProperty("dropout1") double dropout1,
            @JsonProperty("dropout2") double dropout2,
            @JsonProperty("lr") double lr) {
        super(MLConstants.CNN2, steps);
        this.kernelsize = kernelsize;
        this.maxpool = maxpool;
        this.stride = stride;
        this.dropout1 = dropout1;
        this.dropout2 = dropout2;
        this.lr = lr;
    }
    
    public PytorchCNN2Config(PytorchCNN2Config config) {
        this(config.steps, config.kernelsize, config.maxpool, config.stride, config.dropout1, config.dropout2, config.lr);
    }

    public PytorchCNN2Config(String name) {
        super(name);
    }

    public PytorchCNN2Config() {
        super();
        // JSON
    }

    public int getKernelsize() {
        return kernelsize;
    }

    public void setKernelsize(int kernelsize) {
        this.kernelsize = kernelsize;
    }

    public int getMaxpool() {
        return maxpool;
    }

    public void setMaxpool(int maxpool) {
        this.maxpool = maxpool;
    }

    public int getStride() {
        return stride;
    }

    public void setStride(int stride) {
        this.stride = stride;
    }

    public double getDropout1() {
        return dropout1;
    }

    public void setDropout1(double dropout1) {
        this.dropout1 = dropout1;
    }

    public double getDropout2() {
        return dropout2;
    }

    public void setDropout2(double dropout2) {
        this.dropout2 = dropout2;
    }

    public double getLr() {
        return lr;
    }

    public void setLr(double lr) {
        this.lr = lr;
    }

    @Override
    public String toString() {
        return super.toString() + " " + kernelsize + " " + maxpool + " " + stride + " " + dropout1 + " " + dropout2 + " " + lr;
    }

}
