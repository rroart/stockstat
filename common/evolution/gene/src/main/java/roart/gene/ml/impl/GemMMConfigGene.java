package roart.gene.ml.impl;

import roart.common.ml.GemMMConfig;
import roart.common.ml.GemMMConfig;
import roart.gene.AbstractGene;
import roart.gene.NeuralNetConfigGene;

public class GemMMConfigGene extends GemConfigGene {

    public GemMMConfigGene(GemMMConfig config) {
        super(config);
    }

    @Override
    public NeuralNetConfigGene copy() {
        return new GemMMConfigGene(new GemMMConfig((GemMMConfig) getConfig()));
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
        GemMMConfigGene offspring = new GemMMConfigGene((GemMMConfig) getConfig());
        offspring.crossover((GemConfigGene) otherNN);
        return offspring;
    }

}
