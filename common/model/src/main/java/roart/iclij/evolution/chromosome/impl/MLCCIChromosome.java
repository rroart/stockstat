package roart.iclij.evolution.chromosome.impl;

import roart.gene.impl.ConfigMapGene;

public class MLCCIChromosome extends MLAggregatorChromosome {

    public MLCCIChromosome() {
        super();
    }

    public MLCCIChromosome(ConfigMapGene gene) {
        super(gene);
    }

    @Override
    protected MLAggregatorChromosome getNewChromosome() {
        return new MLCCIChromosome(gene);
    }
    
}
