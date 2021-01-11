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
    @Type(value = TensorflowCNNConfigGene.class, name = "TensorflowCNNConfigGene"),
    @Type(value = TensorflowCNN2ConfigGene.class, name = "TensorflowCNN2ConfigGene") })  
public abstract class TensorflowPreFeedConfigGene extends TensorflowConfigGene {

    public TensorflowPreFeedConfigGene(TensorflowConfig config) {
        super(config);
    }

    public TensorflowPreFeedConfigGene() {        
        // JSON
    }
}
