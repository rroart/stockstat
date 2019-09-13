package roart.gene.ml.impl;

import roart.common.ml.PytorchConfig;
import roart.common.ml.PytorchFeedConfig;
import roart.common.util.RandomUtil;
import roart.gene.NeuralNetConfigGene;

public abstract class PytorchConfigGene extends NeuralNetConfigGene {
    
    protected static final int RANDOMS = 4;
    
    public PytorchConfigGene(PytorchConfig config) {
        super(config);
    }

    @Override
    public void randomize() {
        PytorchFeedConfig myconfig = (PytorchFeedConfig) getConfig();
        myconfig.setHidden(generateHidden());
        myconfig.setLayers(generateLayers());
        myconfig.setLr(generateLr());
        myconfig.setSteps(generateSteps());
    }

    public void mutate(int task) {
        PytorchFeedConfig myconfig = (PytorchFeedConfig) getConfig();
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
        case 3:
            myconfig.setSteps(generateSteps());
            break;
        default:
        }
    }

    public void crossover(PytorchConfigGene other) {
        PytorchFeedConfig myconfig = (PytorchFeedConfig) getConfig();
        PytorchFeedConfig otherconfig = (PytorchFeedConfig) other.getConfig();
        if (random.nextBoolean()) {
            myconfig.setHidden(otherconfig.getHidden());
        }
        if (random.nextBoolean()) {
            myconfig.setLayers(otherconfig.getLayers());
        }
        if (random.nextBoolean()) {
            myconfig.setSteps(otherconfig.getSteps());
        }
        if (random.nextBoolean()) {
            myconfig.setLr(otherconfig.getLr());
        }
    }
    
    protected int getMemories() {
        return RandomUtil.random(random, 10, 10, 100);
    }

    protected double getMemorystrength() {
        return RandomUtil.random(random, 0.5, 0.5, 5);
    }

}

