package roart.gene.ml.impl;

import roart.common.ml.TensorflowLSTMConfig;
import roart.gene.AbstractGene;

public class TensorflowLSTMConfigGene extends TensorflowRecurrentConfigGene {
    public TensorflowLSTMConfigGene(TensorflowLSTMConfig config) {
        super(config);
    }
    
    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        TensorflowLSTMConfigGene offspring = new TensorflowLSTMConfigGene((TensorflowLSTMConfig) getConfig());
        offspring.crossover((TensorflowConfigGene) otherNN);
        return offspring;
    }
}
