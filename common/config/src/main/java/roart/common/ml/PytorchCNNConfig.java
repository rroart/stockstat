package roart.common.ml;

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

    public PytorchCNNConfig(int steps, int kernelsize, int stride) {
        super(MLConstants.CNN, steps);
        this.kernelsize = kernelsize;
        this.stride = stride;
    }
    
    @Override
    public String toString() {
        return super.toString() + " " + kernelsize + " " + stride;
    }
}
