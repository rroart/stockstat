package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class TensorflowCNNConfig extends TensorflowPreFeedConfig {

    private int kernelsize;
    
    private int maxpool;
    
    private int stride;

    @JsonCreator
    public TensorflowCNNConfig(
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
            @JsonProperty("convlayers") int convlayers, 
            @JsonProperty("layers") int layers, 
            @JsonProperty("hidden") int hidden, 
            @JsonProperty("kernelsize") int kernelsize, 
            @JsonProperty("maxpool") int maxpool,
            @JsonProperty("stride") int stride) {
        super(MLConstants.CNN, new TensorflowConfigCommon(steps, lr, inputdropout, dropout, normalize, batchnormalize, regularize, batchsize, loss, optimizer, activation, lastactivation), convlayers, layers, hidden);
        this.kernelsize = kernelsize;
        this.maxpool = maxpool;
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
        super(MLConstants.CNN, config.tensorflowConfigCommon, config.convlayers, config.layers, config.hidden);
        this.kernelsize = config.kernelsize;
        this.maxpool = config.maxpool;
        this.stride = config.stride;
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
