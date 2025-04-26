package roart.aggregatorindicator.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.ImmutablePair;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.common.config.ConfigConstants;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfig;
import roart.common.constants.RecommendConstants;
import roart.common.model.StockDTO;
import roart.stockutil.StockDao;

public class MACDRecommendSimple extends RecommendMACD {
    
    public MACDRecommendSimple(IclijConfig conf) {
        super(conf);
    }

    @Override
    public  List<Pair<String, String>> getBuyList() {
        List<Pair<String, String>> buyList = new ArrayList<>();
        buyList.add(new ImmutablePair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLEMACDBUYWEIGHTHISTOGRAM, ConfigConstants.INDICATORSMACD));
        buyList.add(new ImmutablePair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLEMACDBUYWEIGHTHISTOGRAMDELTA, ConfigConstants.INDICATORSMACDMACDHISTOGRAMDELTA));
        buyList.add(new ImmutablePair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLEMACDBUYWEIGHTMACD, ConfigConstants.INDICATORSMACD));
        buyList.add(new ImmutablePair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLEMACDBUYWEIGHTMACDDELTA, ConfigConstants.INDICATORSMACDMACDMACDDELTA));
        buyList.add(new ImmutablePair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLEMACDBUYWEIGHTSIGNAL, ConfigConstants.INDICATORSMACD));
        buyList.add(new ImmutablePair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLEMACDBUYWEIGHTSIGNALDELTA, ConfigConstants.INDICATORSMACDMACDSIGNALDELTA));
        return buyList;
    }

    @Override
    public  List<Pair<String, String>> getSellList() {
        List<Pair<String, String>> buyList = new ArrayList<>();
        buyList.add(new ImmutablePair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLEMACDSELLWEIGHTHISTOGRAM, ConfigConstants.INDICATORSMACD));
        buyList.add(new ImmutablePair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLEMACDSELLWEIGHTHISTOGRAMDELTA, ConfigConstants.INDICATORSMACDMACDHISTOGRAMDELTA));
        buyList.add(new ImmutablePair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLEMACDSELLWEIGHTMACD, ConfigConstants.INDICATORSMACD));
        buyList.add(new ImmutablePair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLEMACDSELLWEIGHTMACDDELTA, ConfigConstants.INDICATORSMACDMACDMACDDELTA));
        buyList.add(new ImmutablePair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLEMACDSELLWEIGHTSIGNAL, ConfigConstants.INDICATORSMACD));
        buyList.add(new ImmutablePair(ConfigConstants.AGGREGATORSINDICATORRECOMMENDERSIMPLEMACDSELLWEIGHTSIGNALDELTA, ConfigConstants.INDICATORSMACDMACDSIGNALDELTA));
        return buyList;
    }

    @Override
    public boolean isEnabled() {
        return conf.wantRecommenderSimpleMACD();
    }

    /*
    //@Override
    public void getBuySellRecommendations(Map<String, Double> buyMap, Map<String, Double> sellMap, IclijConfig conf, List<Double> macdLists[],
            Map<String, Double[]> listMap, Map<String, Double[]> momMap, Map<String, Double[]> rsiMap, List<String> buyList, List<String> sellList) {
        int len = macdLists.length;
        Double macdMax[] = new Double[len];
        Double macdMin[] = new Double[len];
        for (int i = 0; i < len; i ++) {
            List<Double> macdList = macdLists[i];
            if (!macdList.isEmpty()) {
                macdMax[i] = Collections.max(macdList);
                macdMin[i] = Collections.min(macdList);
            } else {
                macdMax[i] = 0.0;
                macdMin[i] = 0.0;
            }
        }
        // find recommendations
        for (String id : listMap.keySet()) {
            Double[] momentum = momMap.get(id);
            if (momentum == null || momentum[0] == null || momentum[1] == null || momentum[2] == null || momentum[3] == null) {
                continue;
            }
            // this is the histogram 0
            if (momentum[0] >= 0 && buyMap != null) {
                double recommend = ((Integer) conf.configValueMap.get(buyList.get(0)))*(macdMax[0] - momentum[0])/macdMax[0];
                for (int i = 1; i < len; i ++) {
                    recommend += ((Integer) conf.configValueMap.get(buyList.get(i)))*(momentum[i])/macdMax[i];
                }
                buyMap.put(id, recommend);
            }
            if (momentum[0] < 0 && sellMap != null) {
                double recommend = ((Integer) conf.configValueMap.get(sellList.get(0)))*(macdMin[0] - momentum[0])/macdMin[0];
                for (int i = 1; i < len; i ++) {
                    recommend += ((Integer) conf.configValueMap.get(sellList.get(i)))*(momentum[i])/macdMin[i];
                }
                sellMap.put(id, recommend);
            }
        }
    }
    
    public static void getBuySellRecommendations2(Map<String, Double> buyMap, Map<String, Double> sellMap, IclijConfig conf, List<Double> macdLists[] /*,List<Double> macdList, List<Double> histList, List<Double> macdDList,
            List<Double> histDList, Map<String, Double[]> listMap, Map<String, Double[]> momMap, List<String> buyList, List<String> sellList) {
        int len = macdLists.length;
        Double macdMax[] = new Double[len];
        Double macdMin[] = new Double[len];
        for (int i = 0; i < len; i ++) {
            List<Double> macdList = macdLists[i];
            if (!macdList.isEmpty()) {
                macdMax[i] = Collections.max(macdList);
                macdMin[i] = Collections.min(macdList);
            } else {
                macdMax[i] = 0.0;
                macdMin[i] = 0.0;
            }
        }
        // find recommendations
        for (String id : listMap.keySet()) {
            Double[] momentum = momMap.get(id);
            if (momentum == null || momentum[0] == null || momentum[1] == null || momentum[2] == null || momentum[3] == null) {
                continue;
            }
            // this is the histogram 0
            if (momentum[0] >= 0 && buyMap != null) {
                double recommend = ((Integer) conf.configValueMap.get(buyList.get(0)))*(macdMax[0] - momentum[0])/macdMax[0];
                for (int i = 1; i < len; i ++) {
                    recommend += ((Integer) conf.configValueMap.get(buyList.get(i)))*(momentum[i])/macdMax[i];
                }
                buyMap.put(id, recommend);
            }
            if (momentum[0] < 0 && sellMap != null) {
                double recommend = ((Integer) conf.configValueMap.get(sellList.get(0)))*(macdMin[0] - momentum[0])/macdMin[0];
                for (int i = 1; i < len; i ++) {
                    recommend += ((Integer) conf.configValueMap.get(sellList.get(i)))*(momentum[i])/macdMin[i];
                }
                sellMap.put(id, recommend);
            }
        }
    }
    
    public static double getQuality(boolean buy, Map<String, Double> buySellMap, Map<String, Double[]> listMap, int curlistidx,
            int newlistidx) {
        double score = 0;
        //System.out.println("buySellMap" + buySellMap.size());
        for (String id : buySellMap.keySet()) {
            Double recommend = buySellMap.get(id);
            Double[] list = listMap.get(id);
            Double newVal = list[newlistidx];
            Double curVal = list[curlistidx];
            //System.out.println("vals " + newVal + "  " + curVal);
            if (newVal != null && curVal != null) {
                double addScore;
                if (buy) {
                    addScore = (newVal / curVal) * recommend / 100;
                    score += addScore;
                } else {
                    addScore = (curVal / newVal) * recommend / 100;
                    score += addScore;                   
                }
                if (false && addScore > 100) {
                    System.out.println("too high " + addScore + " " + newVal + " " + curVal + " " + recommend);
                }
            }
        }
        //System.out.println("score"+score);
        return score;
    }
*/

    @Override
    public String complexity() {
        return RecommendConstants.SIMPLE;
    }

    
    @Override
    public int getFutureDays() {
        return conf.getTestIndicatorRecommenderSimpleFutureDays();
    }
    
    @Override
    public int getIntervalDays() {
        return conf.getTestIndicatorRecommenderSimpleIntervalDays();
    }
}
