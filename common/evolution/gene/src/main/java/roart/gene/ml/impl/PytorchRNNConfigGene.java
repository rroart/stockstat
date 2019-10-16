package roart.gene.ml.impl;

import roart.common.ml.PytorchRNNConfig;
import roart.gene.AbstractGene;

public class PytorchRNNConfigGene extends PytorchRecurrentConfigGene {

    public PytorchRNNConfigGene(PytorchRNNConfig config) {
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
        PytorchRNNConfigGene offspring = new PytorchRNNConfigGene((PytorchRNNConfig) getConfig());
        offspring.crossover((PytorchConfigGene) otherNN);
        return offspring;
    }
}
