package roart.gene.ml.impl;

import roart.common.ml.PytorchConfig;
import roart.common.ml.PytorchLSTMConfig;
import roart.gene.AbstractGene;

public class PytorchLSTMConfigGene extends PytorchRecurrentConfigGene {

    public PytorchLSTMConfigGene(PytorchLSTMConfig config) {
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
        PytorchLSTMConfigGene offspring = new PytorchLSTMConfigGene((PytorchLSTMConfig) getConfig());
        offspring.crossover((PytorchConfigGene) otherNN);
        return offspring;
    }
}
