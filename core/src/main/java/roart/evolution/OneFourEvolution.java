package roart.evolution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.config.EvolutionConfig;
import roart.evaluation.Evaluation;

public class OneFourEvolution extends EvolutionAlgorithm {

    public OneFourEvolution(EvolutionConfig evolutionConfig) {
        super(evolutionConfig);
    }
    
    @Override
    public Individual getFittest(EvolutionConfig evolutionConfig, Evaluation recommend) throws Exception {
        int selectionSize = evolutionConfig.getSelect();
        int four = 4;
        int five = 5;
        List<Individual> populationBuy = new ArrayList<>();
        List<Individual> populationSell = new ArrayList<>();
        int category = 0;
        String market = null; //"tradcomm";
        //List<Double> macdLists[] = new ArrayList[4];

        // TODO clone config

        Individual parent = getBest(four, five, populationBuy, true, recommend);
        return parent;
    }

    private Individual getBest(int four, int five, List<Individual> population,
            boolean doBuy, Evaluation recommend) throws JsonParseException, JsonMappingException, IOException, InterruptedException, ExecutionException {
        for (int i = 0; i < five; i ++) {
            Individual buy = new Individual(recommend).getNewWithValueCopyAndRandomFactory();
            population.add(buy);
            //printmap(buy.getConf().getConfigValueMap());
        }
        Collections.sort(population);

        //printmap(population.get(0).getConf().getConfigValueMap());
        //printmap(population.get(population.size() - 1).getConf().getConfigValueMap());

        for (int i = 0; i < getEvolutionConfig().getGenerations(); i++){
            Individual parent = population.get(0);
            population = new ArrayList<>();
            population.add(parent);
            for (int j = 0; j < four; j++) {
                Individual pop = new Individual(recommend).getNewWithValueCopyFactory();
                pop.mutate();
                population.add(pop);
            }
            calculate(population);
            Collections.sort(population);

        }
        Collections.sort(population);
        //printmap(population.get(0).getConf().getConfigValueMap());
        //printmap(population.get(population.size() - 1).getConf().getConfigValueMap());
        return population.get(0);
    }

}
