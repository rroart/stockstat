package roart.aggregate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.calculate.CalcComplexNode;
import roart.calculate.CalcDoubleNode;
import roart.calculate.CalcNode;
import roart.category.Category;
import roart.category.CategoryConstants;
import roart.config.MyConfig;
import roart.config.MyMyConfig;
import roart.evaluation.IndicatorEvaluation;
import roart.evaluation.Recommend;
import roart.indicator.Indicator;
import roart.indicator.IndicatorUtils;
import roart.model.ResultItemTableRow;
import roart.model.StockItem;
import roart.pipeline.Pipeline;
import roart.pipeline.PipelineConstants;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.TaUtil;

public class AggregatorRecommenderIndicator extends Aggregator {

    //Map<String, Object[]> macdMap;
    Map<String, Double[]> listMap;
    Map<String, List<Recommend>> usedRecommenders;
    Map<String, Map<String, Double[]>> resultMap;
    
    public AggregatorRecommenderIndicator(MyMyConfig conf, String index, List<StockItem> stocks, Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap, Category[] categories, Pipeline[] datareaders) throws Exception {
        super(conf, index, 0);
       Category cat = IndicatorUtils.getWantedCategory(categories);
        Map<String, Indicator> usedIndicatorMap = cat.getIndicatorMap();
        Map<String, Map<String, Object>> localResultMap = cat.getIndicatorLocalResultMap();
        Map<String, Double[]> list0 = (Map<String, Double[]>) localResultMap.get(localResultMap.keySet().iterator().next()).get(PipelineConstants.LIST);
 
        usedRecommenders = Recommend.getUsedRecommenders(conf);
        Map<String, List<String>[]> recommendKeyMap = Recommend.getRecommenderKeyMap(usedRecommenders);
        Map<String, Indicator> indicatorMap = new HashMap<>();
        int category = cat.getPeriod();
        Set<String> ids = new HashSet<>();
        ids.addAll(list0.keySet());
        Map<String, Indicator> newIndicatorMap = new HashMap<>();

        for (String type : usedRecommenders.keySet()) {
            List<Recommend> list = usedRecommenders.get(type);
            for (Recommend recommend : list) {
                String indicator = recommend.indicator();
                if (indicator != null) {
                    Indicator newIndicator = recommend.getIndicator(marketdatamap, category, newIndicatorMap, usedIndicatorMap, datareaders);
                    if (newIndicator != null) {
                        indicatorMap.put(indicator, newIndicator);
                    }
                }
                // TODO fix
                Map<String, Object[]> aResult = (Map<String, Object[]>) cat.getIndicatorLocalResultMap().get(indicator).get(PipelineConstants.LIST);
                ids.retainAll(aResult.keySet());
            }
        }
        Map<String, Double[]> result = new HashMap<>();
        List<String> indDebug = new ArrayList<>();/*
        if (false) {
            List<Recommend> list = usedRecommenders.values().iterator().next(); //get(type);
            //List<Recommend> list2 = usedRecommenders.values().iterator().next(); //get(type);
            //System.out.println("l1 " + list.get);
            for (Recommend recommend : list) {
                String indicator = recommend.indicator();
                indDebug.add(indicator);
                buyKeys.addAll(recommend.getBuyList());
            }
            for (String key : buyKeys) {
                Object o = conf.configValueMap.get(key);
                CalcNode node = (CalcNode) conf.configValueMap.get(key);
                transformToNode(conf, buyKeys, true, macdrsiMinMax);
            }
        }
*/
        //List<Recommend> baseList = usedRecommenders.values().iterator().next(); //get(type);
        if (false) {
            for (String id : ids) {
            Double[] arrayResult = new Double[0];
            //for (String indicator : usedIndicatorMap.keySet()) {
                //List<Recommend> list2 = usedRecommenders.values().iterator().next(); //get(type);
                //System.out.println("l1 " + list.get);
            for (String type : usedRecommenders.keySet()) {
                List<Recommend> list = usedRecommenders.get(type);
                for (Recommend recommend : list) {
                    String indicator = recommend.indicator();
                    Map<String, Double[]> listMap = (Map<String, Double[]>) cat.getIndicatorLocalResultMap().get(indicator).get(PipelineConstants.RESULT);
                    //System.out.println(Arrays.toString(listMap.get(id)));
                    Double[] aResult = listMap.get(id);
                    arrayResult = (Double[]) ArrayUtils.addAll(arrayResult, aResult);
                }
            }
            result.put(id, arrayResult);
        }
        }
        TaUtil tu = new TaUtil();
        resultMap = new HashMap<>();
        for (String recommender : usedRecommenders.keySet()) {
        //for (String type : indicatorMap.keySet()) {
            List<Indicator> indicators = Recommend.getIndicators(recommender, usedRecommenders, indicatorMap);
            //indicators.add(indicatorMap.get(type));
            // We just want the config, any in the list will do
            //Recommend recommend = usedRecommenders.get(recommender).get(0);
            //System.out.println("li " + recommend.getBuyList());
            Object[] retObj = IndicatorUtils.getDayIndicatorMap(conf, tu, indicators, 0 /*recommend.getFutureDays()*/, 1 /*conf.getTableDays()*/, 1 /*recommend.getIntervalDays()*/);
            Map<Integer, Map<String, Double[]>> dayIndicatorMap = (Map<Integer, Map<String, Double[]>>) retObj[0];
            result = dayIndicatorMap.get(0);
            List<Double>[] macdrsiMinMax = (List<Double>[]) retObj[1];
            List<String> buyKeys = new ArrayList<>();
            List<String> sellKeys = new ArrayList<>();
            {
                //List<Recommend> list = usedRecommenders.values().iterator().next(); //get(type);
                //List<Recommend> list2 = usedRecommenders.values().iterator().next(); //get(type);
                //System.out.println("l1 " + list.get);
                List<Recommend> recommenders = usedRecommenders.get(recommender);
                for (Recommend recommend : recommenders) {
                    String indicator = recommend.indicator();
                    //indDebug.add(indicator);
                    buyKeys.addAll(recommend.getBuyList());
                    sellKeys.addAll(recommend.getSellList());
                }
                //for (String key : keys) {
                    //Object o = conf.configValueMap.get(key);
                    //CalcNode node = (CalcNode) conf.configValueMap.get(key);
                    transformToNode(conf, buyKeys, true, macdrsiMinMax);
                    transformToNode(conf, sellKeys, false, macdrsiMinMax);
                //}
            }
            //Map<String, Double[]> indicatorMap = dayIndicatorMap.get(j);
            // find recommendations
            int macdlen = conf.getTableDays();
            int listlen = conf.getTableDays();
            //transform(conf, buyList);
            Map<String, Double[]> indicatorResultMap;
            indicatorResultMap = new HashMap<>();
            for (String id : ids) {
                Double[] aResult = new Double[2]; 
                System.out.println("ttt " + result.get(id));
                //System.out.println(result.get(id).getClass().getName());
                Double[] mergedResult = (Double[]) result.get(id);
                if (mergedResult == null || mergedResult.length == 0) {
                    continue;
                }
                double buyRecommendValue = 0;
                double sellRecommendValue = 0;
                for (int i = 0; i < buyKeys.size(); i++) {
                    String key = buyKeys.get(i);
                    // TODO temp fix
                    Object o = conf.configValueMap.get(key);
                    if (o instanceof Integer) {
                        Integer oint = (Integer) o;
                        buyRecommendValue += mergedResult[i] * oint;
                        continue;
                    }
                    CalcNode node = (CalcNode) conf.configValueMap.get(key);
                    //node.setDoBuy(useMax);
                    if (mergedResult.length < buyKeys.size()) {
                        int jj = 0;
                    }
                    double value = mergedResult[i];
                    buyRecommendValue += node.calc(value, 0);
                }
                aResult[0] = buyRecommendValue;              
                for (int i = 0; i < sellKeys.size(); i++) {
                    String key = sellKeys.get(i);
                    // TODO temp fix
                    Object o = conf.configValueMap.get(key);
                    if (o instanceof Integer) {
                        Integer oint = (Integer) o;
                        sellRecommendValue += mergedResult[i] * oint;
                        continue;
                    }                   
                    CalcNode node = (CalcNode) conf.configValueMap.get(key);
                    //node.setDoBuy(useMax);
                    double value = mergedResult[i];
                    sellRecommendValue += node.calc(value, 0);
                }
                aResult[1] = sellRecommendValue;              
               indicatorResultMap.put(id, aResult);
            }
            resultMap.put(recommender, indicatorResultMap);
        }
    }

    // TODO duplicated...
    public void transformToNode(MyConfig conf, List<String> keys, boolean useMax, List<Double>[] macdrsiMinMax) throws JsonParseException, JsonMappingException, IOException {
        // TODO implement default somewhere else
        //List<Double>[] macdrsiMinMax = (List<Double>[]) retObj[1];
        ObjectMapper mapper = new ObjectMapper();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            Object value = conf.configValueMap.get(key);
            // this if is added to the original
            if (value instanceof CalcNode) {
                continue;
            }
            if (value instanceof Integer) {
                CalcNode anode = new CalcDoubleNode();
                conf.configValueMap.put(key, anode);
                return;
            }
            String jsonValue = (String) conf.configValueMap.get(key);
            if (jsonValue == null || jsonValue.isEmpty()) {
                jsonValue = (String) conf.deflt.get(key);
                //System.out.println(conf.deflt);
            }
            CalcNode anode = mapper.readValue(jsonValue, CalcNode.class);
            CalcNode node = CalcNodeFactory.get(anode.className, jsonValue, macdrsiMinMax, i, useMax);
            conf.configValueMap.put(key, node);
        }
    }

    // TODO duplicated
    private static class CalcNodeFactory {
        public static CalcNode get(String name, String jsonValue, List<Double>[] macdrsiMinMax, int index, boolean useMax) throws JsonParseException, JsonMappingException, IOException {
            // TODO check class name
            CalcComplexNode anode;
            if (name != null && name.equals("Double")) {
                CalcDoubleNode aanode = new CalcDoubleNode();
                return aanode;
            }
            if (jsonValue == null) {
                anode = new CalcComplexNode();
            } else {
                ObjectMapper mapper = new ObjectMapper();
                anode = (CalcComplexNode) mapper.readValue(jsonValue, CalcComplexNode.class);
            }
            List<Double> minmax = macdrsiMinMax[index];
            double minMutateThresholdRange =  minmax.get(0);
            double maxMutateThresholdRange = minmax.get(1);
            anode.setMinMutateThresholdRange(minMutateThresholdRange);
            anode.setMaxMutateThresholdRange(maxMutateThresholdRange);
            anode.setUseMax(useMax);
            return anode;
        }
    }

    @Override
    public boolean isEnabled() {
        return conf.wantMACDRSIRecommender();
    }

    @Override
    public Object[] getResultItem(StockItem stock) {
        Double[] arrayResult = new Double[0];
        for (String recommender : usedRecommenders.keySet()) {
            Map<String, Double[]> indicatorResultMap = resultMap.get(recommender);
            Double[] aResult = indicatorResultMap.get(stock.getId());
            int size = usedRecommenders.keySet().size();
            if (aResult == null || aResult.length < size) {
                aResult = new Double[size];
            }
            //if (aResult == null || aResult)
            arrayResult = (Double[]) ArrayUtils.addAll(arrayResult, aResult);
        }
        return arrayResult;
    }

    @Override
    public void addResultItemTitle(ResultItemTableRow headrow) {
        for (String recommender : usedRecommenders.keySet()) {
        headrow.add("Buy" + Constants.WEBBR  + recommender);
        headrow.add("Sell" + Constants.WEBBR  + recommender);
        }
    }

    @Override
    public void addResultItem(ResultItemTableRow row, StockItem stock) {
        Object[] arrayResult = getResultItem(stock);
        row.addarr(arrayResult);
    }
    
    @Override
    public String getName() {
        return PipelineConstants.AGGREGATORRECOMMENDERINDICATOR;
    }

    @Override
    public Map<String, Object> getLocalResultMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(PipelineConstants.RESULT, resultMap);
        return map;
    }
    
}
