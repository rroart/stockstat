package roart.evaluation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.config.ConfigConstants;
import roart.config.MyConfig;
import roart.config.MyMyConfig;
import roart.util.MarketData;

public class RSIRecommend extends Evaluation{
    
    // TODO add deltadays?
    
    public  List<String> getBuyList() {
        List<String> buyList = new ArrayList<>();
        buyList.add(ConfigConstants.INDICATORSRSIRECOMMENDWEIGHTSBUY);
        buyList.add(ConfigConstants.INDICATORSRSIRECOMMENDWEIGHTSBUYDELTA);
        //buyList.add(ConfigConstants.INDICATORSRSIRECOMMENDWEIGHTSBUY);
        //buyList.add(ConfigConstants.INDICATORSRSIRECOMMENDWEIGHTSBUYDELTA);
        return buyList;
    }

    public  List<String> getSellList() {
        List<String> sellList = new ArrayList<>();
        sellList.add(ConfigConstants.INDICATORSRSIRECOMMENDWEIGHTSSELL);
        sellList.add(ConfigConstants.INDICATORSRSIRECOMMENDWEIGHTSSELLDELTA);
        //sellList.add(ConfigConstants.INDICATORSRSIRECOMMENDWEIGHTSSELL);
        //sellList.add(ConfigConstants.INDICATORSRSIRECOMMENDWEIGHTSSELLDELTA);
        return sellList;
    }

    public static void addToLists(Map<String, MarketData> marketdatamap, int category, List<Double> macdLists[] /*List<Double> macdList,
            List<Double> histList, List<Double> macdDList, List<Double> histDList*/, String market, Double[] momentum) throws Exception {
        {
            for (int i = 0; i < macdLists.length; i ++) {
                List<Double> macdList = macdLists[i];
                if (momentum[i] != null) {
                    macdList.add(momentum[i]);
                }
            }
        }
    }

    public void getBuySellRecommendations(Map<String, Double> buyMap, Map<String, Double> sellMap, MyConfig conf, List<Double> rsiLists[],
            Map<String, Double[]> listMap, Map<String, Double[]> momMap, Map<String, Double[]> rsiMap, List<String> buyList, List<String> sellList) {
        int len = rsiLists.length;
        Double macdMax[] = new Double[len];
        Double macdMin[] = new Double[len];
        for (int i = 0; i < len; i ++) {
            List<Double> macdList = rsiLists[i];
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
            Double[] rsi = (Double[]) rsiMap.get(id);
            if (rsi == null || rsi[0] == null || rsi[1] == null) {
                continue;
            }
            // this is the histogram 0
            if (rsi[0] >= 0 && buyMap != null) {
                double recommend = ((Integer) conf.configValueMap.get(buyList.get(0)))*(macdMax[0] - rsi[0])/macdMax[0];
                for (int i = 1; i < len; i ++) {
                    recommend += ((Integer) conf.configValueMap.get(buyList.get(i)))*(rsi[i])/macdMax[i];
                }
                buyMap.put(id, recommend);
            }
            if (rsi[0] < 0 && sellMap != null) {
                double recommend = ((Integer) conf.configValueMap.get(sellList.get(0)))*(macdMin[0] - rsi[0])/macdMin[0];
                for (int i = 1; i < len; i ++) {
                    recommend += ((Integer) conf.configValueMap.get(sellList.get(i)))*(rsi[i])/macdMin[i];
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
                if (buy) {
                    score += (newVal / curVal - 1) / recommend;
                } else {
                    score += (1 - newVal / curVal) / recommend;
                }
            }
        }
        //System.out.println("score"+score);
        return score;
    }

    @Override
    public void mutate(Map<String, Object> configValueMap, List<String> keys) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void getRandom(Map<String, Object> configValueMap, List<String> keys) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void transformToNode(MyConfig newConf, List<String> keys)
            throws JsonParseException, JsonMappingException, IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void normalize(Map<String, Object> configValueMap, List<String> keys) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void transformFromNode(MyConfig conf, List<String> keys)
            throws JsonParseException, JsonMappingException, IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public double getEvaluations(MyMyConfig conf, int j)
            throws JsonParseException, JsonMappingException, IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getFitness(MyMyConfig testBuySellConfig, List<String> buySellList)
            throws JsonParseException, JsonMappingException, IOException {
        // TODO Auto-generated method stub
        return 0;
    }


}

