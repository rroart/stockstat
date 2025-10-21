package roart.gene.ml.impl;

import roart.common.constants.Constants;
import roart.common.ml.PytorchConfig;
import roart.common.ml.PytorchPreFeedConfig;

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

    public static final int RANDOMS = PytorchConfigGene.RANDOMS + 3;
    
    public PytorchPreFeedConfigGene(PytorchConfig config) {
        super(config);
    }
    
    public PytorchPreFeedConfigGene() {        
        // JSON
    }
    
    @Override
    public void randomize() {
        super.randomize();
        PytorchPreFeedConfig myconfig = (PytorchPreFeedConfig) getConfig();
        myconfig.setConvlayers(generateCnnConvLayers());
        myconfig.setLayers(generateCnnLayers());
        myconfig.setHidden(generateHidden());
    }
    
    @Override
    public void mutate() {
        PytorchPreFeedConfig myconfig = (PytorchPreFeedConfig) getConfig();
        int task = random.nextInt(RANDOMS + 3);
        if (task < RANDOMS) {
            super.mutate(task);
            return;
        }
        task = task - RANDOMS;
        switch (task) {
        case 0:
            myconfig.setConvlayers(generateCnnConvLayers());
            break;
        case 1:
            myconfig.setLayers(generateCnnLayers());
            break;
        case 2:
            myconfig.setHidden(generateHidden());
            break;
        default:
            log.error(Constants.NOTFOUND, task);
        }
     }

    @Override
    public void crossover(PytorchConfigGene otherNN) {
        super.crossover(otherNN);
        PytorchPreFeedConfigGene other = (PytorchPreFeedConfigGene) otherNN;
        PytorchPreFeedConfig myconfig = (PytorchPreFeedConfig) getConfig();
        PytorchPreFeedConfig otherconfig = (PytorchPreFeedConfig) other.getConfig();
        if (random.nextBoolean()) {
            myconfig.setConvlayers(otherconfig.getConvlayers());
        }
        if (random.nextBoolean()) {
            myconfig.setLayers(otherconfig.getLayers());
        }
        if (random.nextBoolean()) {
            myconfig.setHidden(otherconfig.getHidden());
        }
    }

}
