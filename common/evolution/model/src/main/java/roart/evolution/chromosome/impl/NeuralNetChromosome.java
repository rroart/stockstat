package roart.evolution.chromosome.impl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.species.Individual;
import roart.gene.NeuralNetConfigGene;

public class NeuralNetChromosome extends AbstractChromosome {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private NeuralNetConfigGene nnConfigGene;
    
    public NeuralNetChromosome(NeuralNetConfigGene nnConfigGene) {
        this.nnConfigGene = nnConfigGene;
    }

    public NeuralNetChromosome(NeuralNetChromosome chromosome) {
        this(chromosome.nnConfigGene.copy());
    }

    public NeuralNetChromosome() {
    }

    public NeuralNetConfigGene getNnConfig() {
        return nnConfigGene;
    }

    public void setNnConfig(NeuralNetConfigGene nnConfig) {
        this.nnConfigGene = nnConfig;
    }

    @Override
    public double getEvaluations(int j) throws JsonParseException, JsonMappingException, IOException {
        return 0;
    }

    @Override
    public void mutate() {
        nnConfigGene.mutate();
    }

    @Override
    public void getRandom()
            throws JsonParseException, JsonMappingException, IOException {
        nnConfigGene.randomize();
    }

    @Override
    public void transformToNode()
            throws JsonParseException, JsonMappingException, IOException {
    }

    @Override
    public void normalize() {
    }

    @Override
    public void transformFromNode()
            throws JsonParseException, JsonMappingException, IOException {
    }

    @Override
    public Individual crossover(AbstractChromosome evaluation) {
        NeuralNetConfigGene newNNConfig =  (NeuralNetConfigGene) nnConfigGene.crossover(((NeuralNetChromosome) evaluation).nnConfigGene);
        NeuralNetChromosome eval = new NeuralNetChromosome(newNNConfig);
        return new Individual(eval);
    }

    @Override
    public AbstractChromosome copy() {
        return new NeuralNetChromosome(this);
    }
    
    @JsonIgnore
    @Override
    public boolean isEmpty() {
        return nnConfigGene == null || nnConfigGene.getConfig() == null || nnConfigGene.getConfig().empty();
    }
    
    @Override
    public String toString() {
        return "" + nnConfigGene;
    }

    @JsonIgnore
    @Override
    public double getFitness() throws JsonParseException, JsonMappingException, IOException {
        // TODO Auto-generated method stub
        return 0;
    }

}
