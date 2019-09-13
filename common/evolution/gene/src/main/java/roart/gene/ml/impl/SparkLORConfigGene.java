package roart.gene.ml.impl;

import roart.common.ml.SparkLORConfig;
import roart.gene.AbstractGene;

public class SparkLORConfigGene extends SparkConfigGene {

    public SparkLORConfigGene(SparkLORConfig config) {
        super(config);
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
        ((SparkConfigGene) offspring).crossover(otherNN);
        return offspring;
    }
    
}
