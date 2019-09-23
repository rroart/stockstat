package roart.common.ml;

import roart.common.config.MLConstants;

public class TensorflowCNNConfig extends TensorflowPreFeedConfig {

    private int kernelsize;
    
    private int stride;

    private double dropout;
    
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

    public TensorflowCNNConfig(int steps, int kernelsize, int stride, double dropout) {
        super(MLConstants.CNN, steps);
        this.kernelsize = kernelsize;
        this.stride = stride;
        this.dropout = dropout;
    }
 
    @Override
    public String toString() {
        return super.toString() + " " + kernelsize + " " + stride + " " + dropout;
    }
}
