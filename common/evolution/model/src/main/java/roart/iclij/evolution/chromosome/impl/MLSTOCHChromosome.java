package roart.iclij.evolution.chromosome.impl;

import roart.gene.impl.ConfigMapGene;

public class MLSTOCHChromosome extends MLAggregatorChromosome {

    public MLSTOCHChromosome() {
        super();
    }

    public MLSTOCHChromosome(ConfigMapGene gene) {
        super(gene);
    }

    @Override
    protected MLAggregatorChromosome getNewChromosome() {
        return new MLSTOCHChromosome(gene);
    }
    
}
