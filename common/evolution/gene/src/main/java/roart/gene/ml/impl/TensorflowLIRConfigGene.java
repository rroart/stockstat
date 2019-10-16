package roart.gene.ml.impl;

import roart.common.ml.TensorflowConfig;
import roart.common.ml.TensorflowLIRConfig;
import roart.gene.AbstractGene;

public class TensorflowLIRConfigGene extends TensorflowEstimatorConfigGene {

    public TensorflowLIRConfigGene(TensorflowConfig config) {
        super(config);
    }

    @Override
    public void mutate() {
        int task = random.nextInt(RANDOMS);
        super.mutate(task);

    }
    
    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        TensorflowLIRConfigGene offspring = new TensorflowLIRConfigGene((TensorflowLIRConfig) getConfig());
        offspring.crossover((TensorflowConfigGene) otherNN);
        return offspring;
     }

}
