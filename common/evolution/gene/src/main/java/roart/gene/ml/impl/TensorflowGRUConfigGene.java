package roart.gene.ml.impl;

import roart.common.ml.TensorflowCNNConfig;
import roart.common.ml.TensorflowGRUConfig;
import roart.gene.AbstractGene;
import roart.gene.NeuralNetConfigGene;

public class TensorflowGRUConfigGene extends TensorflowRecurrentConfigGene {
    public TensorflowGRUConfigGene(TensorflowGRUConfig config, boolean predictor) {
        super(config);
        this.predictor = predictor;
    }
    
    public TensorflowGRUConfigGene() {        
        // JSON
    }
    
    @Override
    public NeuralNetConfigGene copy() {
        return new TensorflowGRUConfigGene(new TensorflowGRUConfig((TensorflowGRUConfig) getConfig()), predictor);
    }

    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        TensorflowGRUConfigGene offspring = new TensorflowGRUConfigGene((TensorflowGRUConfig) getConfig(), predictor);
        offspring.crossover((TensorflowConfigGene) otherNN);
        return offspring;
    }
}
