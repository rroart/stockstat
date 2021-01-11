package roart.gene.ml.impl;

import roart.common.ml.GemSConfig;
import roart.common.ml.GemSConfig;
import roart.gene.AbstractGene;
import roart.gene.NeuralNetConfigGene;

public class GemSConfigGene extends GemConfigGene {

    public GemSConfigGene(GemSConfig config) {
        super(config);
    }

    public GemSConfigGene() {        
        // JSON
    }
    
    @Override
    public NeuralNetConfigGene copy() {
        return new GemSConfigGene(new GemSConfig((GemSConfig) getConfig()));
    }

    @Override
    public void randomize() {
        super.randomize();
    }

    @Override
    public void mutate() {
        int task = random.nextInt(RANDOMS);
        super.mutate(task);

    }

    @Override
    public AbstractGene crossover(AbstractGene otherNN) {
        GemSConfigGene offspring = new GemSConfigGene((GemSConfig) getConfig());
        offspring.crossover((GemConfigGene) otherNN);
        return offspring;
    }

}
