package roart.gene.ml.impl;

import roart.common.ml.PytorchMLPConfig;
import roart.common.ml.PytorchMLPConfig;
import roart.gene.AbstractGene;
import roart.gene.NeuralNetConfigGene;

public class PytorchMLPConfigGene extends PytorchFeedConfigGene {
    public PytorchMLPConfigGene(PytorchMLPConfig config, boolean predictor) {
        super(config);
        this.predictor = predictor;
    }
    
    public PytorchMLPConfigGene() {        
        // JSON
    }
    
    @Override
    public NeuralNetConfigGene copy() {
        return new PytorchMLPConfigGene(new PytorchMLPConfig((PytorchMLPConfig) getConfig()), predictor);
    }

    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        PytorchMLPConfigGene offspring = new PytorchMLPConfigGene((PytorchMLPConfig) getConfig(), predictor);
        offspring.crossover((PytorchConfigGene) otherNN);
        return offspring;
    }
}
