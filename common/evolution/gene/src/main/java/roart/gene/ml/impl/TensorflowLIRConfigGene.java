package roart.gene.ml.impl;

import roart.common.ml.TensorflowCNNConfig;
import roart.common.ml.TensorflowConfig;
import roart.common.ml.TensorflowLIRConfig;
import roart.gene.AbstractGene;
import roart.gene.NeuralNetConfigGene;

public class TensorflowLIRConfigGene extends TensorflowFeedConfigGene {

    public TensorflowLIRConfigGene(TensorflowConfig config, boolean predictor) {
        super(config);
        this.predictor = predictor;
    }

    public TensorflowLIRConfigGene() {        
        // JSON
    }
    
    @Override
    public NeuralNetConfigGene copy() {
        return new TensorflowLIRConfigGene(new TensorflowLIRConfig((TensorflowLIRConfig) getConfig()), predictor);
    }

    @Override
    public void mutate() {
        int task = random.nextInt(RANDOMS);
        super.mutate(task);

    }
    
    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        TensorflowLIRConfigGene offspring = new TensorflowLIRConfigGene((TensorflowLIRConfig) getConfig(), predictor);
        offspring.crossover((TensorflowConfigGene) otherNN);
        return offspring;
     }

}
