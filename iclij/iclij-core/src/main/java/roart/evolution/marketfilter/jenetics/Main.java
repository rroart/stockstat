package roart.evolution.marketfilter.jenetics;

import io.jenetics.AnyGene;
import io.jenetics.Genotype;
import io.jenetics.MeanAlterer;
import io.jenetics.Phenotype;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Codecs;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.RandomRegistry;
import roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterChromosome;
import roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterCrossover;
import roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterGene;
import roart.evolution.marketfilter.jenetics.gene.impl.MarketFilterMutate;
import roart.iclij.config.MarketFilter;

public class Main {
    public void main(final String[] args) {
        final Codec<MarketFilterChromosome, MarketFilterGene> codec = Codec.of(Genotype.of(new MarketFilterChromosome(new MarketFilter())),gt -> (MarketFilterChromosome) gt.getChromosome());
        final Engine<AnyGene<MarketFilterGene>, Double> engine = Engine
                .builder(this::fitness, codec)
                .alterers(
                        //new MeanAlterer<>(0.175),
                        new MarketFilterMutate(),
                        new MarketFilterCrossover<>(0.5)
                        )
                .build();

        final Phenotype<AnyGene<MarketFilterGene>, Double> pt = engine.stream()
                .limit(50)
                .collect(EvolutionResult.toBestPhenotype());

        System.out.println(pt);
    }

    public double fitness(final MarketFilterChromosome c) {
        //public double fitness(Genotype<AnyGene<MarketFilterGene>> g) {
        return 0.0;
    }
}
