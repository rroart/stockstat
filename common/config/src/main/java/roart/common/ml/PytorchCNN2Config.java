package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class PytorchCNN2Config extends PytorchPreFeedConfig {
    private int kernelsize;
    
    private int maxpool;
    
    private int stride;
    
    @JsonCreator
    public PytorchCNN2Config(
            @JsonProperty("steps") int steps, 
            @JsonProperty("lr") Double lr,
            @JsonProperty("inputdropout") double inputdropout,
            @JsonProperty("dropout") double dropout,
            @JsonProperty("normalize") boolean normalize, 
            @JsonProperty("batchnormalize") boolean batchnormalize, 
            @JsonProperty("regularize") boolean regularize,           
            @JsonProperty("batchsize") int batchsize,
            @JsonProperty("loss") String loss,
            @JsonProperty("optimizer") String optimizer,
            @JsonProperty("activation") String activation,
            @JsonProperty("lastactivation") String lastactivation,
            @JsonProperty("kernelsize") int kernelsize,
            @JsonProperty("maxpool") int maxpool,
            @JsonProperty("stride") int stride) {
        super(MLConstants.CNN2, new PytorchConfigCommon(steps, lr, inputdropout, dropout, normalize, batchnormalize, regularize, batchsize, loss, optimizer, activation, lastactivation));
        this.kernelsize = kernelsize;
        this.maxpool = maxpool;
        this.stride = stride;
    }
    
    public PytorchCNN2Config(PytorchCNN2Config config) {
        this(config.pytorchConfigCommon, config.kernelsize, config.maxpool, config.stride);
    }

    public PytorchCNN2Config(String name) {
        super(name);
    }

    public PytorchCNN2Config() {
        super();
        // JSON
    }

    public PytorchCNN2Config(PytorchConfigCommon pytorchConfigCommon, int kernelsize2, int maxpool2, int stride2) {
        super(MLConstants.CNN2, pytorchConfigCommon);
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

    @Override
    public String toString() {
        return super.toString() + " " + kernelsize + " " + maxpool + " " + stride;
    }

}
