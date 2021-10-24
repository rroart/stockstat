package roart.iclij.evolve;

import java.util.Random;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.MutationPolicy;

import roart.evolution.iclijconfigmap.common.gene.impl.IclijConfigMapMutateCommon;
import roart.evolution.iclijconfigmap.genetics.gene.impl.IclijConfigMapGene;
import roart.iclij.evolution.fitness.impl.IclijConfigMapChromosome3;

public class IclijConfigMapMutateA implements MutationPolicy{

    @Override
    public Chromosome mutate(Chromosome original) throws MathIllegalArgumentException {
        IclijConfigMapGene gene2 = ((IclijConfigMapChromosome3)original).getGene();
        gene2.mutate();
        return original;
    }

}
