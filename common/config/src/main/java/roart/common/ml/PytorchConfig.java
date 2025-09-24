package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = PytorchFeedConfig.class, name = "PytorchFeedConfig"),
    @Type(value = PytorchPreFeedConfig.class, name = "PytorchPreFeedConfig") })  
public abstract class PytorchConfig extends NeuralNetConfig {

    protected PytorchConfigCommon pytorchConfigCommon;

    public PytorchConfig(String name, PytorchConfigCommon pytorchConfigCommon) {
        super(name);
        this.pytorchConfigCommon = pytorchConfigCommon;
    }

    public PytorchConfig(String name) {
        super(name);
    }

    public PytorchConfig() {
        super();
        // JSON
    }

    public PytorchConfigCommon getPytorchConfigCommon() {
        return pytorchConfigCommon;
    }

    public void setPytorchConfigCommon(PytorchConfigCommon pytorchConfigCommon) {
        this.pytorchConfigCommon = pytorchConfigCommon;
    }

    public int getSteps() {
        return pytorchConfigCommon.steps;
    }

    public void setSteps(int steps) {
        this.pytorchConfigCommon.steps = steps;
    }

    public Double getLr() {
        return pytorchConfigCommon.lr;
    }

    public void setLr(Double lr) {
        this.pytorchConfigCommon.lr = lr;
    }

    public double getInputdropout() {
        return pytorchConfigCommon.inputdropout;
    }

    public void setInputdropout(double inputdropout) {
        this.pytorchConfigCommon.inputdropout = inputdropout;
    }

    public double getDropout() {
        return pytorchConfigCommon.dropout;
    }

    public void setDropout(double dropout) {
        this.pytorchConfigCommon.dropout = dropout;
    }

    public boolean isNormalize() {
        return pytorchConfigCommon.normalize;
    }

    public void setNormalize(boolean normalize) {
        this.pytorchConfigCommon.normalize = normalize;
    }

    public boolean isBatchnormalize() {
        return pytorchConfigCommon.batchnormalize;
    }

    public void setBatchnormalize(boolean batchnormalize) {
        this.pytorchConfigCommon.batchnormalize = batchnormalize;
    }

    public boolean isRegularize() {
        return pytorchConfigCommon.regularize;
    }

    public void setRegularize(boolean regularize) {
        this.pytorchConfigCommon.regularize = regularize;
    }

    public int getBatchsize() {
        return pytorchConfigCommon.batchsize;
    }

    public void setBatchsize(int batchsize) {
        this.pytorchConfigCommon.batchsize = batchsize;
    }

    public String getLoss() {
        return pytorchConfigCommon.loss;
    }

    public void setLoss(String loss) {
        this.pytorchConfigCommon.loss = loss;
    }

    public String getOptimizer() {
        return pytorchConfigCommon.optimizer;
    }

    public void setOptimizer(String optimizer) {
        this.pytorchConfigCommon.optimizer = optimizer;
    }

    public String getActivation() {
        return pytorchConfigCommon.activation;
    }

    public void setActivation(String activation) {
        this.pytorchConfigCommon.activation = activation;
    }

    public String getLastactivation() {
        return pytorchConfigCommon.lastactivation;
    }

    public void setLastactivation(String lastactivation) {
        this.pytorchConfigCommon.lastactivation = lastactivation;
    }

    @Override
    public boolean empty() {
        return false;
    }

    @Override
    public PytorchConfig copy() {
        return this;
    }

    @Override
    public String toString() {
        return super.toString() + " " + pytorchConfigCommon.steps + " " + pytorchConfigCommon.lr + " " + pytorchConfigCommon.dropout + " " + pytorchConfigCommon.normalize + " " + pytorchConfigCommon.batchnormalize + " " + pytorchConfigCommon.regularize;
    }
}
