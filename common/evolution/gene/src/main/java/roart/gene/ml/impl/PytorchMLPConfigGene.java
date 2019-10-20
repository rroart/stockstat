package roart.gene.ml.impl;

import roart.common.ml.PytorchMLPConfig;
import roart.common.ml.PytorchMLPConfig;
import roart.gene.AbstractGene;
import roart.gene.NeuralNetConfigGene;

public class PytorchMLPConfigGene extends PytorchFeedConfigGene {
    public PytorchMLPConfigGene(PytorchMLPConfig config) {
        super(config);
    }
    
    @Override
    public NeuralNetConfigGene copy() {
        return new PytorchMLPConfigGene(new PytorchMLPConfig((PytorchMLPConfig) getConfig()));
    }

    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        PytorchMLPConfigGene offspring = new PytorchMLPConfigGene((PytorchMLPConfig) getConfig());
        offspring.crossover((PytorchConfigGene) otherNN);
        return offspring;
    }
}
