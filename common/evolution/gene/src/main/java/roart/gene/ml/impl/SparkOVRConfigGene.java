package roart.gene.ml.impl;

import roart.common.ml.NeuralNetConfig;
import roart.common.ml.SparkOVRConfig;
import roart.gene.AbstractGene;
import roart.gene.NeuralNetConfigGene;
import roart.common.constants.Constants;

public class SparkOVRConfigGene extends SparkConfigGene {

    public SparkOVRConfigGene(NeuralNetConfig config) {
        super(config);
    }

    public SparkOVRConfigGene() {
        // JSON
    }

    @Override
    public NeuralNetConfigGene copy() {
        return new SparkOVRConfigGene(new SparkOVRConfig((SparkOVRConfig) getConfig()));
    }

    @Override
    public void randomize() {
        super.randomize();
        SparkOVRConfig myconfig = (SparkOVRConfig) getConfig();
        myconfig.setFitintercept(generateFitintercept());
    }

    @Override
    public void mutate() {
        SparkOVRConfig myconfig = (SparkOVRConfig) getConfig();
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
        SparkOVRConfigGene offspring = new SparkOVRConfigGene((SparkOVRConfig) getConfig());
        offspring.crossover((SparkConfigGene) otherNN);
        SparkOVRConfigGene other = (SparkOVRConfigGene) otherNN;
        SparkOVRConfig myconfig = (SparkOVRConfig) getConfig();
        SparkOVRConfig otherconfig = (SparkOVRConfig) other.getConfig();
        if (random.nextBoolean()) {
            myconfig.setFitintercept(otherconfig.getFitintercept());
        }
        return offspring;
    }


}
