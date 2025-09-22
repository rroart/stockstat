package roart.gene.ml.impl;

import roart.common.ml.PytorchLSTMConfig;
import roart.common.ml.PytorchLSTMConfig;
import roart.gene.AbstractGene;
import roart.gene.NeuralNetConfigGene;

public class PytorchLSTMConfigGene extends PytorchRecurrentConfigGene {

    public PytorchLSTMConfigGene(PytorchLSTMConfig config, boolean predictor) {
        super(config);
        this.predictor = predictor;
    }
    
    public PytorchLSTMConfigGene() {
        // JSON
    }
    
    @Override
    public NeuralNetConfigGene copy() {
        return new PytorchLSTMConfigGene(new PytorchLSTMConfig((PytorchLSTMConfig) getConfig()), predictor);
    }

    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        PytorchLSTMConfigGene offspring = new PytorchLSTMConfigGene((PytorchLSTMConfig) getConfig(), predictor);
        offspring.crossover((PytorchConfigGene) otherNN);
        return offspring;
    }
}
