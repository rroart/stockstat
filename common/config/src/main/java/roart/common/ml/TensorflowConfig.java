package roart.common.ml;

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

    protected int steps;

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public TensorflowConfig(String name, int steps) {
        super(name);
        this.steps = steps;
    }
    
    public TensorflowConfig(String name) {
        super(name);
    }

    public TensorflowConfig() {
        super();
        // JSON
    }

    @Override
    public boolean empty() {
        return steps == 0;
    }

    @Override
    public TensorflowConfig copy() {
        return this;
    }
    
    @Override
    public String toString() {
        return super.toString() + " " + steps;
    }
}
