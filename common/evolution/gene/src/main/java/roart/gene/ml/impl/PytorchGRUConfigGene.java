package roart.gene.ml.impl;

import roart.common.ml.PytorchGRUConfig;
import roart.common.ml.PytorchGRUConfig;
import roart.gene.AbstractGene;
import roart.gene.NeuralNetConfigGene;

public class PytorchGRUConfigGene extends PytorchRecurrentConfigGene {

    public PytorchGRUConfigGene(PytorchGRUConfig config, boolean predictor) {
        super(config);
        this.predictor = predictor;
    }
    
    public PytorchGRUConfigGene() {        
        // JSON
    }
    
    @Override
    public NeuralNetConfigGene copy() {
        return new PytorchGRUConfigGene(new PytorchGRUConfig((PytorchGRUConfig) getConfig()), predictor);
    }

    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        PytorchGRUConfigGene offspring = new PytorchGRUConfigGene((PytorchGRUConfig) getConfig(), predictor);
        offspring.crossover((PytorchConfigGene) otherNN);
        return offspring;
    }
}
