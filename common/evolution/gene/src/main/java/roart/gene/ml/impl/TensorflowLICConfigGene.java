package roart.gene.ml.impl;

import roart.common.ml.TensorflowLICConfig;
import roart.gene.AbstractGene;

public class TensorflowLICConfigGene extends TensorflowEstimatorConfigGene {

    public TensorflowLICConfigGene(TensorflowLICConfig config) {
        super(config);
    }
    
    @Override
    public void mutate() {
        int task = random.nextInt(RANDOMS);
        super.mutate(task);

    }
    
    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        TensorflowLICConfigGene offspring = new TensorflowLICConfigGene((TensorflowLICConfig) getConfig());
        offspring.crossover((TensorflowConfigGene) otherNN);
        return offspring;
     }

}
