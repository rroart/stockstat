package roart.gene.ml.impl;

import roart.common.ml.PytorchConfig;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = PytorchCNNConfigGene.class, name = "PytorchCNNConfigGene"),
    @Type(value = PytorchCNN2ConfigGene.class, name = "PytorchCNN2ConfigGene") })  
public abstract class PytorchPreFeedConfigGene extends PytorchConfigGene {

    public PytorchPreFeedConfigGene(PytorchConfig config) {
        super(config);
    }
    
    public PytorchPreFeedConfigGene() {        
        // JSON
    }   
}
