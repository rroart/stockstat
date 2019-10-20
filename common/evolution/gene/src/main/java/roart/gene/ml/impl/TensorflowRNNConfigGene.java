package roart.gene.ml.impl;

import roart.common.ml.TensorflowCNNConfig;
import roart.common.ml.TensorflowRNNConfig;
import roart.gene.AbstractGene;
import roart.gene.NeuralNetConfigGene;

public class TensorflowRNNConfigGene extends TensorflowRecurrentConfigGene {
    public TensorflowRNNConfigGene(TensorflowRNNConfig config) {
        super(config);
    }
    
    @Override
    public NeuralNetConfigGene copy() {
        return new TensorflowRNNConfigGene(new TensorflowRNNConfig((TensorflowRNNConfig) getConfig()));
    }

    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        TensorflowRNNConfigGene offspring = new TensorflowRNNConfigGene((TensorflowRNNConfig) getConfig());
        offspring.crossover((TensorflowConfigGene) otherNN);
        return offspring;
    }
}
