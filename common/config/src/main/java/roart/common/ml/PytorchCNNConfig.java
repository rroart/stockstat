package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class PytorchCNNConfig extends PytorchPreFeedConfig {

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
    public PytorchCNNConfig(
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
            @JsonProperty("stride") int stride) {
        super(MLConstants.CNN, new PytorchConfigCommon(steps, lr, inputdropout, dropout, normalize, batchnormalize, regularize, batchsize, loss, optimizer, activation, lastactivation));
        this.kernelsize = kernelsize;
        this.stride = stride;
    }
    
    public PytorchCNNConfig(PytorchCNNConfig config) {
        this(config.pytorchConfigCommon, config.kernelsize, config.stride);
   }

    public PytorchCNNConfig(String name) {
        super(name);
    }

    public PytorchCNNConfig() {
        super();
        // JSON
    }

    public PytorchCNNConfig(PytorchConfigCommon pytorchConfigCommon, int kernelsize2, int stride2) {
        super(MLConstants.CNN, pytorchConfigCommon);
    }

    @Override
    public String toString() {
        return super.toString() + " " + kernelsize + " " + stride;
    }
}
