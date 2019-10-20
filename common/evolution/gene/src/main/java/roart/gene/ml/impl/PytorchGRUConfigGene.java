package roart.gene.ml.impl;

import roart.common.ml.PytorchGRUConfig;
import roart.common.ml.PytorchGRUConfig;
import roart.gene.AbstractGene;
import roart.gene.NeuralNetConfigGene;

public class PytorchGRUConfigGene extends PytorchRecurrentConfigGene {

    public PytorchGRUConfigGene(PytorchGRUConfig config) {
        super(config);
    }
    
    @Override
    public NeuralNetConfigGene copy() {
        return new PytorchGRUConfigGene(new PytorchGRUConfig((PytorchGRUConfig) getConfig()));
    }

    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        PytorchGRUConfigGene offspring = new PytorchGRUConfigGene((PytorchGRUConfig) getConfig());
        offspring.crossover((PytorchConfigGene) otherNN);
        return offspring;
    }
}
