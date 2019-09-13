package roart.gene.ml.impl;

import roart.common.ml.TensorflowConfig;
import roart.common.ml.TensorflowDNNConfig;
import roart.gene.AbstractGene;

public class TensorflowDNNConfigGene extends TensorflowEstimatorConfigGene {

    protected static final int RANDOMS = 2;
    
    public TensorflowDNNConfigGene(TensorflowConfig config) {
        super(config);
    }
   
    @Override
    public void randomize() {
        super.randomize();
        TensorflowDNNConfig myconfig = (TensorflowDNNConfig) getConfig();
        myconfig.setHidden(generateHidden());
        myconfig.setLayers(generateLayers());
    }

    @Override
    public void mutate() {
        TensorflowDNNConfig myconfig = (TensorflowDNNConfig) getConfig();
        int task = random.nextInt(RANDOMS);
        if (task < RANDOMS) {
            super.mutate(task);
            return;
        }
        switch (task) {
        case 0:
            myconfig.setHidden(generateHidden());
            break;
        case 1:
            myconfig.setLayers(generateLayers());
            break;
        default:
        }
    }

    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        TensorflowDNNConfigGene offspring = new TensorflowDNNConfigGene((TensorflowDNNConfig) getConfig());
        ((TensorflowDNNConfigGene) offspring).crossover(otherNN);
        TensorflowDNNConfig myconfig = (TensorflowDNNConfig) getConfig();
        TensorflowDNNConfig otherconfig = (TensorflowDNNConfig) ((TensorflowDNNConfigGene) otherNN).getConfig();
        if (random.nextBoolean()) {
            myconfig.setHidden(otherconfig.getHidden());
        }
        if (random.nextBoolean()) {
            myconfig.setLayers(otherconfig.getLayers());
        }
        return offspring;
    }

}
