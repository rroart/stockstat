package roart.evolution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.config.MyMyConfig;
import roart.recommender.BuySellRecommend;
import roart.recommender.MACDRecommend;
import roart.service.ControlService;
import roart.util.MarketData;
import roart.util.TaUtil;

public class Ordinary extends Common {

    @Override
    public List<Populus> doit(MyMyConfig conf,Map<String, MarketData> marketdatamap,Map<String, Double[]> listMap,Map<String, Object[]> objectMap, BuySellRecommend recommend) throws Exception {
        //MACDRecommend recommend = new MACDRecommend();
        int selectionSize = conf.getTestRecommendSelect();
        int four = 4;
        int five = 5;
        List<String> buyList = recommend.getBuyList();
        List<String> sellList = recommend.getSellList();
        List<Populus> populationBuy = new ArrayList<>();
        List<Populus> populationSell = new ArrayList<>();
        int category = 0;
        String market = null; //"tradcomm";
        //List<Double> macdLists[] = new ArrayList[4];
        TaUtil tu = new TaUtil();

        Object[] retObj = ControlService.getDayMomMap(conf, marketdatamap, objectMap, listMap, category, market, tu);
        Map<Integer, Map<String, Double[]>> dayMomMap = (Map<Integer, Map<String, Double[]>>) retObj[0];
        Map<Integer, List<Double>[]> dayMacdListsMap = (Map<Integer, List<Double>[]>) retObj[1];

        // TODO clone config

        FitnessBuySellMACD scoring = new FitnessBuySellMACD(conf, dayMomMap, dayMacdListsMap, listMap, recommend);
        
        Populus buyParent = getBest(conf, selectionSize, buyList, populationBuy, scoring, true, recommend);
        Populus sellParent = getBest(conf, selectionSize, sellList, populationSell, scoring, false, recommend);
        //Populi sellParent = populationSell.get(0);
        //System.out.println("bP" + buyParent.conf.configValueMap);
        //System.out.println("sP" + sellParent.conf.configValueMap);
        List<Populus> retList = new ArrayList<>();
        retList.add(buyParent);
        retList.add(sellParent);
        return retList;
    }

    private Populus getBest(MyMyConfig conf, int selectionSize, List<String> keyList, List<Populus> population,
            FitnessBuySellMACD scoring, boolean doBuy, BuySellRecommend recommend) throws JsonParseException, JsonMappingException, IOException {
        int macdlen = conf.getTableDays();
        int listlen = conf.getTableDays();
        population.add(new Populus(conf, 0, scoring, recommend).getNewWithValueCopyFactory(conf, keyList, true, doBuy));
        for (int i = 1; i < selectionSize; i ++) {
            Populus pop = new Populus(conf, i, scoring, recommend).getNewWithValueCopyAndRandomFactory(conf, keyList, doBuy);
            population.add(pop);
        }
        Collections.sort(population);
        
        System.out.println("pB " + population);
        printmap(population.get(0).conf.configValueMap, keyList);
        printmap(population.get(population.size() - 1).conf.configValueMap, keyList);
        
        for (int i = 0; i < conf.getTestRecommendGenerations(); i++){
            population = population.subList(0, Math.min(population.size(), conf.getTestRecommendSelect()));
           
            List<Populus> children = crossover(conf.getTestRecommendChildren(), population, keyList, conf, scoring, false, true, recommend);
            
            mutateList(population, conf.getTestRecommendElite(), population.size(), conf.getTestRecommendMutate(), false, keyList, true);
            mutateList(children, 0, population.size(), conf.getTestRecommendMutate(), true, keyList, false);
            
            population.addAll(children);

             Collections.sort(population);
            // System.out.println("pMid " + population);
           
        }
        System.out.println("pBend " + population);
        printmap(population.get(0).conf.configValueMap, keyList);
        printmap(population.get(population.size() - 1).conf.configValueMap, keyList);
        Populus parent = population.get(0);
        return parent;
    }
    
 }
