package roart.gene.ml.impl;

import roart.common.ml.SparkLORConfig;
import roart.common.ml.SparkOVRConfig;
import roart.gene.AbstractGene;
import roart.gene.NeuralNetConfigGene;

public class SparkLORConfigGene extends SparkConfigGene {

    public SparkLORConfigGene(SparkLORConfig config) {
        super(config);
    }

    public SparkLORConfigGene() {
        // JSON
    }

    @Override
    public NeuralNetConfigGene copy() {
        return new SparkLORConfigGene(new SparkLORConfig((SparkLORConfig) getConfig()));
    }

    @Override
    public void randomize() {
        super.randomize();
    }

    @Override
    public void mutate() {
        int task = random.nextInt(RANDOMS);
        super.mutate(task);
    }

    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        SparkLORConfigGene offspring = new SparkLORConfigGene((SparkLORConfig) getConfig());
        offspring.crossover((SparkConfigGene) otherNN);
        return offspring;
    }
    
}
