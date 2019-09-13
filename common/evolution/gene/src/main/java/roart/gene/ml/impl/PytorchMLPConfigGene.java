package roart.gene.ml.impl;

import roart.common.ml.PytorchMLPConfig;
import roart.gene.AbstractGene;

public class PytorchMLPConfigGene extends PytorchFeedConfigGene {
    public PytorchMLPConfigGene(PytorchMLPConfig config) {
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
        PytorchMLPConfigGene offspring = new PytorchMLPConfigGene((PytorchMLPConfig) getConfig());
        ((PytorchMLPConfigGene) offspring).crossover(otherNN);
        return offspring;
    }
}
