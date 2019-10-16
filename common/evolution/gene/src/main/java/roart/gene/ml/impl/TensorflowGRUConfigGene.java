package roart.gene.ml.impl;

import roart.common.ml.TensorflowGRUConfig;
import roart.gene.AbstractGene;

public class TensorflowGRUConfigGene extends TensorflowRecurrentConfigGene {
    public TensorflowGRUConfigGene(TensorflowGRUConfig config) {
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
        TensorflowGRUConfigGene offspring = new TensorflowGRUConfigGene((TensorflowGRUConfig) getConfig());
        offspring.crossover((TensorflowConfigGene) otherNN);
        return offspring;
    }
}
