package roart.gene.ml.impl;

import roart.common.ml.TensorflowCNNConfig;
import roart.common.ml.TensorflowConfig;
import roart.common.ml.TensorflowDNNConfig;
import roart.gene.AbstractGene;
import roart.gene.NeuralNetConfigGene;
import roart.common.constants.Constants;

public class TensorflowDNNConfigGene extends TensorflowFeedConfigGene {

    public TensorflowDNNConfigGene(TensorflowConfig config) {
        super(config);
    }
   
    public TensorflowDNNConfigGene() {        
        // JSON
    }
    
    @Override
    public NeuralNetConfigGene copy() {
        return new TensorflowDNNConfigGene(new TensorflowDNNConfig((TensorflowDNNConfig) getConfig()));
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
        int task = random.nextInt(RANDOMS + 2);
        if (task < RANDOMS) {
            super.mutate(task);
            return;
        }
        task = task - RANDOMS;
        switch (task) {
        case 0:
            myconfig.setHidden(generateHidden());
            break;
        case 1:
            myconfig.setLayers(generateLayers());
            break;
        default:
	    log.error(Constants.NOTFOUND, task);
        }
    }

    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        TensorflowDNNConfigGene offspring = new TensorflowDNNConfigGene((TensorflowDNNConfig) getConfig());
        offspring.crossover((TensorflowConfigGene) otherNN);
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
