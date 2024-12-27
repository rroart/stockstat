package roart.iclij.evolution.chromosome.impl;

import roart.gene.impl.ConfigMapGene;

public class MLATRChromosome extends MLAggregatorChromosome {

    public MLATRChromosome(ConfigMapGene gene) {
        super(gene);
    }

    @Override
    protected MLAggregatorChromosome getNewChromosome() {
        return new MLATRChromosome(gene);
    }
    
}
