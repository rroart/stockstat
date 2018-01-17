package roart.evolution;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.config.MyMyConfig;
import roart.evaluation.Evaluation;
import roart.util.TaUtil;

public class OrdinaryEvolution extends EvolutionAlgorithm {

    @Override
    public Individual getFittest(MyMyConfig conf,Evaluation evaluation) throws Exception {
        int selectionSize = conf.getEvolutionSelect();
        List<String> keys = evaluation.getKeys();
        Population population = new Population(selectionSize, conf, evaluation, keys, false);

        // TODO clone config

        
        Individual parent = getBest(conf, selectionSize, keys, population, true, evaluation);
        evaluation.transformFromNode(parent.conf, keys);
        return parent;
    }

    private Individual getBest(MyMyConfig conf, int selectionSize, List<String> keyList, Population population,
            boolean useMax, Evaluation evaluation) throws JsonParseException, JsonMappingException, IOException {
        printmap(population.getFittest().conf.configValueMap, keyList);
        printmap(population.getIndividuals().get(population.size() - 1).conf.configValueMap, keyList);
        
        for (int i = 0; i < conf.getEvolutionGenerations(); i++){
            population.truncate(Math.min(population.size(), conf.getEvolutionSelect()));
           
            List<Individual> children = crossover(conf.getEvolutionCrossover(), population.getIndividuals(), keyList, conf, false, useMax, evaluation);
            
            mutateList(population.getIndividuals(), conf.getEvolutionElite(), population.size(), conf.getEvolutionMutate(), false, keyList, useMax);
            List<Individual> clonedmutated = clonedmutated(conf.getEvolutionEliteCloneAndMutate(), conf, evaluation, keyList);
            mutateList(children, 0, population.size(), conf.getEvolutionMutate(), true, keyList, useMax);
            
            population.getIndividuals().addAll(children);

            List<Individual> created = created(conf.getEvolutionGenerationCreate(), conf, evaluation, keyList);
            population.getIndividuals().addAll(created);
            
        }
        printmap(population.getIndividuals().get(0).conf.configValueMap, keyList);
        printmap(population.getIndividuals().get(population.size() - 1).conf.configValueMap, keyList);
        return population.getFittest();
    }

 }
