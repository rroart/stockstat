package roart.gene.ml.impl;

import roart.common.ml.PytorchMLPConfig;
import roart.gene.AbstractGene;

public class PytorchMLPConfigGene extends PytorchFeedConfigGene {
    public PytorchMLPConfigGene(PytorchMLPConfig config) {
        super(config);
    }
    
    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        PytorchMLPConfigGene offspring = new PytorchMLPConfigGene((PytorchMLPConfig) getConfig());
        offspring.crossover((PytorchConfigGene) otherNN);
        return offspring;
    }
}
