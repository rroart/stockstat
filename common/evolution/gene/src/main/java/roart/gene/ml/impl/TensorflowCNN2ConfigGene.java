package roart.gene.ml.impl;

import roart.common.constants.Constants;
import roart.common.ml.TensorflowCNN2Config;
import roart.common.ml.TensorflowConfig;
import roart.gene.AbstractGene;
import roart.gene.NeuralNetConfigGene;

public class TensorflowCNN2ConfigGene extends TensorflowPreFeedConfigGene {
    public TensorflowCNN2ConfigGene(TensorflowConfig config) {
        super(config);
     }
    
    @Override
    public NeuralNetConfigGene copy() {
        return new TensorflowCNN2ConfigGene(new TensorflowCNN2Config((TensorflowCNN2Config) getConfig()));
    }

    @Override
    public void randomize() {
        super.randomize();
        TensorflowCNN2Config myconfig = (TensorflowCNN2Config) getConfig();
        myconfig.setKernelsize(generateKernelsize());
        myconfig.setMaxpool(generateMaxpool());
        myconfig.setStride(generateStride());
        myconfig.setDropout1(generateDropout());
        myconfig.setDropout2(generateDropout());
    }
    
    @Override
    public void mutate() {
        TensorflowCNN2Config myconfig = (TensorflowCNN2Config) getConfig();
        int task = random.nextInt(RANDOMS + 5);
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
            myconfig.setDropout1(generateDropout());
            break;
        case 3:
            myconfig.setDropout2(generateDropout());
            break;
        case 4:
            myconfig.setMaxpool(generateMaxpool());
            break;
        default:
            log.error(Constants.NOTFOUND, task);
        }
     }

    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        TensorflowCNN2ConfigGene offspring = new TensorflowCNN2ConfigGene((TensorflowCNN2Config) getConfig());
        offspring.crossover((TensorflowConfigGene) otherNN);
        TensorflowCNN2ConfigGene other = (TensorflowCNN2ConfigGene) otherNN;
        TensorflowCNN2Config myconfig = (TensorflowCNN2Config) getConfig();
        TensorflowCNN2Config otherconfig = (TensorflowCNN2Config) other.getConfig();
        if (random.nextBoolean()) {
            myconfig.setKernelsize(otherconfig.getKernelsize());
        }
        if (random.nextBoolean()) {
            myconfig.setMaxpool(otherconfig.getMaxpool());
        }
        if (random.nextBoolean()) {
            myconfig.setStride(otherconfig.getStride());
        }
        if (random.nextBoolean()) {
            myconfig.setDropout1(otherconfig.getDropout1());
        }
        if (random.nextBoolean()) {
            myconfig.setDropout1(otherconfig.getDropout2());
        }
        return offspring;
    }

}
