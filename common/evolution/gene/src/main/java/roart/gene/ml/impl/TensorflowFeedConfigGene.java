package roart.gene.ml.impl;

import roart.common.ml.TensorflowConfig;
import roart.common.ml.TensorflowFeedConfig;

public abstract class TensorflowFeedConfigGene extends TensorflowConfigGene {

    protected static final int RANDOMS = 4;

    public TensorflowFeedConfigGene(TensorflowConfig config) {
        super(config);
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
    public void mutate(int task) {
        TensorflowFeedConfig myconfig = (TensorflowFeedConfig) getConfig();
        if (task < RANDOMS) {
            super.mutate(task);
            return;
        }
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
