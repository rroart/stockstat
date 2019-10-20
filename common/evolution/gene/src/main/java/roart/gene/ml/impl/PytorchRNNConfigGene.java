package roart.gene.ml.impl;

import roart.common.ml.PytorchRNNConfig;
import roart.common.ml.PytorchRNNConfig;
import roart.gene.AbstractGene;
import roart.gene.NeuralNetConfigGene;

public class PytorchRNNConfigGene extends PytorchRecurrentConfigGene {

    public PytorchRNNConfigGene(PytorchRNNConfig config) {
        super(config);
    }
    
    @Override
    public NeuralNetConfigGene copy() {
        return new PytorchRNNConfigGene(new PytorchRNNConfig((PytorchRNNConfig) getConfig()));
    }

    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        PytorchRNNConfigGene offspring = new PytorchRNNConfigGene((PytorchRNNConfig) getConfig());
        offspring.crossover((PytorchConfigGene) otherNN);
        return offspring;
    }
}
