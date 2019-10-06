package roart.gene.ml.impl;

import roart.common.ml.GemConfig;
import roart.common.ml.GemGEMConfig;
import roart.common.util.RandomUtil;
import roart.gene.NeuralNetConfigGene;
import roart.common.constants.Constants;

public abstract class GemConfigGene extends NeuralNetConfigGene {
    
    protected static final int RANDOMS = 4;
    
    public GemConfigGene(GemConfig config) {
        super(config);
    }

    @Override
    public void randomize() {
        GemConfig myconfig = (GemConfig) getConfig();
        myconfig.setN_hiddens(generateHidden());
        myconfig.setN_layers(generateLayers());
        myconfig.setLr(generateLr());
        myconfig.setSteps(generateSteps());
    }

    public void mutate(int task) {
        GemConfig myconfig = (GemConfig) getConfig();
        switch (task) {
        case 0:
            myconfig.setN_hiddens(generateHidden());
            break;
        case 1:
            myconfig.setN_layers(generateLayers());
            break;
        case 2:
            myconfig.setLr(generateLr());
            break;
        case 3:
            myconfig.setSteps(generateSteps());
            break;
        default:
	    log.error(Constants.NOTFOUND, task);
        }
    }

    public void crossover(GemConfigGene other) {
        GemConfig myconfig = (GemConfig) getConfig();
        GemConfig otherconfig = (GemConfig) other.getConfig();
        if (random.nextBoolean()) {
            myconfig.setN_hiddens(otherconfig.getN_hiddens());
        }
        if (random.nextBoolean()) {
            myconfig.setN_layers(otherconfig.getN_layers());
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

