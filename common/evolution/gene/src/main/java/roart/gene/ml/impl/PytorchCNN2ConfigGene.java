package roart.gene.ml.impl;

import roart.common.constants.Constants;
import roart.common.ml.PytorchCNN2Config;
import roart.common.ml.PytorchConfig;
import roart.gene.AbstractGene;
import roart.gene.NeuralNetConfigGene;

public class PytorchCNN2ConfigGene extends PytorchPreFeedConfigGene {
    public PytorchCNN2ConfigGene(PytorchConfig config) {
        super(config);
    }
    
    public PytorchCNN2ConfigGene() {        
        // JSON
    }
    
    @Override
    public NeuralNetConfigGene copy() {
        return new PytorchCNN2ConfigGene(new PytorchCNN2Config((PytorchCNN2Config) getConfig()));
    }

    @Override
    public void randomize() {
        super.randomize();
        PytorchCNN2Config myconfig = (PytorchCNN2Config) getConfig();
        myconfig.setKernelsize(generateKernelsize());
        myconfig.setMaxpool(generateMaxpool());
        myconfig.setStride(generateStride());
    }
    
    @Override
    public void mutate() {
        PytorchCNN2Config myconfig = (PytorchCNN2Config) getConfig();
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
        PytorchCNN2ConfigGene offspring = new PytorchCNN2ConfigGene((PytorchCNN2Config) getConfig());
        offspring.crossover((PytorchConfigGene) otherNN);
        PytorchCNN2ConfigGene other = (PytorchCNN2ConfigGene) otherNN;
        PytorchCNN2Config myconfig = (PytorchCNN2Config) getConfig();
        PytorchCNN2Config otherconfig = (PytorchCNN2Config) other.getConfig();
        if (random.nextBoolean()) {
            myconfig.setKernelsize(otherconfig.getKernelsize());
        }
        if (random.nextBoolean()) {
            myconfig.setStride(otherconfig.getStride());
        }
        if (random.nextBoolean()) {
            myconfig.setMaxpool(otherconfig.getMaxpool());
        }
        return offspring;
    }

}
