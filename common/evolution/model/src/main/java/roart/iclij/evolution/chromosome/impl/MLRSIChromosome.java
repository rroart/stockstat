package roart.iclij.evolution.chromosome.impl;

import roart.gene.impl.ConfigMapGene;

public class MLRSIChromosome extends MLAggregatorChromosome {

    public MLRSIChromosome() {
        super();
    }

    public MLRSIChromosome(ConfigMapGene gene) {
        super(gene);
    }

    @Override
    protected MLAggregatorChromosome getNewChromosome() {
        return new MLRSIChromosome(gene);
    }
    

}
