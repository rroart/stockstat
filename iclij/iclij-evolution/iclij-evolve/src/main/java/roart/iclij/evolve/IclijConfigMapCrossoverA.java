package roart.iclij.evolve;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.ChromosomePair;
import org.apache.commons.math3.genetics.CrossoverPolicy;

import roart.evolution.iclijconfigmap.genetics.gene.impl.IclijConfigMapGene;
import roart.iclij.evolution.fitness.impl.IclijConfigMapChromosome3;

public class IclijConfigMapCrossoverA implements CrossoverPolicy {

    @Override
    public ChromosomePair crossover(Chromosome first, Chromosome second) throws MathIllegalArgumentException {
        /*
        IclijConfigMapGene newNNConfig =  (IclijConfigMapGene) gene.crossover(((IclijConfigMapChromosome3) chromosome).gene);
        IclijConfigMapChromosome3 eval = new IclijConfigMapChromosome3(newNNConfig);
        //MarketFilterChromosome eval = new MarketFilterChromosome(conf, ml, dataReaders, categories, key, newNNConfig, catName, cat, neuralnetcommand);
        return new Individual(eval);
        */
        // TODO Auto-generated method stub
        return new ChromosomePair(first, second);
    }

}
