package roart.gene.ml.impl;

import roart.common.ml.PytorchConfig;
import roart.common.ml.PytorchRecurrentConfig;

public abstract class PytorchRecurrentConfigGene extends PytorchFeedConfigGene {

    public PytorchRecurrentConfigGene(PytorchConfig config) {
        super(config);
    }

    @Override
    public void randomize() {
        super.randomize();
        PytorchRecurrentConfig myconfig = (PytorchRecurrentConfig) getConfig();
        myconfig.setSlide(generateSlide());
    }

    @Override
    public void mutate() {
        PytorchRecurrentConfig myconfig = (PytorchRecurrentConfig) getConfig();
        int task = random.nextInt(RANDOMS + 1);
        if (task < RANDOMS) {
            super.mutate(task);
            return;
        }
        switch (task) {
        case 0:
            myconfig.setSlide(generateStride());
            break;
        default:
        }
    }

    @Override
    public void crossover(PytorchConfigGene other) {
        super.crossover(other);
        PytorchRecurrentConfig myconfig = (PytorchRecurrentConfig) getConfig();
        PytorchRecurrentConfig otherconfig = (PytorchRecurrentConfig) other.getConfig();
        if (random.nextBoolean()) {
            myconfig.setSlide(otherconfig.getSlide());
        }
    }
}
