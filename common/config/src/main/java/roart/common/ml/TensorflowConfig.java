package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = TensorflowEstimatorConfig.class, name = "TensorflowEstimatorConfig"),
    @Type(value = TensorflowFeedConfig.class, name = "TensorflowFeedConfig"),
    @Type(value = TensorflowGANConfig.class, name = "TensorflowGANConfig"),
    @Type(value = TensorflowPreFeedConfig.class, name = "TensorflowPreFeedConfig") })  
public abstract class TensorflowConfig extends NeuralNetConfig {

    protected TensorflowConfigCommon tensorflowConfigCommon;
    public int getSteps() {
        return tensorflowConfigCommon.steps;
    }

    public void setSteps(int steps) {
        this.tensorflowConfigCommon.steps = steps;
    }

    public Double getLr() {
        return tensorflowConfigCommon.lr;
    }

    public void setLr(Double lr) {
        this.tensorflowConfigCommon.lr = lr;
    }

    public double getInputdropout() {
        return tensorflowConfigCommon.inputdropout;
    }

    public void setInputdropout(double inputdropout) {
        this.tensorflowConfigCommon.inputdropout = inputdropout;
    }

    public double getDropout() {
        return tensorflowConfigCommon.dropout;
    }

    public void setDropout(double dropout) {
        this.tensorflowConfigCommon.dropout = dropout;
    }

    public boolean isNormalize() {
        return tensorflowConfigCommon.normalize;
    }

    public void setNormalize(boolean normalize) {
        this.tensorflowConfigCommon.normalize = normalize;
    }

    public boolean isBatchnormalize() {
        return tensorflowConfigCommon.batchnormalize;
    }

    public void setBatchnormalize(boolean batchnormalize) {
        this.tensorflowConfigCommon.batchnormalize = batchnormalize;
    }

    public boolean isRegularize() {
        return tensorflowConfigCommon.regularize;
    }

    public void setRegularize(boolean regularize) {
        this.tensorflowConfigCommon.regularize = regularize;
    }

    public int getBatchsize() {
        return tensorflowConfigCommon.batchsize;
    }

    public void setBatchsize(int batchsize) {
        this.tensorflowConfigCommon.batchsize = batchsize;
    }

    public String getLoss() {
        return tensorflowConfigCommon.loss;
    }

    public void setLoss(String loss) {
        this.tensorflowConfigCommon.loss = loss;
    }

    public String getOptimizer() {
        return tensorflowConfigCommon.optimizer;
    }

    public void setOptimizer(String optimizer) {
        this.tensorflowConfigCommon.optimizer = optimizer;
    }

    public String getActivation() {
        return tensorflowConfigCommon.activation;
    }

    public void setActivation(String activation) {
        this.tensorflowConfigCommon.activation = activation;
    }

    public String getLastactivation() {
        return tensorflowConfigCommon.lastactivation;
    }

    public void setLastactivation(String lastactivation) {
        this.tensorflowConfigCommon.lastactivation = lastactivation;
    }

    public TensorflowConfig(String name, TensorflowConfigCommon tensorflowConfigCommon) {
        super(name);
        this.tensorflowConfigCommon = tensorflowConfigCommon;
    }
    
    public TensorflowConfig(String name) {
        super(name);
    }

    public TensorflowConfig() {
        super();
        // JSON
    }

    // to avoid getting double content
    @JsonIgnore
    public TensorflowConfigCommon getTensorflowConfigCommon() {
        return tensorflowConfigCommon;
    }

    public void setTensorflowConfigCommon(TensorflowConfigCommon tensorflowConfigCommon) {
        this.tensorflowConfigCommon = tensorflowConfigCommon;
    }

    @Override
    public boolean empty() {
        return tensorflowConfigCommon.steps == 0;
    }

    @Override
    public TensorflowConfig copy() {
        return this;
    }
    
    @Override
    public String toString() {
        return super.toString() + " " + tensorflowConfigCommon.normalize + " " + tensorflowConfigCommon.batchnormalize + " " + tensorflowConfigCommon.regularize + " " + tensorflowConfigCommon.steps + " " + tensorflowConfigCommon.lr;
    }
}
