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
    
    public PytorchConfig(String name, int steps) {
        super(name);
        this.steps = steps;
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
        return super.toString() + " " + steps;
    }
}
