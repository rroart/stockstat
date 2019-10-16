package roart.gene.ml.impl;

import roart.common.ml.TensorflowMLPConfig;
import roart.gene.AbstractGene;

public class TensorflowMLPConfigGene extends TensorflowFeedConfigGene {

    public TensorflowMLPConfigGene(TensorflowMLPConfig config) {
        super(config);
    }
    
    @Override
    public void randomize() {
        super.randomize();
    }

    @Override
    public void mutate() {
        int task = random.nextInt(RANDOMS);
        super.mutate(task);

    }
    
    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        TensorflowMLPConfigGene offspring = new TensorflowMLPConfigGene((TensorflowMLPConfig) getConfig());
        offspring.crossover((TensorflowConfigGene) otherNN);
        return offspring;
    }
    
}
