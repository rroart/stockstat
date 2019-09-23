package roart.gene.ml.impl;

import roart.common.ml.PytorchConfig;
import roart.common.ml.PytorchFeedConfig;
import roart.common.constants.Constants;

public abstract class PytorchFeedConfigGene extends PytorchConfigGene {
    public PytorchFeedConfigGene(PytorchConfig config) {
        super(config);
    }

    @Override
    public void randomize() {
        super.randomize();
        PytorchFeedConfig myconfig = (PytorchFeedConfig) getConfig();
        myconfig.setHidden(generateHidden());
        myconfig.setLayers(generateLayers());
        myconfig.setLr(generateLr());
    }
    
    @Override
    public void mutate(int task) {
        PytorchFeedConfig myconfig = (PytorchFeedConfig) getConfig();
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
        if (random.nextBoolean()) {
            myconfig.setLr(otherconfig.getLr());
        }
    }
}
