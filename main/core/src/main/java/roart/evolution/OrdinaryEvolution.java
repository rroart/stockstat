package roart.evolution;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.evolution.algorithm.EvolutionAlgorithm;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.model.Evaluation;
import roart.evolution.species.Individual;
import roart.evolution.species.Population;

public class OrdinaryEvolution extends EvolutionAlgorithm {

    public OrdinaryEvolution(EvolutionConfig evolutionConfig) {
        super(evolutionConfig);
    }
    
    @Override
    public Individual getFittest(EvolutionConfig evolutionConfig, Evaluation evaluation) throws Exception {
        int selectionSize = getEvolutionConfig().getSelect();
        Population population = new Population(selectionSize, evolutionConfig, evaluation, false);
        if (getEvolutionConfig().getUseoldelite() && !evaluation.isEmpty()) {
            population.getIndividuals().add(new Individual(evaluation).getNewWithValueCopyFactory());
        }
        calculate(population.getIndividuals());
        Collections.sort(population.getIndividuals());
        Individual parent = getBest(selectionSize, population, true, evaluation);
        parent.getEvaluation().transformFromNode();
        return parent;
    }

    private Individual getBest(int selectionSize, Population population,
            boolean useMax, Evaluation evaluation) throws JsonParseException, JsonMappingException, IOException, InterruptedException, ExecutionException {
        //printmap(population.getFittest().getConf().getConfigValueMap());
        //printmap(population.getIndividuals().get(population.size() - 1).getConf().getConfigValueMap());
        
        for (int i = 0; i < getEvolutionConfig().getGenerations(); i++){
            log.info("Iteration {} of {}", i, getEvolutionConfig().getGenerations());
            population.truncate(Math.min(population.size(), getEvolutionConfig().getSelect()));
           
            List<Individual> children = crossover(getEvolutionConfig().getCrossover(), population.getIndividuals(), useMax, evaluation);
            
            mutateList(population.getIndividuals(), getEvolutionConfig().getElite(), population.size(), getEvolutionConfig().getMutate(), false, useMax);
            List<Individual> clonedmutated = clonedmutated(getEvolutionConfig().getElitecloneandmutate(), population.getIndividuals().get(0).getEvaluation());
            mutateList(children, 0, population.size(), getEvolutionConfig().getMutate(), true, useMax);
            
            population.getIndividuals().addAll(children);
            population.getIndividuals().addAll(clonedmutated);
            
            List<Individual> created = created(getEvolutionConfig().getGenerationcreate(), evaluation);
            population.getIndividuals().addAll(created);
            calculate(population.getIndividuals());
            Collections.sort(population.getIndividuals());
        }
        printmap(population.getIndividuals());
        //printmap(population.getIndividuals().get(0).getConf().getConfigValueMap());
        //printmap(population.getIndividuals().get(population.size() - 1).getConf().getConfigValueMap());
        return population.getFittest();
    }
 }
