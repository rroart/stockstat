package roart.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roart.config.MyMyConfig;
import roart.indicator.Indicator;
import roart.util.MarketData;
import roart.util.PeriodData;

public abstract class Recommend {
    MyMyConfig conf;
    public Recommend(MyMyConfig conf) {
        this.conf = conf;
    }
    public abstract List<String> getBuyList();
    public abstract List<String> getSellList();
    public abstract boolean isEnabled();
    public abstract String complexity();
    public abstract String indicator();
    
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
    
    public abstract Indicator getIndicator(Map<String, MarketData> marketdatamap, int category) throws Exception;

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
    
    public static List<Indicator> getIndicators(String type, Map<String, List<Recommend>> usedRecommenders, Map<String, Indicator> indicatorMap) {
        List<Indicator> result = new ArrayList<>();
        List<Recommend> recommenders = usedRecommenders.get(type);
        for (Recommend recommend : recommenders) {
            String name = recommend.indicator();
            Indicator indicator = indicatorMap.get(name);
            result.add(indicator);
        }
        return result;
    }
}
