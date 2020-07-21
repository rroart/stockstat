package roart.evolution.chromosome.impl;

import org.apache.commons.lang3.tuple.Pair;

import roart.gene.impl.ConfigMapGene;

public class MLSTOCHChromosome extends MLAggregatorChromosome {

    public MLSTOCHChromosome(ConfigMapGene gene) {
        super(gene);
    }

    @Override
    protected MLAggregatorChromosome getNewChromosome() {
        return new MLSTOCHChromosome(gene);
    }
    
}
