package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class TensorflowCNN2Config extends TensorflowPreFeedConfig {
    private int kernelsize;
    
    private int maxpool;

    private int stride;

    private double dropout1;
    
    private double dropout2;
    
    @JsonCreator
    public TensorflowCNN2Config(
            @JsonProperty("steps") int steps, 
            @JsonProperty("lr") double lr, 
            @JsonProperty("dropout") double dropout,
            @JsonProperty("normalize") boolean normalize, 
            @JsonProperty("batchnormalize") boolean batchnormalize, 
            @JsonProperty("regularize") boolean regularize,           
            @JsonProperty("kernelsize") int kernelsize,
            @JsonProperty("maxpool") int maxpool,
            @JsonProperty("stride") int stride, 
            @JsonProperty("dropout1") double dropout1,
            @JsonProperty("dropout2") double dropout2) {
        super(MLConstants.CNN2, steps, lr, dropout, normalize, batchnormalize, regularize);
        this.kernelsize = kernelsize;
        this.maxpool = maxpool;
        this.stride = stride;
        this.dropout1 = dropout1;
        this.dropout2 = dropout2;
    }
 
    public TensorflowCNN2Config(String name) {
        super(name);
    }

    public TensorflowCNN2Config(TensorflowCNN2Config config) {
        this(config.steps, config.lr, config.dropout, config.normalize, config.batchnormalize, config.regularize, config.kernelsize, config.maxpool, config.stride, config.dropout1, config.dropout2);
    }

    public TensorflowCNN2Config() {
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

    @Override
    public String toString() {
        return super.toString() + " " + kernelsize + " " + maxpool + " " + stride + " " + dropout1 + " " + dropout2;
    }

}
