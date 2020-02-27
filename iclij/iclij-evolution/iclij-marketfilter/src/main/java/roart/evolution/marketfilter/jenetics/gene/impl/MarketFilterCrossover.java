package roart.evolution.marketfilter.jenetics.gene.impl;

import java.util.Random;

import io.jenetics.Crossover;
import io.jenetics.Gene;
import io.jenetics.util.MSeq;
import io.jenetics.util.RandomRegistry;

public class MarketFilterCrossover <
G extends Gene<?, G>,
C extends Comparable<? super C>
>
extends Crossover<G, C>{
    public MarketFilterCrossover(final double probability) {
        super(probability);
    }
    @Override
    protected int crossover(final MSeq<G> that, final MSeq<G> other) {
        assert that.length() == other.length();

        int alteredGenes = 0;
        final Random random = RandomRegistry.getRandom();
        for (int i = 0; i < that.length(); ++i) {
            if (random.nextFloat() < getProbability()) {
                G oneThat = that.get(i);
                G oneOther = other.get(i);
                //oneThat.
                // copy oneThat
                //new roart.evolution.marketfilter.gene.impl.MarketFilterGene().crossover(oneThat, oneOther);
                crossover(that, other, i);
                ++alteredGenes;
            }
        }

        return alteredGenes;
    }
    // Package private for testing purpose.
    static <T> void crossover(
            final MSeq<T> that,
            final MSeq<T> other,
            final int index
            ) {
        /*
            assert index >= 0 : format(
                    "Crossover index must be within [0, %d) but was %d",
                    that.length(), index
            );
         */
        that.swap(index, index + 1, other, index);
    }


}
