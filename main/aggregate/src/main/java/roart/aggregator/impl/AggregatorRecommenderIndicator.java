package roart.aggregator.impl;

import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;

import roart.aggregatorindicator.impl.Recommend;
import roart.category.AbstractCategory;
import roart.common.pipeline.data.*;
import roart.iclij.config.IclijConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.util.PipelineUtils;
import roart.gene.CalcGene;
import roart.gene.impl.CalcGeneUtils;
import roart.common.constants.Constants;
import roart.common.inmemory.model.Inmemory;
import roart.common.model.StockDTO;
import roart.indicator.AbstractIndicator;
import roart.indicator.util.IndicatorUtils;
import roart.result.model.ResultItemTableRow;
import roart.stockutil.StockUtil;
import roart.model.data.MarketData;
import roart.talib.util.TaUtil;
import roart.pipeline.common.aggregate.Aggregator;

public class AggregatorRecommenderIndicator extends Aggregator {

    Map<String, Double[]> listMap;
    Map<String, List<Recommend>> usedRecommenders;
    Map<String, Map<String, Double[]>> resultMap;
    public List<String> disableList;
 
    public AggregatorRecommenderIndicator(IclijConfig conf, String index, Map<String, MarketData> marketdatamap, AbstractCategory[] categories,
            SerialPipeline datareaders, List<String> disableList, Inmemory inmemory) throws Exception {
        super(conf, index, 0, inmemory);
        this.disableList = disableList;
        if (!isEnabled()) {
            return;
        }
        AbstractCategory cat = StockUtil.getWantedCategory(categories, marketdatamap.get(conf.getConfigData().getMarket()).meta);
        if (cat == null) {
            return;
        }
        log.info("CCC" + cat.getPeriod() + " " + cat.getTitle());
        SerialPipeline datareader = PipelineUtils.getPipelines(datareaders, "" + cat.getTitle(), inmemory);
        //PipelineData datareader = PipelineUtils.getPipeline(datareaders, "" + cat.getPeriod(), inmemory);
        Map<String, AbstractIndicator> usedIndicatorMap = cat.getIndicatorMap();
        //Map<String, PipelineData> localResultMap = cat.putData();
        // TODO datareader null
        Map<String, Double[][]> list0 = PipelineUtils.sconvertMapDD(PipelineUtils.getPipelineValue(datareaders, "" + cat.getTitle(), PipelineConstants.LIST, inmemory));
 
        usedRecommenders = Recommend.getUsedRecommenders(conf);
        Map<String, List<String>[]> recommendKeyMap = Recommend.getRecommenderKeyMap(usedRecommenders, usedIndicatorMap, conf, Boolean.TRUE.equals(marketdatamap.get(conf.getConfigData().getMarket()).meta.isLhc()));
        Map<String, AbstractIndicator> indicatorMap = new HashMap<>();
        this.category = cat.getPeriod();
        this.title = cat.getTitle();
        Set<String> ids = new HashSet<>();
        ids.addAll(list0.keySet());
        Map<String, AbstractIndicator> newIndicatorMap = new HashMap<>();

        for (Entry<String, List<Recommend>> entry : usedRecommenders.entrySet()) {
            List<Recommend> list = entry.getValue();
            for (Recommend recommend : list) {
                String indicator = recommend.indicator();
                if (indicator != null) {
                    AbstractIndicator newIndicator = recommend.getIndicator(category, newIndicatorMap, usedIndicatorMap, datareaders, cat.getTitle(), inmemory);
                    if (newIndicator != null) {
                        indicatorMap.put(indicator, newIndicator);
                    }
                }
                // fix
                SerialPipeline indicatorResultMap = new SerialPipeline();
                indicatorResultMap.add(cat.putData().get(indicator));
                if (indicatorResultMap != null) {
                    //log.info("III" + indicator + " " + indicatorResultMap[0].getAllKeys());
                    //Map<String, Object[]> aResult = (Map<String, Object[]>) indicatorResultMap.get(PipelineConstants.LIST);
                    SerialMapD aResult = PipelineUtils.getResultMap(indicatorResultMap, indicator, inmemory);
                    if (aResult != null && aResult.getMap() != null && aResult.keySet() != null) {
                        log.info("AAA"+ids + " " + aResult.keySet());
                    ids.retainAll(aResult.keySet());
                    } else {
                        log.info("AAAnull"+indicator);
                    }
                } else {
                    int jj = 0;
                }
            }
        }
        Map<String, Double[]> result = new HashMap<>();
        TaUtil tu = new TaUtil();
        resultMap = new HashMap<>();
        for (Entry<String, List<Recommend>> entry : usedRecommenders.entrySet()) {
            String recommender = entry.getKey();
            List<String>[] buysell = recommendKeyMap.get(recommender);
            List<AbstractIndicator> indicators = Recommend.getIndicators(recommender, usedRecommenders, indicatorMap);
            // We just want the config, any in the list will do
            Object[] retObj = IndicatorUtils.getDayIndicatorMap(conf, indicators.stream().map(AbstractIndicator::indicatorName).toList(), 0 /*recommend.getFutureDays()*/, 1 /*conf.getTableDays()*/, 1 /*recommend.getIntervalDays()*/, null, datareaders, inmemory);
            Map<Integer, Map<String, Double[]>> dayIndicatorMap = (Map<Integer, Map<String, Double[]>>) retObj[0];
            result = dayIndicatorMap.get(0);
            List<Double>[] macdrsiMinMax = (List<Double>[]) retObj[1];
            List<String> buyKeys = buysell[0];
            List<String> sellKeys = buysell[1];
            CalcGeneUtils.transformToNode(conf, buyKeys, true, macdrsiMinMax, disableList);
            CalcGeneUtils.transformToNode(conf, sellKeys, false, macdrsiMinMax, disableList);
            // find recommendations
            Map<String, Double[]> indicatorResultMap;
            indicatorResultMap = new HashMap<>();
            for (String id : ids) {
                if (id.equals("KZKAK:IND")) {
                    int jj=0;
                }
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
                    // temp fix
                    Object o = conf.getConfigData().getConfigValueMap().get(key);
                    if (o instanceof Integer) {
                        Integer oint = (Integer) o;
                        buyRecommendValue += mergedResult[i] * oint;
                        continue;
                    }
                    Object tmp = conf.getConfigData().getConfigValueMap().get(key);
                    CalcGene node = (CalcGene) conf.getConfigData().getConfigValueMap().get(key);
                    //node.setDoBuy(useMax);
                    if (mergedResult.length < buyKeys.size()) {
                        int jj = 0;
                    }
                    double value = mergedResult[i];
                    double calc = node.calc(value, 0);
                    if (Double.isNaN(calc)) {
                        int jj = 0;
                    } else {
                        buyRecommendValue += calc;
                    }
                }
                aResult[0] = buyRecommendValue;              
                for (int i = 0; i < sellKeys.size(); i++) {
                    String key = sellKeys.get(i);
                    if (disableList.contains(key)) {
                        continue;
                    }
                    // temp fix
                    Object o = conf.getConfigData().getConfigValueMap().get(key);
                    if (o instanceof Integer) {
                        Integer oint = (Integer) o;
                        sellRecommendValue += mergedResult[i] * oint;
                        continue;
                    }                   
                    CalcGene node = (CalcGene) conf.getConfigData().getConfigValueMap().get(key);
                    //node.setDoBuy(useMax);
                    double value = mergedResult[i];
                    double calc = node.calc(value, 0);
                    if (Double.isNaN(calc)) {
                        int jj = 0;
                    } else {
                        sellRecommendValue += calc;
                    }
                }
                aResult[1] = sellRecommendValue;              
               indicatorResultMap.put(id, aResult);
            }
            resultMap.put(recommender, indicatorResultMap);
        }
    }


    @Override
    public boolean isEnabled() {
        return conf.wantIndicatorRecommender();
    }

    @Override
    public Object[] getResultItem(StockDTO stock) {
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
    public void addResultItem(ResultItemTableRow row, StockDTO stock) {
        Object[] arrayResult = getResultItem(stock);
        arrayResult = round(arrayResult, 3);
        row.addarr(arrayResult);
    }
    
    @Override
    public String getName() {
        return PipelineConstants.AGGREGATORRECOMMENDERINDICATOR;
    }

    @Override
    public SerialPipeline putData() {
        SerialPipeline list = new SerialPipeline();
        //map.setName(getName());
        list.add(new PipelineData(getName(), PipelineConstants.CATEGORY, null, new SerialInteger(category), false));
        list.add(new PipelineData(getName(), PipelineConstants.CATEGORYTITLE, null, new SerialString(title), false));
        // rec with own result
        list.add(new PipelineData(getName(), PipelineConstants.RESULT, null, new SerialMapPlain(resultMap), true));
        return list;
    }
    
}
