package roart.gene.ml.impl;

import roart.common.ml.PytorchConfig;
import roart.gene.AbstractGene;
import roart.common.ml.PytorchCNNConfig;
import roart.common.constants.Constants;

public class PytorchCNNConfigGene extends PytorchPreFeedConfigGene {

    public PytorchCNNConfigGene(PytorchConfig config) {
        super(config);
    }
    
    @Override
    public void randomize() {
        super.randomize();
        PytorchCNNConfig myconfig = (PytorchCNNConfig) getConfig();
        myconfig.setKernelsize(generateKernelsize());
        myconfig.setStride(generateStride());
        myconfig.setLr(generateLr());
    }
    
    @Override
    public void mutate() {
        PytorchCNNConfig myconfig = (PytorchCNNConfig) getConfig();
        int task = random.nextInt(RANDOMS + 3);
        if (task < RANDOMS) {
            super.mutate(task);
            return;
        }
        switch (task) {
        case 0:
            myconfig.setKernelsize(generateKernelsize());
            break;
        case 1:
            myconfig.setStride(generateStride());
            break;
        case 2:
            myconfig.setLr(generateLr());
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
            myconfig.setStride(otherconfig.getStride());
        }
        if (random.nextBoolean()) {
            myconfig.setLr(otherconfig.getLr());
        }
        return offspring;
    }

}
