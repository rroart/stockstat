package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class TensorflowCNNConfig extends TensorflowPreFeedConfig {

    private int kernelsize;
    
    private int stride;

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

    @JsonCreator
    public TensorflowCNNConfig(
            @JsonProperty("steps") int steps, 
            @JsonProperty("lr") double lr, 
            @JsonProperty("dropout") double dropout,
            @JsonProperty("normalize") boolean normalize, 
            @JsonProperty("batchnormalize") boolean batchnormalize, 
            @JsonProperty("regularize") boolean regularize,           
            @JsonProperty("kernelsize") int kernelsize, 
            @JsonProperty("stride") int stride) {
        super(MLConstants.CNN, steps, lr, dropout, normalize, batchnormalize, regularize);
        this.kernelsize = kernelsize;
        this.stride = stride;
    }
 
    public TensorflowCNNConfig(String name) {
        super(name);
    }

    public TensorflowCNNConfig() {
        super();
        // JSON
    }

    public TensorflowCNNConfig(TensorflowCNNConfig config) {
        this(config.steps, config.lr, config.dropout, config.normalize, config.batchnormalize, config.regularize, config.kernelsize, config.stride);
    }

    @Override
    public String toString() {
        return super.toString() + " " + kernelsize + " " + stride;
    }
}
