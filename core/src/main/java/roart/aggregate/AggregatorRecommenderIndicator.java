package roart.aggregate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.calculate.CalcComplexNode;
import roart.calculate.CalcDoubleNode;
import roart.calculate.CalcNode;
import roart.calculate.CalcNodeUtils;
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

    Map<String, Double[]> listMap;
    Map<String, List<Recommend>> usedRecommenders;
    Map<String, Map<String, Double[]>> resultMap;
    public List<String> disableList;
 
    public AggregatorRecommenderIndicator(MyMyConfig conf, String index, List<StockItem> stocks, Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap, Category[] categories, Pipeline[] datareaders, List<String> disableList) throws Exception {
        super(conf, index, 0);
        this.disableList = disableList;
       Category cat = IndicatorUtils.getWantedCategory(categories);
       if (cat == null) {
           return;
       }
        Map<String, Indicator> usedIndicatorMap = cat.getIndicatorMap();
        Map<String, Map<String, Object>> localResultMap = cat.getIndicatorLocalResultMap();
        Map<String, Double[]> list0 = (Map<String, Double[]>) localResultMap.get(localResultMap.keySet().iterator().next()).get(PipelineConstants.LIST);
 
        usedRecommenders = Recommend.getUsedRecommenders(conf);
        Map<String, List<String>[]> recommendKeyMap = Recommend.getRecommenderKeyMap(usedRecommenders);
        Map<String, Indicator> indicatorMap = new HashMap<>();
        this.category = cat.getPeriod();
        this.title = cat.getTitle();
        Set<String> ids = new HashSet<>();
        ids.addAll(list0.keySet());
        Map<String, Indicator> newIndicatorMap = new HashMap<>();

        for (Entry<String, List<Recommend>> entry : usedRecommenders.entrySet()) {
            List<Recommend> list = entry.getValue();
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
        TaUtil tu = new TaUtil();
        resultMap = new HashMap<>();
        for (Entry<String, List<Recommend>> entry : usedRecommenders.entrySet()) {
            String recommender = entry.getKey();
            List<Indicator> indicators = Recommend.getIndicators(recommender, usedRecommenders, indicatorMap);
            // We just want the config, any in the list will do
            Object[] retObj = IndicatorUtils.getDayIndicatorMap(conf, tu, indicators, 0 /*recommend.getFutureDays()*/, 1 /*conf.getTableDays()*/, 1 /*recommend.getIntervalDays()*/, null);
            Map<Integer, Map<String, Double[]>> dayIndicatorMap = (Map<Integer, Map<String, Double[]>>) retObj[0];
            result = dayIndicatorMap.get(0);
            List<Double>[] macdrsiMinMax = (List<Double>[]) retObj[1];
            List<String> buyKeys = new ArrayList<>();
            List<String> sellKeys = new ArrayList<>();
            {
                List<Recommend> recommenders = entry.getValue();
                for (Recommend recommend : recommenders) {
                    buyKeys.addAll(recommend.getBuyList());
                    sellKeys.addAll(recommend.getSellList());
                }
                CalcNodeUtils.transformToNode(conf, buyKeys, true, macdrsiMinMax, disableList);
                CalcNodeUtils.transformToNode(conf, sellKeys, false, macdrsiMinMax, disableList);
            }
            // find recommendations
            Map<String, Double[]> indicatorResultMap;
            indicatorResultMap = new HashMap<>();
            for (String id : ids) {
                Double[] aResult = new Double[2]; 
                System.out.println("ttt " + result.get(id));
                Double[] mergedResult = result.get(id);
                if (mergedResult == null || mergedResult.length == 0) {
                    continue;
                }
                double buyRecommendValue = 0;
                double sellRecommendValue = 0;
                for (int i = 0; i < buyKeys.size(); i++) {
                    String key = buyKeys.get(i);
                    if (disableList.contains(key)) {
                        continue;
                    }
                    // TODO temp fix
                    Object o = conf.configValueMap.get(key);
                    if (o instanceof Integer) {
                        Integer oint = (Integer) o;
                        buyRecommendValue += mergedResult[i] * oint;
                        continue;
                    }
                    Object tmp = conf.configValueMap.get(key);
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
                    if (disableList.contains(key)) {
                        continue;
                    }
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


    @Override
    public boolean isEnabled() {
        return conf.wantMACDRSIRecommender();
    }

    @Override
    public Object[] getResultItem(StockItem stock) {
        Double[] arrayResult = new Double[0];
        if (usedRecommenders == null) {
            return arrayResult;
        }
        for (String recommender : usedRecommenders.keySet()) {
            Map<String, Double[]> indicatorResultMap = resultMap.get(recommender);
            Double[] aResult = indicatorResultMap.get(stock.getId());
            int size = usedRecommenders.keySet().size();
            if (aResult == null || aResult.length < size) {
                aResult = new Double[size];
            }
            arrayResult = ArrayUtils.addAll(arrayResult, aResult);
        }
        return arrayResult;
    }

    @Override
    public void addResultItemTitle(ResultItemTableRow headrow) {
        if (usedRecommenders == null) {
            return;
        }
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
        map.put(PipelineConstants.CATEGORY, category);
        map.put(PipelineConstants.CATEGORYTITLE, title);
        map.put(PipelineConstants.RESULT, resultMap);
        return map;
    }
    
}
