package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = PytorchCNNConfig.class, name = "PytorchCNNConfig"),
    @Type(value = PytorchCNN2Config.class, name = "PytorchCNN2Config") })  
public abstract class PytorchPreFeedConfig extends PytorchConfig {

    public PytorchPreFeedConfig(String name, int steps, double lr, double dropout, boolean normalize, boolean batchnormalize, boolean regularize) {
        super(name, steps, lr, dropout, normalize, batchnormalize, regularize);
    }

    public PytorchPreFeedConfig(String name) {
        super(name);
    }

    public PytorchPreFeedConfig() {
        super();
        // JSON
    }

}
