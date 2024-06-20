package roart.gene.ml.impl;

import roart.common.ml.TensorflowConfig;
import roart.common.ml.TensorflowFeedConfig;
import roart.common.constants.Constants;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = TensorflowMLPConfigGene.class, name = "TensorflowMLPConfigGene"),
    @Type(value = TensorflowDNNConfigGene.class, name = "TensorflowDNNConfigGene"),
    @Type(value = TensorflowLICConfigGene.class, name = "TensorflowLICConfigGene"),
    @Type(value = TensorflowLIRConfigGene.class, name = "TensorflowLIRConfigGene"),
    @Type(value = TensorflowRecurrentConfigGene.class, name = "TensorflowRecurrentConfigGene") })  
public abstract class TensorflowFeedConfigGene extends TensorflowConfigGene {

    public TensorflowFeedConfigGene(TensorflowConfig config) {
        super(config);
    }

    public TensorflowFeedConfigGene() {
        // JSON
    }
    
    @Override
    public void randomize() {
        super.randomize();
        TensorflowFeedConfig myconfig = (TensorflowFeedConfig) getConfig();
        myconfig.setHidden(generateHidden());
        myconfig.setLayers(generateLayers());
        myconfig.setLr(generateLr());
    }
    
    @Override
    public void mutate() {
        TensorflowFeedConfig myconfig = (TensorflowFeedConfig) getConfig();
	int task = random.nextInt(RANDOMS + 3);
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
        case 2:
            myconfig.setLr(generateLr());
            break;
        default:
	    log.error(Constants.NOTFOUND, task);
        }
     }

    @Override
    public void crossover(TensorflowConfigGene other) {
        super.crossover(other);
        TensorflowFeedConfig myconfig = (TensorflowFeedConfig) getConfig();
        TensorflowFeedConfig otherconfig = (TensorflowFeedConfig) other.getConfig();
        if (random.nextBoolean()) {
            myconfig.setHidden(otherconfig.getHidden());
        }
        if (random.nextBoolean()) {
            myconfig.setLayers(otherconfig.getLayers());
        }
        if (random.nextBoolean()) {
            myconfig.setLr(otherconfig.getLr());
        }
    }
    
}
