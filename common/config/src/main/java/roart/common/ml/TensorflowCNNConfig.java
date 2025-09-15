package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class TensorflowCNNConfig extends TensorflowPreFeedConfig {

    private double lr;
    
    private int kernelsize;
    
    private int stride;

    private double dropout;
    
    public double getLr() {
        return lr;
    }

    public void setLr(double lr) {
        this.lr = lr;
    }

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

    public double getDropout() {
        return dropout;
    }

    public void setDropout(double dropout) {
        this.dropout = dropout;
    }

    @JsonCreator
    public TensorflowCNNConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("lr") double lr, 
            @JsonProperty("kernelsize") int kernelsize, 
            @JsonProperty("stride") int stride, 
            @JsonProperty("dropout") double dropout) {
        super(MLConstants.CNN, steps);
        this.lr = lr;
        this.kernelsize = kernelsize;
        this.stride = stride;
        this.dropout = dropout;
    }
 
    public TensorflowCNNConfig(String name) {
        super(name);
    }

    public TensorflowCNNConfig() {
        super();
        // JSON
    }

    public TensorflowCNNConfig(TensorflowCNNConfig config) {
        this(config.steps, config.lr, config.kernelsize, config.stride, config.dropout);
    }

    @Override
    public String toString() {
        return super.toString() + " " + lr + " " + kernelsize + " " + stride + " " + dropout;
    }
}
