package roart.gene.ml.impl;

import roart.common.ml.TensorflowCNNConfig;
import roart.common.ml.TensorflowConfig;
import roart.gene.AbstractGene;
import roart.gene.NeuralNetConfigGene;
import roart.common.constants.Constants;

public class TensorflowCNNConfigGene extends TensorflowPreFeedConfigGene {

    public TensorflowCNNConfigGene(TensorflowConfig config) {
        super(config);
     }
    
    public TensorflowCNNConfigGene() {
        // JSON
    }
    
    @Override
    public NeuralNetConfigGene copy() {
        return new TensorflowCNNConfigGene(new TensorflowCNNConfig((TensorflowCNNConfig) getConfig()));
    }

    @Override
    public void randomize() {
        super.randomize();
        TensorflowCNNConfig myconfig = (TensorflowCNNConfig) getConfig();
        myconfig.setKernelsize(generateKernelsize());
        myconfig.setStride(generateStride());
        myconfig.setDropout(generateDropout());
    }
    
    @Override
    public void mutate() {
        TensorflowCNNConfig myconfig = (TensorflowCNNConfig) getConfig();
        int task = random.nextInt(RANDOMS + 4);
        if (task < RANDOMS) {
            super.mutate(task);
            return;
        }
	task = task - RANDOMS;
        switch (task) {
        case 0:
            myconfig.setKernelsize(generateKernelsize());
            break;
        case 1:
            myconfig.setStride(generateStride());
            break;
        case 2:
            myconfig.setDropout(generateDropout());
            break;
        case 3:
            myconfig.setLr(generateLr());
            break;
        default:
	    log.error(Constants.NOTFOUND, task);
        }
     }

    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        TensorflowCNNConfigGene offspring = new TensorflowCNNConfigGene((TensorflowCNNConfig) getConfig());
        offspring.crossover((TensorflowConfigGene) otherNN);
        TensorflowCNNConfigGene other = (TensorflowCNNConfigGene) otherNN;
        TensorflowCNNConfig myconfig = (TensorflowCNNConfig) getConfig();
        TensorflowCNNConfig otherconfig = (TensorflowCNNConfig) other.getConfig();
        if (random.nextBoolean()) {
            myconfig.setKernelsize(otherconfig.getKernelsize());
        }
        if (random.nextBoolean()) {
            myconfig.setStride(otherconfig.getStride());
        }
        if (random.nextBoolean()) {
            myconfig.setDropout(otherconfig.getDropout());
        }
        if (random.nextBoolean()) {
            myconfig.setLr(otherconfig.getLr());
        }
        return offspring;
    }

}
