package roart.gene.ml.impl;

import roart.common.constants.Constants;
import roart.common.ml.TensorflowPreFeedConfig;
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

    public static final int RANDOMS = TensorflowConfigGene.RANDOMS + 3;
    
    public TensorflowPreFeedConfigGene(TensorflowConfig config) {
        super(config);
    }

    public TensorflowPreFeedConfigGene() {        
        // JSON
    }
    
    @Override
    public void randomize() {
        super.randomize();
        TensorflowPreFeedConfig myconfig = (TensorflowPreFeedConfig) getConfig();
        myconfig.setConvlayers(generateCnnConvLayers());
        myconfig.setLayers(generateCnnLayers());
        myconfig.setHidden(generateHidden());
    }
    
    @Override
    public void mutate() {
        TensorflowPreFeedConfig myconfig = (TensorflowPreFeedConfig) getConfig();
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
    public void crossover(TensorflowConfigGene otherNN) {
        super.crossover(otherNN);
        TensorflowPreFeedConfigGene other = (TensorflowPreFeedConfigGene) otherNN;
        TensorflowPreFeedConfig myconfig = (TensorflowPreFeedConfig) getConfig();
        TensorflowPreFeedConfig otherconfig = (TensorflowPreFeedConfig) other.getConfig();
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
