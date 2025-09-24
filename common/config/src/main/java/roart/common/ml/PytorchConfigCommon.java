package roart.common.ml;

public class PytorchConfigCommon {

    protected int steps;
    
    protected Double lr;
    
    protected double inputdropout;
    
    protected double dropout;
    
    protected boolean normalize;

    protected boolean batchnormalize;

    protected boolean regularize;
    
    protected int batchsize;
    
    protected String loss;
    
    protected String optimizer;
    
    protected String activation;
    
    protected String lastactivation;

    public PytorchConfigCommon(int steps, double lr, double inputdropout, double dropout, boolean normalize,
            boolean batchnormalize, boolean regularize, int batchsize, String loss, String optimizer, String activation,
            String lastactivation) {
        super();
        this.steps = steps;
        this.lr = lr;
        this.inputdropout = inputdropout;
        this.dropout = dropout;
        this.normalize = normalize;
        this.batchnormalize = batchnormalize;
        this.regularize = regularize;
        this.batchsize = batchsize;
        this.loss = loss;
        this.optimizer = optimizer;
        this.activation = activation;
        this.lastactivation = lastactivation;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public double getLr() {
        return lr;
    }

    public void setLr(double lr) {
        this.lr = lr;
    }

    public double getInputdropout() {
        return inputdropout;
    }

    public void setInputdropout(double inputdropout) {
        this.inputdropout = inputdropout;
    }

    public double getDropout() {
        return dropout;
    }

    public void setDropout(double dropout) {
        this.dropout = dropout;
    }

    public boolean isNormalize() {
        return normalize;
    }

    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
    }

    public boolean isBatchnormalize() {
        return batchnormalize;
    }

    public void setBatchnormalize(boolean batchnormalize) {
        this.batchnormalize = batchnormalize;
    }

    public boolean isRegularize() {
        return regularize;
    }

    public void setRegularize(boolean regularize) {
        this.regularize = regularize;
    }

    public int getBatchsize() {
        return batchsize;
    }

    public void setBatchsize(int batchsize) {
        this.batchsize = batchsize;
    }

    public String getLoss() {
        return loss;
    }

    public void setLoss(String loss) {
        this.loss = loss;
    }

    public String getOptimizer() {
        return optimizer;
    }

    public void setOptimizer(String optimizer) {
        this.optimizer = optimizer;
    }

    public String getActivation() {
        return activation;
    }

    public void setActivation(String activation) {
        this.activation = activation;
    }

    public String getLastactivation() {
        return lastactivation;
    }

    public void setLastactivation(String lastactivation) {
        this.lastactivation = lastactivation;
    }
    
    @Override
    public String toString() {
        return steps + " " + lr + " " + inputdropout + " " + dropout + " " + normalize + " " + batchnormalize + " " + regularize + " " + batchsize + " " + loss + " " + optimizer + " " + activation + " " + lastactivation;
    }
}
