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

    protected int steps;
    
    protected double lr;
    
    protected double dropout;
    
    protected boolean normalize;

    protected boolean batchnormalize;

    protected boolean regularize;
    
    public PytorchConfig(String name, int steps, double lr, double dropout, boolean normalize, boolean batchnormalize, boolean regularize) {
        super(name);
        this.steps = steps;
        this.lr = lr;
        this.dropout = dropout;
        this.normalize = normalize;
        this.batchnormalize = batchnormalize;
        this.regularize = regularize;
    }

    public PytorchConfig(String name) {
        super(name);
    }

    public PytorchConfig() {
        super();
        // JSON
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
        return super.toString() + " " + steps + " " + lr + " " + dropout + " " + normalize + " " + batchnormalize + " " + regularize;
    }
}
