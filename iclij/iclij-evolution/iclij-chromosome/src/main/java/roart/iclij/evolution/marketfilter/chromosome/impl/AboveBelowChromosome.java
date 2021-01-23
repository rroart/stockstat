package roart.iclij.evolution.marketfilter.chromosome.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.species.Individual;

public class AboveBelowChromosome extends AbstractChromosome {
    private List<Boolean> genes;

    public AboveBelowChromosome(int size) {
        //this.incdecs = incdecs;
        Boolean[] booleans = new Boolean[size];
        Arrays.fill(booleans, false);
        genes = Arrays.asList(booleans);
    }

    public AboveBelowChromosome(List<Boolean> genes) {
        this.genes = genes;
    }

    public AboveBelowChromosome() {
        // Json
    }

    public List<Boolean> getGenes() {
        return genes;
    }

    public void setGenes(List<Boolean> genes) {
        this.genes = genes;
    }

    @Override
    public double getEvaluations(int j) throws JsonParseException, JsonMappingException, IOException {
        return 0;
    }

    @Override
    public void mutate() {
        int index = random.nextInt(genes.size());
        genes.set(index, !genes.get(index));
    }

    @Override
    public void getRandom() throws JsonParseException, JsonMappingException, IOException {
        for (int i = 0; i < genes.size(); i++) {
            genes.set(i, random.nextBoolean());
        }
    }

    @Override
    public void transformToNode() throws JsonParseException, JsonMappingException, IOException {
    }

    @Override
    public void normalize() {
    }

    @Override
    public void transformFromNode() throws JsonParseException, JsonMappingException, IOException {
    }

    @JsonIgnore
    @Override
    public double getFitness() throws JsonParseException, JsonMappingException, IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Individual crossover(AbstractChromosome chromosome) {
        List<Boolean> newGenes = new ArrayList<>();
        for (int i = 0; i < genes.size(); i++) {
            if (random.nextBoolean()) {
                newGenes.add(this.genes.get(i));
            } else {
                newGenes.add((Boolean) ((AboveBelowChromosome) chromosome).genes.get(i));
            }
        }
        AboveBelowChromosome newChromosome = new AboveBelowChromosome(newGenes);
        return new Individual(newChromosome);
    }

    @Override
    public AbstractChromosome copy() {
        return new AboveBelowChromosome(new ArrayList<>(genes));
    }

    @JsonIgnore
    @Override
    public boolean isEmpty() {
        return genes == null || genes.isEmpty();
    }

    @Override
    public String toString() {
        return "" + genes;
    }
}
