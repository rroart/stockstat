package roart.gene.ml.impl;

import roart.common.ml.TensorflowCNNConfig;
import roart.common.ml.TensorflowLICConfig;
import roart.gene.AbstractGene;
import roart.gene.NeuralNetConfigGene;

public class TensorflowLICConfigGene extends TensorflowFeedConfigGene {

    public TensorflowLICConfigGene(TensorflowLICConfig config) {
        super(config);
    }
    
    public TensorflowLICConfigGene() {        
        // JSON
    }
    
    @Override
    public NeuralNetConfigGene copy() {
        return new TensorflowLICConfigGene(new TensorflowLICConfig((TensorflowLICConfig) getConfig()));
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
