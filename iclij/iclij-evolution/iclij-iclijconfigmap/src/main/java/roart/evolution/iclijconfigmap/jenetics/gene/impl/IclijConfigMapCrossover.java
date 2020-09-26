package roart.evolution.iclijconfigmap.jenetics.gene.impl;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import io.jenetics.Crossover;
import io.jenetics.Gene;
import io.jenetics.util.MSeq;
import io.jenetics.util.RandomRegistry;
import roart.evolution.iclijconfigmap.common.gene.impl.IclijConfigMapCrossoverCommon;
import roart.evolution.iclijconfigmap.genetics.gene.impl.IclijConfigMapGene;

public class IclijConfigMapCrossover <
G extends Gene<?, G>,
C extends Comparable<? super C>
>
extends Crossover<G, C>{

    public IclijConfigMapCrossover(double probability) {
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
                //crossover(that, other, i);
                ++alteredGenes;
                Map mapone = (Map) oneThat.getAllele();
                Map maptwo = (Map) oneOther.getAllele();
                Set<String> keys = mapone.keySet();
                for (String key : keys) {
                    if (random.nextFloat() < getProbability()) {
                        Object value1 = mapone.get(key);
                        Object value2 = maptwo.get(key);
                        mapone.put(key, value2);
                        maptwo.put(key, value1);
                    }
                }
                //Map<String, Object> neww = new IclijConfigMapCrossoverCommon().crossover(getConfList(), mapone, maptwo);
                //newGene.map = neww;
            }
        }

        return alteredGenes;
    }

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

    //@Override
    protected int crossover2(MSeq<G> that, MSeq<G> other) {
        assert that.length() == other.length();
        //that.
        //Map<String, Object> neww = new IclijConfigMapCrossoverCommon().crossover(getConfList(), map, ((IclijConfigMapGene) other).map);
        //newGene.map = neww;
        return 0;
    }

}
