package roart.gene.ml.impl;

import roart.common.ml.SparkMLPCConfig;
import roart.gene.AbstractGene;

public class SparkMLPCConfigGene extends SparkConfigGene {
    
    public SparkMLPCConfigGene(SparkMLPCConfig config) {
        super(config);
    }
    
    @Override
    public void randomize() {
        super.randomize();
        SparkMLPCConfig myconfig = (SparkMLPCConfig) getConfig();
        myconfig.setHidden(generateHidden());
        myconfig.setLayers(generateLayers());
    }
    
    @Override
    public void mutate() {
        SparkMLPCConfig myconfig = (SparkMLPCConfig) getConfig();
        int task = random.nextInt(RANDOMS + 2);
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
        default:
        }
    }

    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        SparkMLPCConfigGene offspring = new SparkMLPCConfigGene((SparkMLPCConfig) getConfig());
        ((SparkConfigGene) offspring).crossover(otherNN);
        SparkMLPCConfigGene other = (SparkMLPCConfigGene) otherNN;
        SparkMLPCConfig myconfig = (SparkMLPCConfig) getConfig();
        SparkMLPCConfig otherconfig = (SparkMLPCConfig) other.getConfig();
        if (random.nextBoolean()) {
            myconfig.setHidden(otherconfig.getHidden());
        }
        if (random.nextBoolean()) {
            myconfig.setLayers(otherconfig.getLayers());
        }
        return offspring;
    }

}
