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
import roart.config.MyMyConfig;
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
                //System.out.println("macdlist " + i + " " + macdList);
                macdMax[i] = Collections.max(macdList);
                macdMin[i] = Collections.min(macdList);
            } else {
                macdMax[i] = 0.0;
                macdMin[i] = 0.0;
            }
        }
        // find recommendations
        double recommend = 0;
        //transform(conf, buyList);
        int ii = 0;
        boolean doBuy = true;
        List<String> keys = buyList;
        if (keys == null) {
            keys = sellList;
            doBuy = false;
        }
        for (String key : keys) {
            // TODO temp fix
            if (ii > 3) {
                break;
            }
            CalcMACDNode node = (CalcMACDNode) conf.configValueMap.get(key);
            node.setDoBuy(doBuy);

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
            // TODO check class name
            if (jsonValue == null) {
                return new CalcMACDNode();
            }
            ObjectMapper mapper = new ObjectMapper();
            CalcNode anode = (CalcNode) mapper.readValue(jsonValue, CalcMACDNode.class);
            return anode;
        }
    }

    @Override
    public void mutate(Map<String, Object> configValueMap, List<String> keys) {
        for (String key : keys) {
            CalcNode node = (CalcNode) configValueMap.get(key);
            node.mutate();
        }
    }

    @Override
    public void getRandom(Map<String, Object> configValueMap, List<String> keys) throws JsonParseException, JsonMappingException, IOException {
        for (String key : keys) {
            CalcNode node = CalcNodeFactory.get(null, null);
            node.randomize();
            configValueMap.put(key, node);
        }
            }

    @Override
    public void transform(MyConfig conf, List<String> keys) throws JsonParseException, JsonMappingException, IOException {
        // TODO implement default somewhere else
        ObjectMapper mapper = new ObjectMapper();
        for (String key : keys) {
            String jsonValue = (String) conf.configValueMap.get(key);
        if (jsonValue == null || jsonValue.isEmpty()) {
            jsonValue = (String) conf.deflt.get(key);
            //System.out.println(conf.deflt);
        }
        CalcNode anode = mapper.readValue(jsonValue, CalcNode.class);
        CalcNode node = CalcNodeFactory.get(anode.className, jsonValue);
        conf.configValueMap.put(key, node);
        }
    }

    @Override
    public void normalize(Map<String, Object> map, List<String> keys) {
        int total = 0;
        for (String key : keys) {
            CalcMACDNode node = (CalcMACDNode) map.get(key);
            int tmpNum = node.getWeight();
            total += tmpNum;
        }
        for (String key : keys) {
            CalcMACDNode node = (CalcMACDNode) map.get(key);
            int tmpNum = node.getWeight();
            node.setWeight(tmpNum * 100 / total);
        }
        
    }
}
