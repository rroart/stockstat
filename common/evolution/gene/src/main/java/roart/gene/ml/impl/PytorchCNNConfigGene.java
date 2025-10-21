package roart.gene.ml.impl;

import roart.common.ml.PytorchConfig;
import roart.common.ml.TensorflowCNNConfig;
import roart.gene.AbstractGene;
import roart.gene.NeuralNetConfigGene;
import roart.common.ml.PytorchCNNConfig;
import roart.common.constants.Constants;

public class PytorchCNNConfigGene extends PytorchPreFeedConfigGene {

    public PytorchCNNConfigGene(PytorchConfig config) {
        super(config);
    }
    
    public PytorchCNNConfigGene() {        
        // JSON
    }
    
    @Override
    public NeuralNetConfigGene copy() {
        return new PytorchCNNConfigGene(new PytorchCNNConfig((PytorchCNNConfig) getConfig()));
    }

    @Override
    public void randomize() {
        super.randomize();
        PytorchCNNConfig myconfig = (PytorchCNNConfig) getConfig();
        myconfig.setKernelsize(generateKernelsize());
        myconfig.setMaxpool(generateMaxpool());
        myconfig.setStride(generateStride());
    }
    
    @Override
    public void mutate() {
        PytorchCNNConfig myconfig = (PytorchCNNConfig) getConfig();
        int task = random.nextInt(RANDOMS + 3);
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
            myconfig.setMaxpool(generateMaxpool());
            break;
        default:
	    log.error(Constants.NOTFOUND, task);
        }
     }

    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        PytorchCNNConfigGene offspring = new PytorchCNNConfigGene((PytorchCNNConfig) getConfig());
        offspring.crossover((PytorchConfigGene) otherNN);
        PytorchCNNConfigGene other = (PytorchCNNConfigGene) otherNN;
        PytorchCNNConfig myconfig = (PytorchCNNConfig) getConfig();
        PytorchCNNConfig otherconfig = (PytorchCNNConfig) other.getConfig();
        if (random.nextBoolean()) {
            myconfig.setKernelsize(otherconfig.getKernelsize());
        }
        if (random.nextBoolean()) {
            myconfig.setMaxpool(otherconfig.getMaxpool());
        }
        if (random.nextBoolean()) {
            myconfig.setStride(otherconfig.getStride());
        }
        return offspring;
    }

}
