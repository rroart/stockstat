package roart.evolution.species;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.evolution.config.EvolutionConfig;
import roart.evolution.model.Evaluation;

public class Population {
    private EvolutionConfig evolutionConfig;
    
    private List<Individual> population;
    
    @Deprecated
    public Population(int populationSize) {
        this.population = new ArrayList<>();
    }
    
    public Population(int populationSize, EvolutionConfig evolutionConfig, Evaluation evaluation, boolean doClone) throws JsonParseException, JsonMappingException, IOException {
        this.evolutionConfig = evolutionConfig;
        this.population = new ArrayList<>();

        for (int individualCount = 0; individualCount < populationSize; individualCount++) {

            Individual individual;
            if (doClone) {
                individual = new Individual(evaluation).getNewWithValueCopyFactory();
            } else {
                individual = new Individual(evaluation).getNewWithValueCopyAndRandomFactory();
            }
            this.population.add(individual);
        }
    }

    public List<Individual> getIndividuals() {
        return this.population;
    }
    
    public Individual getFittest() {
        Collections.sort(population);
        return this.population.get(0);
    }
    
    public int size() {
        return this.population.size();
    }
    
    public void shuffle() {
        Random rnd = new Random();
        for (int i = population.size() - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            Individual tmp = population.get(index);
            population.set(index, population.get(i));
            population.set(i, tmp);
        }
    }
    
    public void truncate(int min) {
        population = population.subList(0, Math.min(population.size(), evolutionConfig.getSelect()));
    }

}
