package roart.evolution.marketfilter.jenetics.gene.impl;

import io.jenetics.AbstractChromosome;
import io.jenetics.Chromosome;
import io.jenetics.util.ISeq;

public class ConfigMapChromosome extends AbstractChromosome<ConfigMapGene> {

    protected ConfigMapChromosome(ISeq<? extends ConfigMapGene> genes) {
        super(genes);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Chromosome<ConfigMapGene> newInstance(ISeq<ConfigMapGene> genes) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Chromosome<ConfigMapGene> newInstance() {
        // TODO Auto-generated method stub
        return null;
    }

}
