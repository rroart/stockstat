package roart.component;

import io.jenetics.BitChromosome;
import io.jenetics.BitGene;
import io.jenetics.Chromosome;
import io.jenetics.Genotype;

public class FitnessAboveBelow2 {
    public synchronized Double fitness3(Genotype<BitGene> gt) {
        Chromosome<BitGene> c = gt.chromosome();
        c.get(0).allele();
        return null;
    }
    public synchronized Double fitness2(BitChromosome chromosome) {
        return null;
    }
    public synchronized Integer fitness1(BitChromosome chromosome) {
        return null;
    }
    public synchronized BitGene fitness(BitChromosome chromosome) {
        return null;
    }
}
