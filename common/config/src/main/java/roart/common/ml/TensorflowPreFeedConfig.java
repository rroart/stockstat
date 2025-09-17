package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = TensorflowCNNConfig.class, name = "TensorflowCNNConfig"),
    @Type(value = TensorflowCNN2Config.class, name = "TensorflowCNN2Config") })  
public abstract class TensorflowPreFeedConfig extends TensorflowConfig {

    public TensorflowPreFeedConfig(String name, int steps, double lr) {
        super(name, steps, lr);
    }

    public TensorflowPreFeedConfig(String name) {
        super(name);
    }

    public TensorflowPreFeedConfig() {
        super();
        // JSON
    }

}
