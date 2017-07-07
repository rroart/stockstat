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

public class OneFour extends Common {

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
        
        Populus buyParent = getBest(conf, four, five, buyList, populationBuy, scoring, true, recommend);
        Populus sellParent = getBest(conf, four, five, sellList, populationSell, scoring, false, recommend);
        printmap(buyParent.conf.configValueMap, buyList);
        //Populi sellParent = populationSell.get(0);
        //System.out.println("bP" + buyParent.conf.configValueMap);
        //System.out.println("sP" + sellParent.conf.configValueMap);
        List<Populus> retList = new ArrayList<>();
        retList.add(buyParent);
        retList.add(sellParent);
        return retList;
    }

    private Populus getBest(MyMyConfig conf, int four, int five, List<String> keyList, List<Populus> population,
            FitnessBuySellMACD scoring, boolean doBuy, BuySellRecommend recommend) throws JsonParseException, JsonMappingException, IOException {
        int macdlen = conf.getTableDays();
        int listlen = conf.getTableDays();
        for (int i = 0; i < five; i ++) {
            Populus buy = new Populus(conf, i, scoring, recommend).getNewWithValueCopyAndRandomFactory(conf, keyList, doBuy);
            population.add(buy);
            printmap(buy.conf.configValueMap, keyList);
        }
        System.out.println("pB " + population);
        Collections.sort(population);
        
        System.out.println("pB " + population);
        printmap(population.get(0).conf.configValueMap, keyList);
        printmap(population.get(population.size() - 1).conf.configValueMap, keyList);

        for (int i = 0; i < conf.getTestRecommendGenerations(); i++){
            Populus buyParent = population.get(0);
             population = new ArrayList<>();
            population.add(buyParent);
            for (int j = 0; j < four; j++) {
                Populus pop = new Populus(conf, i, scoring, recommend).getNewWithValueCopyFactory(buyParent.conf, keyList, false, doBuy);
                pop.mutate(keyList, doBuy);
                population.add(pop);
                if ( i == conf.getTestRecommendGenerations() - 1) {
                    printmap(pop.conf.configValueMap, keyList);

                }

            }
            Collections.sort(population);
            //System.out.println("pMid " + population);

        }
        System.out.println("pBend " + population);
        Collections.sort(population);
        System.out.println("pBend " + population);
        printmap(population.get(0).conf.configValueMap, keyList);
        printmap(population.get(population.size() - 1).conf.configValueMap, keyList);
        Populus parent = population.get(0);
        return parent;
    }
    
}
