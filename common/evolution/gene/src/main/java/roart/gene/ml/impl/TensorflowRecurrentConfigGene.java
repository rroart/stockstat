package roart.gene.ml.impl;

import roart.common.ml.TensorflowConfig;
import roart.common.ml.TensorflowRecurrentConfig;
import roart.common.constants.Constants;

public abstract class TensorflowRecurrentConfigGene extends TensorflowFeedConfigGene {

    public TensorflowRecurrentConfigGene(TensorflowConfig config) {
        super(config);
    }

    @Override
    public void randomize() {
        super.randomize();
        TensorflowRecurrentConfig myconfig = (TensorflowRecurrentConfig) getConfig();
        myconfig.setSlide(generateSlide());
        myconfig.setDropout(generateDropout());
        myconfig.setDropoutin(generateDropoutIn());
    }
    
    @Override
    public void mutate() {
        TensorflowRecurrentConfig myconfig = (TensorflowRecurrentConfig) getConfig();
        int task = random.nextInt(RANDOMS + 3 + 3);
        if (task < RANDOMS + 3) {
            super.mutate();
            return;
        }
	task = task - RANDOMS - 3;
        switch (task) {
        case 0:
            myconfig.setSlide(generateSlide());
            break;
        case 1:
            myconfig.setDropout(generateDropout());
            break;
        case 2:
            myconfig.setDropoutin(generateDropoutIn());
            break;
        default:
	    log.error(Constants.NOTFOUND, task);
        }
     }

    @Override
    public void crossover(TensorflowConfigGene other) {
        super.crossover(other);
        TensorflowRecurrentConfig myconfig = (TensorflowRecurrentConfig) getConfig();
        TensorflowRecurrentConfig otherconfig = (TensorflowRecurrentConfig) other.getConfig();
        if (random.nextBoolean()) {
            myconfig.setSlide(otherconfig.getSlide());
        }
        if (random.nextBoolean()) {
            myconfig.setDropout(otherconfig.getDropout());
        }
        if (random.nextBoolean()) {
            myconfig.setDropoutin(otherconfig.getDropoutin());
        }
    }
}
