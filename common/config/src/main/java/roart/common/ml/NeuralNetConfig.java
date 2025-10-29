package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = GemConfig.class, name = "GemConfig"),
    @Type(value = PytorchConfig.class, name = "PytorchConfig"),
    @Type(value = SparkConfig.class, name = "SparkConfig"),
    @Type(value = TensorflowConfig.class, name = "TensorflowConfig") })  
public abstract class NeuralNetConfig {
    private String name;

    private boolean binary;
    
    public NeuralNetConfig(String name) {
        super();
        this.name = name;
    }

    public NeuralNetConfig() {
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    
    /*
    public abstract void randomize();
    
    public abstract void mutate();
    
    public abstract NeuralNetConfig crossover(NeuralNetConfig other);
    */
    
    public boolean isBinary() {
        return binary;
    }

    public void setBinary(boolean binary) {
        this.binary = binary;
    }

    public abstract NeuralNetConfig copy();
    
    public abstract boolean empty();
    
    @Override
    public String toString() {
        return getName();
    }
}
