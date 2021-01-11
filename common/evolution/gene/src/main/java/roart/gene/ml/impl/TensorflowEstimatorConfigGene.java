package roart.gene.ml.impl;

import roart.common.ml.TensorflowConfig;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = TensorflowDNNConfigGene.class, name = "TensorflowDNNConfigGene"),
    @Type(value = TensorflowLICConfigGene.class, name = "TensorflowLICConfigGene"),
    @Type(value = TensorflowLIRConfigGene.class, name = "TensorflowLIRConfigGene") })  
public abstract class TensorflowEstimatorConfigGene extends TensorflowConfigGene {

    public TensorflowEstimatorConfigGene(TensorflowConfig config) {
        super(config);
    }

    public TensorflowEstimatorConfigGene() {        
        // JSON
    }
    
}
