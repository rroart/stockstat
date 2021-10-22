package roart.iclij.evolution.fitness.impl;

import java.io.IOException;

import org.apache.commons.math3.genetics.Chromosome;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.evolution.iclijconfigmap.genetics.gene.impl.IclijConfigMapGene;

public class IclijConfigMapChromosome3 extends Chromosome {

    private IclijConfigMapGene gene;
    private FitnessIclijConfigMap3 fitness;

    public IclijConfigMapChromosome3(IclijConfigMapGene gene, FitnessIclijConfigMap3 fit) {
        this.gene = gene;
        this.fitness = fit;
    }

    public double fitness() {
        int f = 0; // start at 0; the best fitness
        return fitness.fitness(this);
    }

    public IclijConfigMapGene getGene() {
        return gene;
    }

    public void setGene(IclijConfigMapGene gene) {
        this.gene = gene;
    }

    public void getRandom() throws JsonParseException, JsonMappingException, IOException {
        gene.randomize();
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        return String.format("(f=%s '%s')", getFitness(), sb.toString());
    }

}

