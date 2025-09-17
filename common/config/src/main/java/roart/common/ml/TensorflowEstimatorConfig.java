package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = TensorflowDNNConfig.class, name = "TensorflowDNNConfig"),
    @Type(value = TensorflowLICConfig.class, name = "TensorflowLICConfig"),
    @Type(value = TensorflowLIRConfig.class, name = "TensorflowLIRConfig") })  
public abstract class TensorflowEstimatorConfig extends TensorflowConfig {

    public TensorflowEstimatorConfig(String name, int steps, double lr) {
        super(name, steps, lr);
    }

    public TensorflowEstimatorConfig(String name) {
        super(name);
    }

    public TensorflowEstimatorConfig() {
        super();
        // JSON
    }

}
