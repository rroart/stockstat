package roart.gene.ml.impl;

import roart.common.ml.TensorflowMLPConfig;
import roart.gene.AbstractGene;
import roart.gene.NeuralNetConfigGene;

public class TensorflowMLPConfigGene extends TensorflowFeedConfigGene {

    public TensorflowMLPConfigGene(TensorflowMLPConfig config, boolean predictor) {
        super(config);
        this.predictor = predictor;
    }
    
    public TensorflowMLPConfigGene() {        
        // JSON
    }
    
    @Override
    public NeuralNetConfigGene copy() {
        return new TensorflowMLPConfigGene(new TensorflowMLPConfig((TensorflowMLPConfig) getConfig()), predictor);
    }

    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        TensorflowMLPConfigGene offspring = new TensorflowMLPConfigGene((TensorflowMLPConfig) getConfig(), predictor);
        offspring.crossover((TensorflowConfigGene) otherNN);
        return offspring;
    }
    
}
