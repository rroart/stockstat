package roart.gene.ml.impl;

import roart.common.ml.NeuralNetConfig;
import roart.common.ml.SparkLSVCConfig;
import roart.gene.AbstractGene;
import roart.common.constants.Constants;

public class SparkLSVCConfigGene extends SparkConfigGene {

    public SparkLSVCConfigGene(NeuralNetConfig config) {
        super(config);
    }

    @Override
    public void randomize() {
        super.randomize();
        SparkLSVCConfig myconfig = (SparkLSVCConfig) getConfig();
        myconfig.setFitintercept(generateFitintercept());
    }

    @Override
    public void mutate() {
        SparkLSVCConfig myconfig = (SparkLSVCConfig) getConfig();
        int task = random.nextInt(RANDOMS + 1);
        if (task < RANDOMS) {
            super.mutate(task);
            return;
        }
        task = task - RANDOMS;
        switch (task) {
        case 0:
            myconfig.setFitintercept(generateFitintercept());
            break;
        default:
	    log.error(Constants.NOTFOUND, task);
        }              
    }

    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        SparkLSVCConfigGene offspring = new SparkLSVCConfigGene((SparkLSVCConfig) getConfig());
        offspring.crossover((SparkConfigGene) otherNN);
        SparkLSVCConfigGene other = (SparkLSVCConfigGene) otherNN;
        SparkLSVCConfig myconfig = (SparkLSVCConfig) getConfig();
        SparkLSVCConfig otherconfig = (SparkLSVCConfig) other.getConfig();
        if (random.nextBoolean()) {
            myconfig.setFitintercept(otherconfig.getFitintercept());
        }
        return offspring;
    }

}
