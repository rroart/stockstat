package roart.gene.ml.impl;

import roart.common.ml.TensorflowConfig;
import roart.common.ml.TensorflowRecurrentConfig;
import roart.common.constants.Constants;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = TensorflowGRUConfigGene.class, name = "TensorflowGRUConfigGene"),
    @Type(value = TensorflowLSTMConfigGene.class, name = "TensorflowLSTMConfigGene"),
    @Type(value = TensorflowRNNConfigGene.class, name = "TensorflowRNNConfigGene") })  
public abstract class TensorflowRecurrentConfigGene extends TensorflowFeedConfigGene {

    public TensorflowRecurrentConfigGene(TensorflowConfig config) {
        super(config);
    }

    public TensorflowRecurrentConfigGene() {        
        // JSON
    }
    
    @Override
    public void randomize() {
        super.randomize();
        TensorflowRecurrentConfig myconfig = (TensorflowRecurrentConfig) getConfig();
        myconfig.setSlide(generateSlide());
        myconfig.setDropoutin(generateDropoutIn());
    }
    
    @Override
    public void mutate() {
        TensorflowRecurrentConfig myconfig = (TensorflowRecurrentConfig) getConfig();
        int task = random.nextInt(RANDOMS + 2);
        if (task < RANDOMS + 2) {
            super.mutate();
            return;
        }
	task = task - RANDOMS - 2;
        switch (task) {
        case 0:
            myconfig.setSlide(generateSlide());
            break;
        case 1:
            myconfig.setDropoutin(generateDropoutIn());
            break;
        default:
	    log.error(Constants.NOTFOUND, task);
        }
     }

    @Override
    public void crossover(TensorflowConfigGene other) {
        super.crossover(other);
        TensorflowRecurrentConfig myconfig = (TensorflowRecurrentConfig) getConfig();
        TensorflowRecurrentConfig otherconfig = (TensorflowRecurrentConfig) other.getConfig();
        if (random.nextBoolean()) {
            myconfig.setSlide(otherconfig.getSlide());
        }
        if (random.nextBoolean()) {
            myconfig.setDropout(otherconfig.getDropout());
        }
        if (random.nextBoolean()) {
            myconfig.setDropoutin(otherconfig.getDropoutin());
        }
    }
}
