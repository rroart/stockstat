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
        myconfig.setDropout1(generateDropout());
        myconfig.setDropout2(generateDropout());
        myconfig.setLr(generateLr());
    }
    
    @Override
    public void mutate() {
        PytorchCNN2Config myconfig = (PytorchCNN2Config) getConfig();
        int task = random.nextInt(RANDOMS + 6);
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
            myconfig.setLr(generateLr());
            break;
        case 3:
            myconfig.setMaxpool(generateMaxpool());
            break;
        case 4:
            myconfig.setDropout1(generateDropout());
            break;
        case 5:
            myconfig.setDropout2(generateDropout());
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
            myconfig.setLr(otherconfig.getLr());
        }
        if (random.nextBoolean()) {
            myconfig.setMaxpool(otherconfig.getMaxpool());
        }
        if (random.nextBoolean()) {
            myconfig.setDropout1(otherconfig.getDropout1());
        }
        if (random.nextBoolean()) {
            myconfig.setDropout2(otherconfig.getDropout2());
        }
        return offspring;
    }

}
