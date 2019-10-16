package roart.gene.ml.impl;

import roart.common.ml.TensorflowConfig;
import roart.common.ml.TensorflowFeedConfig;
import roart.gene.NeuralNetConfigGene;
import roart.common.constants.Constants;

public abstract class TensorflowConfigGene extends NeuralNetConfigGene {
    
    protected static final int RANDOMS = 1;
    
    public TensorflowConfigGene(TensorflowConfig config) {
        super(config);
    }

    @Override
    public void randomize() {
        TensorflowConfig myconfig = (TensorflowConfig) getConfig();
        myconfig.setSteps(generateSteps());
    }

    public void mutate(int task) {
        TensorflowConfig myconfig = (TensorflowConfig) getConfig();
        switch (task) {
        case 0:
            myconfig.setSteps(generateSteps());
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
    }
    
}

