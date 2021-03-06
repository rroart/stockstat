package roart.gene.ml.impl;

import roart.common.ml.PytorchConfig;
import roart.common.ml.PytorchFeedConfig;
import roart.common.util.RandomUtil;
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
    @Type(value = PytorchFeedConfigGene.class, name = "PytorchFeedConfigGene"),
    @Type(value = PytorchPreFeedConfigGene.class, name = "PytorchPreFeedConfigGene") })  
public abstract class PytorchConfigGene extends NeuralNetConfigGene {
    
    protected static final int RANDOMS = 1;
    
    public PytorchConfigGene(PytorchConfig config) {
        super(config);
    }

    public PytorchConfigGene() {        
        // JSON
    }
    
    @Override
    public void randomize() {
        PytorchConfig myconfig = (PytorchConfig) getConfig();
        myconfig.setSteps(generateSteps());
    }

    public void mutate(int task) {
        PytorchConfig myconfig = (PytorchConfig) getConfig();
        switch (task) {
        case 0:
            myconfig.setSteps(generateSteps());
            break;
        default:
	    log.error(Constants.NOTFOUND, task);
        }
    }

    public void crossover(PytorchConfigGene other) {
        PytorchConfig myconfig = (PytorchConfig) getConfig();
        PytorchConfig otherconfig = (PytorchConfig) other.getConfig();
        if (random.nextBoolean()) {
            myconfig.setSteps(otherconfig.getSteps());
        }
    }
    
    protected int getMemories() {
        return RandomUtil.random(random, 10, 10, 100);
    }

    protected double getMemorystrength() {
        return RandomUtil.random(random, 0.5, 0.5, 5);
    }

}

