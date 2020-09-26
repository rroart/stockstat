package roart.evolution.iclijconfigmap.jenetics.gene.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import io.jenetics.Chromosome;
import io.jenetics.Mutator;
import io.jenetics.MutatorResult;
import roart.evolution.iclijconfigmap.common.gene.impl.IclijConfigMapMutateCommon;

public final class IclijConfigMapMutate <C extends Comparable<? super C>>
extends Mutator<IclijConfigMapGene, C>{

    @Override
    protected MutatorResult<Chromosome<IclijConfigMapGene>> mutate(
            final Chromosome<IclijConfigMapGene> chromosome,
            final double p,
            final Random random
    ) {
            return MutatorResult.of(
                    chromosome.newInstance(chromosome.toSeq().map(this::mutate)),
                    chromosome.length()
            );
    }

    private IclijConfigMapGene mutate(final IclijConfigMapGene gene) {
        //new MarketFilterMutate().mutate(gene);
        //IclijConfigMapMutateCommon mutate = new IclijConfigMapMutateCommon();
        //Map<String, Object> filter = gene.getAllele();
        //Map<String, Object> newFilter = new HashMap<>(filter);
        //roart.evolution.marketfilter.genetics.gene.impl.MarketFilterGene other = new roart.evolution.marketfilter.genetics.gene.impl.MarketFilterGene(newFilter, filter.categories);
        //mutate.mutate(other);
        //if (true) return gene.newInstance(other.getMarketfilter());
        IclijConfigMapGene gene2 = gene.newInstance(gene.getMap());
        int conf = new Random().nextInt(gene.getConfList().size());
        new IclijConfigMapMutateCommon().generateConfigNum(new Random(), conf, gene2.getConfList(), gene2.getConf(), gene2.getMap());
        return gene2;
}
}
