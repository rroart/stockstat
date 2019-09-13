package roart.gene.ml.impl;

import roart.common.ml.PytorchGRUConfig;
import roart.gene.AbstractGene;

public class PytorchGRUConfigGene extends PytorchRecurrentConfigGene {

    public PytorchGRUConfigGene(PytorchGRUConfig config) {
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
        PytorchGRUConfigGene offspring = new PytorchGRUConfigGene((PytorchGRUConfig) getConfig());
        ((PytorchGRUConfigGene) offspring).crossover(otherNN);
        return offspring;
    }
}
