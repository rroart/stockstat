package roart.gene.ml.impl;

import roart.common.ml.TensorflowConfig;
import roart.common.ml.TensorflowPredictorLSTMConfig;
import roart.gene.AbstractGene;
import roart.gene.NeuralNetConfigGene;
import roart.common.constants.Constants;

public class TensorflowPredictorLSTMConfigGene extends NeuralNetConfigGene {
    
    private static final int MAX_EPOCHS = 10;
        
    private static final int MAX_WINDOWSIZE = 10;
        
    private static final int MAX_HORIZON = 10;

    public TensorflowPredictorLSTMConfigGene(TensorflowPredictorLSTMConfig config) {
        super(config);
    }

    public void mutate(int task) {
        TensorflowConfig myconfig = (TensorflowConfig) getConfig();
        switch (task) {
        case 0:
            myconfig.setSteps(generateSteps());
            break;
        default:
	    log.error(Constants.NOTFOUND, task);
        }
    }

    private void generateEpochs() {
        TensorflowPredictorLSTMConfig myconfig = (TensorflowPredictorLSTMConfig) getConfig();
        myconfig.setEpochs(1 + random.nextInt(MAX_EPOCHS));
    }
    
    private void generateWindowsize() {
        TensorflowPredictorLSTMConfig myconfig = (TensorflowPredictorLSTMConfig) getConfig();
        myconfig.setWindowsize(2 + random.nextInt(MAX_WINDOWSIZE - 1));
    }
    
    private void generateHorizon() {
        TensorflowPredictorLSTMConfig myconfig = (TensorflowPredictorLSTMConfig) getConfig();
        myconfig.setHorizon(1 + random.nextInt(MAX_HORIZON));
    }

    @Override
    public void randomize() {
        TensorflowPredictorLSTMConfig myconfig = (TensorflowPredictorLSTMConfig) getConfig();
        generateEpochs();
        generateWindowsize();
        if (myconfig.full) {
            generateHorizon();
        }
    }

    @Override
    public void mutate() {
        TensorflowPredictorLSTMConfig myconfig = (TensorflowPredictorLSTMConfig) getConfig();
        int task = random.nextInt(2);
        if (myconfig.full) {
            task = random.nextInt(3);
        }
        switch (task) {
        case 0:
            generateEpochs();
            break;
        case 1:
            generateWindowsize();
            break;
        case 2:
            generateHorizon();
            break;
	default:
	    log.error(Constants.NOTFOUND, task);
        }
    }

    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        TensorflowPredictorLSTMConfigGene offspring = new TensorflowPredictorLSTMConfigGene((TensorflowPredictorLSTMConfig) getConfig());
        TensorflowPredictorLSTMConfig myconfig = (TensorflowPredictorLSTMConfig) getConfig();
        TensorflowPredictorLSTMConfig otherconfig = (TensorflowPredictorLSTMConfig) ((NeuralNetConfigGene)otherNN).getConfig();
        myconfig.full = otherconfig.full;
        if (random.nextBoolean()) {
            myconfig.setEpochs(otherconfig.getEpochs());
        }
        if (random.nextBoolean()) {
            myconfig.setWindowsize(otherconfig.getWindowsize());
        }
        if (myconfig.full && random.nextBoolean()) {
            myconfig.setHorizon(otherconfig.getHorizon());
        }
        return offspring;
    }

}

