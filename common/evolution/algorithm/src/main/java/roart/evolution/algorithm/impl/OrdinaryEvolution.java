package roart.evolution.algorithm.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.evolution.algorithm.EvolutionAlgorithm;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.species.Individual;
import roart.evolution.species.Population;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class OrdinaryEvolution extends EvolutionAlgorithm {

    public OrdinaryEvolution(EvolutionConfig evolutionConfig) {
        super(evolutionConfig);
    }
    
    @Override
    public Individual getFittest(EvolutionConfig evolutionConfig, AbstractChromosome chromosome, List<String> individuals, List<Pair<Double, AbstractChromosome>> results) throws Exception {
        int selectionSize = getEvolutionConfig().getSelect();
        Population population = new Population(selectionSize, evolutionConfig, chromosome, false);
        if (getEvolutionConfig().getUseoldelite() && !chromosome.isEmpty()) {
            population.getIndividuals().add(new Individual(chromosome).getNewWithValueCopyFactory());
        }
        boolean interrupted = calculate(population.getIndividuals());
        if (interrupted && population.getIndividuals().get(0).getFitness() < 0) {
        	throw new InterruptedException();
        }
        Collections.sort(population.getIndividuals());
        if (!chromosome.isAscending()) {
            Collections.reverse(population.getIndividuals());
        }
        Individual parent = getBest(selectionSize, population, true, chromosome, individuals);
        parent.getEvaluation().transformFromNode();
        if (results != null) {
            for (Individual individual : population.getIndividuals()) {
                Pair<Double, AbstractChromosome> pair = new ImmutablePair<>(individual.getFitness(), individual.getEvaluation());
                results.add(pair);
            }
        }
        return parent;
    }

    private Individual getBest(int selectionSize, Population population,
            boolean useMax, AbstractChromosome chromosome, List<String> individuals) throws JsonParseException, JsonMappingException, IOException, InterruptedException, ExecutionException {
        //printmap(population.getFittest().getConf().getConfigValueMap());
        //printmap(population.getIndividuals().get(population.size() - 1).getConf().getConfigValueMap());
        
        for (int i = 0; i < getEvolutionConfig().getGenerations(); i++){
            log.debug("Iteration {} of {}", i, getEvolutionConfig().getGenerations());
            population.truncate(Math.min(population.size(), getEvolutionConfig().getSelect()));
           
            List<Individual> children = crossover(getEvolutionConfig().getCrossover(), population.getIndividuals(), useMax, chromosome);
            
            mutateList(population.getIndividuals(), getEvolutionConfig().getElite(), population.size(), getEvolutionConfig().getMutate(), false, useMax);
            List<Individual> clonedmutated = clonedmutated(getEvolutionConfig().getElitecloneandmutate(), population.getIndividuals().get(0).getEvaluation());
            mutateList(children, 0, population.size(), getEvolutionConfig().getMutate(), true, useMax);
            
            population.getIndividuals().addAll(children);
            population.getIndividuals().addAll(clonedmutated);
            
            List<Individual> created = created(getEvolutionConfig().getGenerationcreate(), chromosome);
            population.getIndividuals().addAll(created);
            boolean interrupted = calculate(population.getIndividuals());
            Collections.sort(population.getIndividuals());
            if (!chromosome.isAscending()) {
                Collections.reverse(population.getIndividuals());
            }
            if (interrupted) {
            	break;
            }
        }
        printmap(population.getIndividuals(), individuals);
        //printmap(population.getIndividuals().get(0).getConf().getConfigValueMap());
        //printmap(population.getIndividuals().get(population.size() - 1).getConf().getConfigValueMap());
        return population.getFittest();
    }
 }
