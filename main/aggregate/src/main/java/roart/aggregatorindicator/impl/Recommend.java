package roart.aggregatorindicator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.aggregatorindicator.AggregatorIndicator;
import roart.iclij.config.IclijConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialTA;
import roart.common.pipeline.util.PipelineUtils;
import roart.indicator.AbstractIndicator;

public abstract class Recommend extends AggregatorIndicator {
    protected static Logger log = LoggerFactory.getLogger(AggregatorIndicator.class);

    public Recommend(IclijConfig conf) {
        super(conf);
    }
    public abstract List<Pair<String, String>> getBuyList();
    public abstract List<Pair<String, String>> getSellList();
    public abstract String complexity();
    
    public static Map<String, List<Recommend>> getUsedRecommenders(IclijConfig conf) {
        List<Recommend> all = new ArrayList<>();
        all.add(new MACDRecommendSimple(conf));
        all.add(new MACDRecommendComplex(conf));
        all.add(new RSIRecommendSimple(conf));
        all.add(new RSIRecommendComplex(conf));
        all.add(new ATRRecommendComplex(conf));
        all.add(new CCIRecommendComplex(conf));
        all.add(new STOCHRecommendComplex(conf));
        all.add(new STOCHRSIRecommendComplex(conf));
        Map<String, List<Recommend>> result = new HashMap<>();
        for (Recommend recommend : all) {
            if (recommend.isEnabled()) {
                List<Recommend> recommendList = result.get(recommend.complexity());
                if (recommendList == null) {
                    recommendList = new ArrayList<>();
                }
                recommendList.add(recommend);
                result.put(recommend.complexity(), recommendList);
            }
        }
        return result;
    }
    
    public static Map<String, List<String>[]> getRecommenderKeyMap(Map<String, List<Recommend>> usedRecommenders, Map<String, AbstractIndicator> usedIndicatorMap, IclijConfig conf) {
        Map<String, List<String>[]> result = new HashMap<>();
        for (String complexity : usedRecommenders.keySet()) {
            List<String>[] recommenders = new ArrayList[2];
            List<Recommend> recommenderList = usedRecommenders.get(complexity);
            List<String> buyList = new ArrayList<>();
            List<String> sellList = new ArrayList<>();
            for (Recommend recommend : recommenderList) {
                AbstractIndicator indicator = usedIndicatorMap.get(recommend.indicator());
                if (indicator == null) {
                    int jj = 0;
                    log.error("Indicator null for {} {}", recommend.indicator(), complexity);
                    continue;
                }
                PipelineData resultMap = indicator.putData();
                Map<String, SerialTA> objMap = PipelineUtils.getObjectMap(resultMap);
                if (objMap != null) { 
                    List<Pair<String, String>> aBuyList = recommend.getBuyList();
                    for (Pair<String, String> pair : aBuyList) {
                        String key = pair.getLeft();
                        String cnf = pair.getRight();
                        if ((boolean) conf.getValueOrDefault(cnf)) {
                            buyList.add(key);
                        }
                    }
                    List<Pair<String, String>> aSellList = recommend.getSellList();
                    for (Pair<String, String> pair : aSellList) {
                        String key = pair.getLeft();
                        String cnf = pair.getRight();
                        if ((boolean) conf.getValueOrDefault(cnf)) {
                            sellList.add(key);
                        }
                    }
                }
                /*
                List<String> aBuyList = recommend.getBuyList();
                List<String> aSellList = recommend.getSellList();
                buyList.addAll(aBuyList);
                sellList.addAll(aSellList);
                */
            }
            recommenders[0] = buyList;
            recommenders[1] = sellList;
            result.put(complexity, recommenders);
        }
        return result;
    }
    
    public static List<AbstractIndicator> getIndicators(String type, Map<String, List<Recommend>> usedRecommenders, Map<String, AbstractIndicator> indicatorMap) {
        List<AbstractIndicator> result = new ArrayList<>();
        List<Recommend> recommenders = usedRecommenders.get(type);
        for (Recommend recommend : recommenders) {
            String name = recommend.indicator();
            AbstractIndicator indicator = indicatorMap.get(name);
            if (indicator != null) {
                result.add(indicator);
            }
        }
        return result;
    }
    
    public abstract int getFutureDays();
    
    public abstract int getIntervalDays();
    
}
