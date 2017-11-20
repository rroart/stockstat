package roart.evaluation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.calculate.CalcDoubleNode;
import roart.calculate.CalcComplexNode;
import roart.calculate.CalcNode;
import roart.config.ConfigConstantMaps;
import roart.config.MyConfig;
import roart.config.MyMyConfig;
import roart.evolution.FitnessBuySellMACD;
import roart.model.StockItem;
import roart.util.MarketData;
import roart.util.StockDao;

public class IndicatorEvaluation extends Evaluation {
    
    // TODO add deltadays?
    
    public MyMyConfig conf;
    public Object[] retObj;
    public boolean useMax;
    
    public IndicatorEvaluation(MyMyConfig conf, List<String> keys, Object[] retObj, boolean b) {
        this.conf = conf;
        this.keys = keys;
        this.retObj = retObj;
        this.useMax = b;
    }

    public  List<String> getBuyList() {
        List<String> buyList = new ArrayList<>();
        return buyList;
    }

    public  List<String> getSellList() {
        List<String> sellList = new ArrayList<>();
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
    public double getEvaluations(MyMyConfig conf, int j) throws JsonParseException, JsonMappingException, IOException {
        int listlen = conf.getTableDays();
        List<Map<String, Double[][]>> listList = (List<Map<String, Double[][]>>) retObj[2];
        Map<Integer, Map<String, Double[]>> dayIndicatorMap = (Map<Integer, Map<String, Double[]>>) retObj[0];
        //List<Double>[] macdrsiMinMax = (List<Double>[]) retObj[1];
        Map<String, Double[]> indicatorMap = dayIndicatorMap.get(j);
        // find recommendations
        double recommend = 0;
        //transform(conf, buyList);
        if (indicatorMap == null) {
            return 0;
        }
        for (String id : indicatorMap.keySet()) {
            //System.out.println(Arrays.toString(indicatorMap.get(id)));
            int newlistidx = listlen - 1 - j + conf.getTestIndicatorRecommenderComplexFutureDays();
            int curlistidx = listlen - 1 - j;
            Double[] list = listList.get(0).get(id)[0];
            if (list[newlistidx] == null || list[curlistidx] == null) {
                continue;
            }
	    // TODO change filtering?
            double change = (list[newlistidx]/list[curlistidx] - 1) / 100;
            Double[] momrsi = indicatorMap.get(id);
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                // TODO temp fix
                CalcNode node = (CalcNode) conf.configValueMap.get(key);
                //node.setDoBuy(useMax);
                double value = momrsi[i];
                recommend += node.calc(value, 0) * change * 1000 * 100;
            }
        }
        return recommend;
    }

    /*
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
    public void mutate(Map<String, Object> configValueMap, List<String> keys) {
        for (String key : keys) {
            CalcNode node = (CalcNode) configValueMap.get(key);
            node.mutate();
        }
    }

    @Override
    public void getRandom(Map<String, Object> configValueMap, List<String> keys) throws JsonParseException, JsonMappingException, IOException {
        List<Double>[] macdrsiMinMax = (List<Double>[]) retObj[1];
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String name = null;
            if (key.contains("simple")) {
                name = "Double";
            }
            CalcNode node = CalcNodeFactory.get(name, null, macdrsiMinMax, i, useMax);
            node.randomize();
            configValueMap.put(key, node);
        }
        normalize(configValueMap, keys);
    }

    @Override
    public void transformToNode(MyConfig conf, List<String> keys) throws JsonParseException, JsonMappingException, IOException {
        // TODO implement default somewhere else
        List<Double>[] macdrsiMinMax = (List<Double>[]) retObj[1];
        ObjectMapper mapper = new ObjectMapper();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            Object value = conf.configValueMap.get(key);
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

    @Override
    public void transformFromNode(MyConfig conf, List<String> keys) throws JsonParseException, JsonMappingException, IOException {
        // TODO implement default somewhere else
        ObjectMapper mapper = new ObjectMapper();
        for (String key : keys) {
            CalcNode node = (CalcNode) conf.configValueMap.get(key);
            if (node instanceof CalcComplexNode) {
                String string = mapper.writeValueAsString(node);
                conf.configValueMap.put(key, string);
            } else {
                CalcDoubleNode anode = (CalcDoubleNode) node;
                conf.configValueMap.put(key, anode.getWeight());
            }
        }
    }

    @Override
    public void normalize(Map<String, Object> map, List<String> keys) {
        int total = 0;
        for (String key : keys) {
            CalcNode anode = (CalcNode) map.get(key);
            int tmpNum = 0;
            if (anode instanceof CalcComplexNode) {
                CalcComplexNode node = (CalcComplexNode) anode;
                tmpNum = node.getWeight();
            } else {
                CalcDoubleNode node = (CalcDoubleNode) anode;
                tmpNum = node.getWeight();               
            }
            total += tmpNum;
        }
        for (String key : keys) {
            CalcNode anode = (CalcNode) map.get(key);
            int tmpNum = 0;
            if (anode instanceof CalcComplexNode) {
                CalcComplexNode node = (CalcComplexNode) anode;
                tmpNum = node.getWeight();
                node.setWeight(tmpNum * 100 / total);
            } else {
                CalcDoubleNode node = (CalcDoubleNode) anode;
                tmpNum = node.getWeight();               
                node.setWeight(tmpNum * 100 / total);
            }
        }
    }

    @Override
    public double getFitness(MyMyConfig testBuySellConfig, List<String> keys) throws JsonParseException, JsonMappingException, IOException {
        int macdlen = conf.getTableDays();
        int listlen = conf.getTableDays();

        double testRecommendQualBuySell = 0;
        for (int j = conf.getTestIndicatorRecommenderComplexFutureDays(); j < macdlen; j += conf.getTestIndicatorRecommenderComplexIntervalDays()) {
            //List<Double> macdLists[] = macdMinMax.get(j);
            //int newmacdidx = macdlen - 1 - j + conf.getTestRecommendFutureDays();
            //int curmacdidx = macdlen - 1 - j;
            Map<Integer, Map<String, Double[]>> dayIndicatorMap = (Map<Integer, Map<String, Double[]>>) retObj[0];
            Map<String, Double[]> momrsiMap = dayIndicatorMap.get(j);
           //System.out.println("j"+j);
            testRecommendQualBuySell += getEvaluations(testBuySellConfig, j);
            int newlistidx = listlen - 1 - j + conf.getTestIndicatorRecommenderComplexFutureDays();
            int curlistidx = listlen - 1 - j;
            //testRecommendQualBuySell += MACDRecommend.getQuality(buy, buysellMap, listMap, curlistidx, newlistidx);
        }
        return testRecommendQualBuySell;
    }

}
