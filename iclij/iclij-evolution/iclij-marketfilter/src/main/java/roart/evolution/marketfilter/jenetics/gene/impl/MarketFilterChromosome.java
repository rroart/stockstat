package roart.evolution.marketfilter.jenetics.gene.impl;

import io.jenetics.AbstractChromosome;
import io.jenetics.Chromosome;
import io.jenetics.util.ISeq;
import roart.iclij.config.MarketFilter;

public class MarketFilterChromosome extends AbstractChromosome<MarketFilterGene> {

    public MarketFilterChromosome(ISeq<? extends MarketFilterGene> genes) {
        super(genes);
    }

    public MarketFilterChromosome(MarketFilter filter) {
        super(MarketFilterGene.seq(1, filter));
    }
    
    @Override
    public Chromosome<MarketFilterGene> newInstance(ISeq<MarketFilterGene> genes) {
        return new MarketFilterChromosome(genes);
    }

    @Override
    public Chromosome<MarketFilterGene> newInstance() {
        int i = length();
        return new MarketFilterChromosome(getGene().getAllele());
    }

}
