package roart.evolution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.config.MyMyConfig;
import roart.evaluation.Evaluation;
import roart.evaluation.MACDRecommend;
import roart.service.ControlService;
import roart.util.TaUtil;

public class OrdinaryEvolution extends EvolutionAlgorithm {

    @Override
    public Individual getFittest(MyMyConfig conf,Evaluation evaluation) throws Exception {
        //MACDRecommend recommend = new MACDRecommend();
        int selectionSize = conf.getEvolutionSelect();
        List<String> keys = evaluation.getKeys();
        Population population = new Population(selectionSize, conf, evaluation, keys, false);
        int category = 0;
        String market = null; //"tradcomm";
        //List<Double> macdLists[] = new ArrayList[4];
        TaUtil tu = new TaUtil();

        /*
        Object[] retMacdObj = ControlService.getDayMomMap(conf, objectMACDMap, listMap, tu);
        Map<Integer, Map<String, Double[]>> dayMomMap = (Map<Integer, Map<String, Double[]>>) retMacdObj[0];
        List<Double>[] dayMacdListsMap = (List<Double>[]) retMacdObj[1];
        Object[] retRsiObj = ControlService.getDayRsiMap(conf, objectRSIMap, listMap, tu);
        Map<Integer, Map<String, Double[]>> dayRsiMap = (Map<Integer, Map<String, Double[]>>) retMacdObj[0];
        List<Double>[] dayRsiListsMap = (List<Double>[]) retMacdObj[1];
*/
        
        // TODO clone config

        
        Individual parent = getBest(conf, selectionSize, keys, population, true, evaluation);
        //Populi sellParent = populationSell.get(0);
        //System.out.println("bP" + buyParent.conf.configValueMap);
        //System.out.println("sP" + sellParent.conf.configValueMap);
        evaluation.transformFromNode(parent.conf, keys);
        return parent;
    }

    private Individual getBest(MyMyConfig conf, int selectionSize, List<String> keyList, Population population,
            boolean useMax, Evaluation evaluation) throws JsonParseException, JsonMappingException, IOException {
        int macdlen = conf.getTableDays();
        int listlen = conf.getTableDays();
/*
        population.add(new Individual(conf, 0, evaluation).getNewWithValueCopyFactory(conf, keyList, true));
        for (int i = 1; i < selectionSize; i ++) {
            Individual pop = new Individual(conf, i, evaluation).getNewWithValueCopyAndRandomFactory(conf, keyList);
            population.add(pop);
        }
        */
        //Collections.sort(population);
        
        System.out.println("pB " + population);
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
            
             //Collections.sort(population);
            // System.out.println("pMid " + population);
           
        }
        System.out.println("pBend " + population);
        printmap(population.getIndividuals().get(0).conf.configValueMap, keyList);
        printmap(population.getIndividuals().get(population.size() - 1).conf.configValueMap, keyList);
        Individual parent = population.getFittest();
        return parent;
    }

 }
