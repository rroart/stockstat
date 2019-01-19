package roart.aggregatorindicator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roart.aggregatorindicator.AggregatorIndicator;
import roart.common.config.MyMyConfig;
import roart.indicator.AbstractIndicator;

public abstract class Recommend extends AggregatorIndicator {
    public Recommend(MyMyConfig conf) {
        super(conf);
    }
    public abstract List<String> getBuyList();
    public abstract List<String> getSellList();
    public abstract String complexity();
    
    public static Map<String, List<Recommend>> getUsedRecommenders(MyMyConfig conf) {
        List<Recommend> all = new ArrayList<>();
        all.add(new MACDRecommendSimple(conf));
        all.add(new MACDRecommendComplex(conf));
        all.add(new RSIRecommendSimple(conf));
        all.add(new RSIRecommendComplex(conf));
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
    
    public static Map<String, List<String>[]> getRecommenderKeyMap(Map<String, List<Recommend>> usedRecommenders) {
        Map<String, List<String>[]> result = new HashMap<>();
        for (String complexity : usedRecommenders.keySet()) {
            List<String>[] recommenders = new ArrayList[2];
            List<Recommend> recommenderList = usedRecommenders.get(complexity);
            List<String> buyList = new ArrayList<>();
            List<String> sellList = new ArrayList<>();
            for (Recommend recommend : recommenderList) {
                List<String> aBuyList = recommend.getBuyList();
                List<String> aSellList = recommend.getSellList();
                buyList.addAll(aBuyList);
                sellList.addAll(aSellList);
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
