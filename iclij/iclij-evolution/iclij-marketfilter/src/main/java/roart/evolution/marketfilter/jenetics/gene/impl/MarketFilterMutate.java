package roart.evolution.marketfilter.jenetics.gene.impl;

import java.util.Random;

import io.jenetics.Chromosome;
import io.jenetics.Mutator;
import io.jenetics.MutatorResult;
import roart.iclij.config.MarketFilter;

public class MarketFilterMutate <C extends Comparable<? super C>>
extends Mutator<MarketFilterGene, C> {
    @Override
    protected MutatorResult<Chromosome<MarketFilterGene>> mutate(
            final Chromosome<MarketFilterGene> chromosome,
            final double p,
            final Random random
    ) {
            return MutatorResult.of(
                    chromosome.newInstance(chromosome.toSeq().map(this::mutate)),
                    chromosome.length()
            );
    }

    private MarketFilterGene mutate(final MarketFilterGene gene) {
        //new MarketFilterMutate().mutate(gene);
        roart.evolution.marketfilter.genetics.gene.impl.MarketFilterMutate mutate = new roart.evolution.marketfilter.genetics.gene.impl.MarketFilterMutate(); 
        MarketFilter filter = gene.getAllele();
        MarketFilter newFilter = new MarketFilter(filter.getInccategory(), filter.getIncdays(), filter.getIncthreshold(), filter.getDeccategory(), filter.getDecdays(), filter.getDecthreshold(), filter.getConfidence(), filter.getRecordage());
        roart.evolution.marketfilter.genetics.gene.impl.MarketFilterGene other = new roart.evolution.marketfilter.genetics.gene.impl.MarketFilterGene(newFilter);
        mutate.mutate(other);
        return gene.newInstance(other.getMarketfilter());
}
}
