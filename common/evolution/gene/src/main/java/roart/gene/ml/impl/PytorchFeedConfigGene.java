package roart.gene.ml.impl;

import roart.common.ml.PytorchConfig;
import roart.common.ml.PytorchFeedConfig;
import roart.common.constants.Constants;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = PytorchMLPConfigGene.class, name = "PytorchMLPConfigGene"),
    @Type(value = PytorchRecurrentConfigGene.class, name = "PytorchRecurrentConfigGene") })  
public abstract class PytorchFeedConfigGene extends PytorchConfigGene {
    public PytorchFeedConfigGene(PytorchConfig config) {
        super(config);
    }

    public PytorchFeedConfigGene() {        
        // JSON
    }
    
    @Override
    public void randomize() {
        super.randomize();
        PytorchFeedConfig myconfig = (PytorchFeedConfig) getConfig();
        myconfig.setHidden(generateHidden());
        myconfig.setLayers(generateLayers());
    }
    
    @Override
    public void mutate() {
        PytorchFeedConfig myconfig = (PytorchFeedConfig) getConfig();
        int task = random.nextInt(RANDOMS + 2);
        if (task < RANDOMS) {
            super.mutate(task);
            return;
        }
        task = task - RANDOMS;
        switch (task) {
        case 0:
            myconfig.setHidden(generateHidden());
            break;
        case 1:
            myconfig.setLayers(generateLayers());
            break;
        default:
	    log.error(Constants.NOTFOUND, task);
        }
     }

    @Override
    public void crossover(PytorchConfigGene other) {
        super.crossover(other);
        PytorchFeedConfig myconfig = (PytorchFeedConfig) getConfig();
        PytorchFeedConfig otherconfig = (PytorchFeedConfig) other.getConfig();
        if (random.nextBoolean()) {
            myconfig.setHidden(otherconfig.getHidden());
        }
        if (random.nextBoolean()) {
            myconfig.setLayers(otherconfig.getLayers());
        }
    }
}
