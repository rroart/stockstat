package roart.evolution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.config.MyMyConfig;
import roart.evaluation.Evaluation;

public class OneFourEvolution extends EvolutionAlgorithm {

    @Override
    public Individual getFittest(MyMyConfig conf,Evaluation recommend) throws Exception {
        int selectionSize = conf.getEvolutionSelect();
        int four = 4;
        int five = 5;
        List<String> buyList = recommend.getKeys();
        List<Individual> populationBuy = new ArrayList<>();
        List<Individual> populationSell = new ArrayList<>();
        int category = 0;
        String market = null; //"tradcomm";
        //List<Double> macdLists[] = new ArrayList[4];

        // TODO clone config

        Individual parent = getBest(conf, four, five, buyList, populationBuy, true, recommend);
        printmap(parent.conf.configValueMap, buyList);
        return parent;
    }

    private Individual getBest(MyMyConfig conf, int four, int five, List<String> keyList, List<Individual> population,
            boolean doBuy, Evaluation recommend) throws JsonParseException, JsonMappingException, IOException {
        for (int i = 0; i < five; i ++) {
            Individual buy = new Individual(conf, i, recommend).getNewWithValueCopyAndRandomFactory(conf, keyList);
            population.add(buy);
            printmap(buy.conf.configValueMap, keyList);
        }
        Collections.sort(population);

        printmap(population.get(0).conf.configValueMap, keyList);
        printmap(population.get(population.size() - 1).conf.configValueMap, keyList);

        for (int i = 0; i < conf.getEvolutionGenerations(); i++){
            Individual parent = population.get(0);
            population = new ArrayList<>();
            population.add(parent);
            for (int j = 0; j < four; j++) {
                Individual pop = new Individual(conf, i, recommend).getNewWithValueCopyFactory(parent.conf, keyList, false);
                pop.mutate(keyList);
                population.add(pop);
                if ( i == conf.getEvolutionGenerations() - 1) {
                    printmap(pop.conf.configValueMap, keyList);

                }

            }
            Collections.sort(population);

        }
        Collections.sort(population);
        printmap(population.get(0).conf.configValueMap, keyList);
        printmap(population.get(population.size() - 1).conf.configValueMap, keyList);
        return population.get(0);
    }

}
