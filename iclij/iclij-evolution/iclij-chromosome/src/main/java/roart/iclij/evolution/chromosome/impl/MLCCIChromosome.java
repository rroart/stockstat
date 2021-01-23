package roart.iclij.evolution.chromosome.impl;

import org.apache.commons.lang3.tuple.Pair;

import roart.gene.impl.ConfigMapGene;

public class MLCCIChromosome extends MLAggregatorChromosome {

    public MLCCIChromosome(ConfigMapGene gene) {
        super(gene);
    }

    @Override
    protected MLAggregatorChromosome getNewChromosome() {
        return new MLCCIChromosome(gene);
    }
    
}
