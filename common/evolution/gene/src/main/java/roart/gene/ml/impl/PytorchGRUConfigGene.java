package roart.gene.ml.impl;

import roart.common.ml.PytorchGRUConfig;
import roart.gene.AbstractGene;

public class PytorchGRUConfigGene extends PytorchRecurrentConfigGene {

    public PytorchGRUConfigGene(PytorchGRUConfig config) {
        super(config);
    }
    
    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        PytorchGRUConfigGene offspring = new PytorchGRUConfigGene((PytorchGRUConfig) getConfig());
        offspring.crossover((PytorchConfigGene) otherNN);
        return offspring;
    }
}
