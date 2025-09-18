package roart.gene.ml.impl;

import roart.common.ml.TensorflowConfig;
import roart.common.ml.TensorflowFeedConfig;
import roart.gene.NeuralNetConfigGene;
import roart.common.constants.Constants;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = TensorflowEstimatorConfigGene.class, name = "TensorflowEstimatorConfigGene"),
    @Type(value = TensorflowFeedConfigGene.class, name = "TensorflowFeedConfigGene"),
    @Type(value = TensorflowPreFeedConfigGene.class, name = "TensorflowPreFeedConfigGene") })  
public abstract class TensorflowConfigGene extends NeuralNetConfigGene {
    
    protected static final int RANDOMS = 6;
    
    public TensorflowConfigGene(TensorflowConfig config) {
        super(config);
    }

    public TensorflowConfigGene() {        
        // JSON
    }
    
    @Override
    public void randomize() {
        TensorflowConfig myconfig = (TensorflowConfig) getConfig();
        myconfig.setSteps(generateSteps());
        myconfig.setLr(generateLr());
        myconfig.setDropout(generateDropout());
        myconfig.setNormalize(generateBoolean());
        myconfig.setBatchnormalize(generateBoolean());
        myconfig.setRegularize(generateBoolean());
    }

    public void mutate(int task) {
        TensorflowConfig myconfig = (TensorflowConfig) getConfig();
        switch (task) {
        case 0:
            myconfig.setSteps(generateSteps());
            break;
        case 1:
            myconfig.setLr(generateLr());
            break;
        case 2:
            myconfig.setDropout(generateDropout());
            break;
        case 3:
            myconfig.setNormalize(generateBoolean());
            break;
        case 5:
            myconfig.setBatchnormalize(generateBoolean());
            break;
        case 4:
            myconfig.setRegularize(generateBoolean());
            break;
        default:
	    log.error(Constants.NOTFOUND, task);
        }
    }

    public void crossover(TensorflowConfigGene other) {
        TensorflowConfig myconfig = (TensorflowConfig) getConfig();
        TensorflowConfig otherconfig = (TensorflowConfig) other.getConfig();
        if (random.nextBoolean()) {
            myconfig.setSteps(otherconfig.getSteps());
        }
        if (random.nextBoolean()) {
            myconfig.setLr(otherconfig.getLr());
        }
        if (random.nextBoolean()) {
            myconfig.setDropout(otherconfig.getDropout());
        }
        if (random.nextBoolean()) {
            myconfig.setNormalize(otherconfig.isNormalize());
        }
        if (random.nextBoolean()) {
            myconfig.setBatchnormalize(otherconfig.isBatchnormalize());
        }
        if (random.nextBoolean()) {
            myconfig.setRegularize(otherconfig.isRegularize());
        }
    }
    
}

