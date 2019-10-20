package roart.gene.ml.impl;

import roart.common.ml.TensorflowMLPConfig;
import roart.gene.AbstractGene;
import roart.gene.NeuralNetConfigGene;

public class TensorflowMLPConfigGene extends TensorflowFeedConfigGene {

    public TensorflowMLPConfigGene(TensorflowMLPConfig config) {
        super(config);
    }
    
    @Override
    public NeuralNetConfigGene copy() {
        return new TensorflowMLPConfigGene(new TensorflowMLPConfig((TensorflowMLPConfig) getConfig()));
    }

    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        TensorflowMLPConfigGene offspring = new TensorflowMLPConfigGene((TensorflowMLPConfig) getConfig());
        offspring.crossover((TensorflowConfigGene) otherNN);
        return offspring;
    }
    
}
