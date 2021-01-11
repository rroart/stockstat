package roart.gene.ml.impl;

import roart.common.ml.PytorchConfig;
import roart.common.ml.PytorchRecurrentConfig;
import roart.common.constants.Constants;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = PytorchGRUConfigGene.class, name = "PytorchGRUConfigGene"),
    @Type(value = PytorchLSTMConfigGene.class, name = "PytorchLSTMConfigGene"),
    @Type(value = PytorchRNNConfigGene.class, name = "PytorchRNNConfigGene") })  
public abstract class PytorchRecurrentConfigGene extends PytorchFeedConfigGene {

    public PytorchRecurrentConfigGene(PytorchConfig config) {
        super(config);
    }

    public PytorchRecurrentConfigGene() {        
        // JSON
    }
    
    @Override
    public void randomize() {
        super.randomize();
        PytorchRecurrentConfig myconfig = (PytorchRecurrentConfig) getConfig();
        myconfig.setSlide(generateSlide());
    }

    @Override
    public void mutate() {
        PytorchRecurrentConfig myconfig = (PytorchRecurrentConfig) getConfig();
        int task = random.nextInt(RANDOMS + 1 + 3);
        if (task < RANDOMS + 3) {
            super.mutate();
            return;
        }
        task = task - RANDOMS - 3;
        switch (task) {
        case 0:
            myconfig.setSlide(generateSlide());
            break;
        default:
	    log.error(Constants.NOTFOUND, task);
        }
    }

    @Override
    public void crossover(PytorchConfigGene other) {
        super.crossover(other);
        PytorchRecurrentConfig myconfig = (PytorchRecurrentConfig) getConfig();
        PytorchRecurrentConfig otherconfig = (PytorchRecurrentConfig) other.getConfig();
        if (random.nextBoolean()) {
            myconfig.setSlide(otherconfig.getSlide());
        }
    }
}
