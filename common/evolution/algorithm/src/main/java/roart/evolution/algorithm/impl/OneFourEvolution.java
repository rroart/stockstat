package roart.evolution.algorithm.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.evolution.algorithm.EvolutionAlgorithm;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.species.Individual;
import roart.common.pipeline.data.SerialList;
import roart.common.pipeline.data.SerialScoreChromosome;

import org.apache.commons.lang3.tuple.Pair;

public class OneFourEvolution extends EvolutionAlgorithm {

    public OneFourEvolution(EvolutionConfig evolutionConfig) {
        super(evolutionConfig);
    }
    
    @Override
    public Individual getFittest(EvolutionConfig evolutionConfig, AbstractChromosome recommend, List<String> individuals, List<SerialScoreChromosome> results, AbstractChromosome defaultChromosome) throws Exception {
        int selectionSize = evolutionConfig.getSelect();
        int four = 4;
        int five = 5;
        List<Individual> populationBuy = new ArrayList<>();
        List<Individual> populationSell = new ArrayList<>();
        int category = 0;
        String market = null; //"tradcomm";
        //List<Double> macdLists[] = new ArrayList[4];

        Individual parent = getBest(four, five, populationBuy, true, recommend);
        return parent;
    }

    private Individual getBest(int four, int five, List<Individual> population,
            boolean doBuy, AbstractChromosome chromosome) throws JsonParseException, JsonMappingException, IOException, InterruptedException, ExecutionException {
        for (int i = 0; i < five; i ++) {
            Individual buy = new Individual(chromosome).getNewWithValueCopyAndRandomFactory();
            population.add(buy);
            //printmap(buy.getConf().getConfigValueMap());
        }
        Collections.sort(population);
        if (!chromosome.isAscending()) {
            Collections.reverse(population);
        }

        //printmap(population.get(0).getConf().getConfigValueMap());
        //printmap(population.get(population.size() - 1).getConf().getConfigValueMap());

        for (int i = 0; i < getEvolutionConfig().getGenerations(); i++){
            Individual parent = population.get(0);
            population = new ArrayList<>();
            population.add(parent);
            for (int j = 0; j < four; j++) {
                Individual pop = new Individual(chromosome).getNewWithValueCopyFactory();
                pop.mutate();
                population.add(pop);
            }
            calculate(population);
            Collections.sort(population);
            if (!chromosome.isAscending()) {
                Collections.reverse(population);
            }

        }
        Collections.sort(population);
        if (!chromosome.isAscending()) {
            Collections.reverse(population);
        }
        //printmap(population.get(0).getConf().getConfigValueMap());
        //printmap(population.get(population.size() - 1).getConf().getConfigValueMap());
        return population.get(0);
    }

}
