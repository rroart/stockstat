package roart.gene.ml.impl;

import roart.common.ml.GemMMConfig;
import roart.gene.AbstractGene;

public class GemMMConfigGene extends GemConfigGene {

    public GemMMConfigGene(GemMMConfig config) {
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
        GemMMConfigGene offspring = new GemMMConfigGene((GemMMConfig) getConfig());
        ((GemConfigGene) offspring).crossover(otherNN);
        return offspring;
    }

}
