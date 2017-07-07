package roart.recommender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.calculate.CalcMACDNode;
import roart.calculate.CalcNode;
import roart.config.ConfigConstants;
import roart.config.MyConfig;
import roart.model.StockItem;
import roart.util.MarketData;
import roart.util.StockDao;

public class MACDRSIRecommend extends BuySellRecommend {
    
    // TODO add deltadays?
    
    public  List<String> getBuyList() {
        List<String> buyList = new ArrayList<>();
        buyList.add(ConfigConstants.AGGREGATORSMACDRSIRECOMMENDWEIGHTSBUYHISTOGRAMNODE);
        buyList.add(ConfigConstants.AGGREGATORSMACDRSIRECOMMENDWEIGHTSBUYHISTOGRAMDELTANODE);
        buyList.add(ConfigConstants.AGGREGATORSMACDRSIRECOMMENDWEIGHTSBUYMOMENTUMNODE);
        buyList.add(ConfigConstants.AGGREGATORSMACDRSIRECOMMENDWEIGHTSBUYMOMENTUMDELTANODE);
        buyList.add(ConfigConstants.AGGREGATORSMACDRSIRECOMMENDWEIGHTSBUYRSINODE);
        buyList.add(ConfigConstants.AGGREGATORSMACDRSIRECOMMENDWEIGHTSBUYRSIDELTANODE);
        return buyList;
    }

    public  List<String> getSellList() {
        List<String> sellList = new ArrayList<>();
        sellList.add(ConfigConstants.AGGREGATORSMACDRSIRECOMMENDWEIGHTSSELLHISTOGRAMNODE);
        sellList.add(ConfigConstants.AGGREGATORSMACDRSIRECOMMENDWEIGHTSSELLHISTOGRAMDELTANODE);
        sellList.add(ConfigConstants.AGGREGATORSMACDRSIRECOMMENDWEIGHTSSELLMOMENTUMNODE);
        sellList.add(ConfigConstants.AGGREGATORSMACDRSIRECOMMENDWEIGHTSSELLMOMENTUMDELTANODE);
        sellList.add(ConfigConstants.AGGREGATORSMACDRSIRECOMMENDWEIGHTSSELLRSINODE);
        sellList.add(ConfigConstants.AGGREGATORSMACDRSIRECOMMENDWEIGHTSSELLRSIDELTANODE);
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

    @Override
    public void getBuySellRecommendations(Map<String, Double> buyMap, Map<String, Double> sellMap, MyConfig conf, List<Double> macdLists[],
            Map<String, Double[]> listMap, Map<String, Double[]> momMap, List<String> buyList, List<String> sellList) throws JsonParseException, JsonMappingException, IOException {
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
        ObjectMapper mapper = new ObjectMapper();
        double recommend = 0;
        int ii = 0;
        for (String key : buyList) {
            String jsonValue = (String) conf.configValueMap.get(key);
            CalcNode anode = mapper.readValue(jsonValue, CalcNode.class);
            CalcNode node = CalcNodeFactory.get(anode.className, jsonValue);
            int value = 0;
            double minmax = 0;
            if (buyMap != null) {
                minmax = macdMax[ii];
            } else {
                minmax = macdMin[ii];
            }
            recommend += node.calc(value, minmax);
            ii++;
        }
        
        for (String id : listMap.keySet()) {
            Double[] momentum = momMap.get(id);
            if (momentum == null || momentum[0] == null || momentum[1] == null || momentum[2] == null || momentum[3] == null) {
                continue;
            }
            // this is the histogram 0
            if (momentum[0] >= 0 && buyMap != null) {
                recommend = ((Integer) conf.configValueMap.get(buyList.get(0)))*(macdMax[0] - momentum[0])/macdMax[0];
                for (int i = 1; i < len; i ++) {
                    recommend += ((Integer) conf.configValueMap.get(buyList.get(i)))*(momentum[i])/macdMax[i];
                }
                buyMap.put(id, recommend);
            }
            if (momentum[0] < 0 && sellMap != null) {
                recommend = ((Integer) conf.configValueMap.get(sellList.get(0)))*(macdMin[0] - momentum[0])/macdMin[0];
                for (int i = 1; i < len; i ++) {
                    recommend += ((Integer) conf.configValueMap.get(sellList.get(i)))*(momentum[i])/macdMin[i];
                }
                sellMap.put(id, recommend);
            }
        }
    }
    
    public static void getBuySellRecommendations2(Map<String, Double> buyMap, Map<String, Double> sellMap, MyConfig conf, List<Double> macdLists[] /*,List<Double> macdList, List<Double> histList, List<Double> macdDList,
            List<Double> histDList*/, Map<String, Double[]> listMap, Map<String, Double[]> momMap, List<String> buyList, List<String> sellList) {
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

    private static class CalcNodeFactory {
        public static CalcNode get(String name, String jsonValue) throws JsonParseException, JsonMappingException, IOException {
            ObjectMapper mapper = new ObjectMapper();
            CalcNode anode = (CalcNode) mapper.readValue(jsonValue, CalcMACDNode.class);
            return anode;
        }
    }

    @Override
    public void mutate(Map<String, Object> configValueMap, List<String> keys) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void getRandom(Map<String, Object> configValueMap, List<String> keys) {
        // TODO Auto-generated method stub
        
    }
}
