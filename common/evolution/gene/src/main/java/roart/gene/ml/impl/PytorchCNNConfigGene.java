package roart.gene.ml.impl;

import roart.common.ml.PytorchConfig;
import roart.gene.AbstractGene;
import roart.common.ml.PytorchCNNConfig;

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
    }
    
    @Override
    public void mutate() {
        PytorchCNNConfig myconfig = (PytorchCNNConfig) getConfig();
        int task = random.nextInt(RANDOMS + 2);
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
        default:
        }
     }

    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        PytorchCNNConfigGene offspring = new PytorchCNNConfigGene((PytorchCNNConfig) getConfig());
        ((PytorchPreFeedConfigGene) offspring).crossover(otherNN);
        PytorchCNNConfigGene other = (PytorchCNNConfigGene) otherNN;
        PytorchCNNConfig myconfig = (PytorchCNNConfig) getConfig();
        PytorchCNNConfig otherconfig = (PytorchCNNConfig) other.getConfig();
        if (random.nextBoolean()) {
            myconfig.setKernelsize(otherconfig.getKernelsize());
        }
        if (random.nextBoolean()) {
            myconfig.setStride(otherconfig.getStride());
        }
        return offspring;
    }

}
