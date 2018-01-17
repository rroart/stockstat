package roart.evaluation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.aggregate.CalcNodeFactory;
import roart.calculate.CalcComplexNode;
import roart.calculate.CalcDoubleNode;
import roart.calculate.CalcNode;
import roart.calculate.CalcNodeUtils;
import roart.config.MyConfig;
import roart.config.MyMyConfig;
import roart.util.MarketData;

public class IndicatorEvaluation extends Evaluation {
    
    // TODO add deltadays?
    
    public MyMyConfig conf;
    public Object[] retObj;
    public boolean useMax;
    public List<String> disableList;
    
    public IndicatorEvaluation(MyMyConfig conf, List<String> keys, Object[] retObj, boolean b, List<String> disableList) {
        this.conf = conf;
        this.keys = keys;
        this.retObj = retObj;
        this.useMax = b;
        this.disableList = disableList;
    }

    public  List<String> getBuyList() {
        return new ArrayList<>();
    }

    public  List<String> getSellList() {
        return new ArrayList<>();
    }

    public static void addToLists(Map<String, MarketData> marketdatamap, int category, List<Double> macdLists[] /*List<Double> macdList,
            List<Double> histList, List<Double> macdDList, List<Double> histDList*/, String market, Double[] momentum) throws Exception {
            for (int i = 0; i < macdLists.length; i ++) {
                List<Double> macdList = macdLists[i];
                if (momentum[i] != null) {
                    macdList.add(momentum[i]);
                }
            }
    }

    @Override
    public double getEvaluations(MyMyConfig conf, int j) throws JsonParseException, JsonMappingException, IOException {
        int listlen = conf.getTableDays();
        List<Map<String, Double[][]>> listList = (List<Map<String, Double[][]>>) retObj[2];
        Map<Integer, Map<String, Double[]>> dayIndicatorMap = (Map<Integer, Map<String, Double[]>>) retObj[0];
        Map<String, Double[]> indicatorMap = dayIndicatorMap.get(j);
        // find recommendations
        double recommend = 0;
        if (indicatorMap == null) {
            return 0;
        }
        for (Entry<String, Double[]> entry : indicatorMap.entrySet()) {
            String id = entry.getKey();
            int newlistidx = listlen - 1 - j + conf.getTestIndicatorRecommenderComplexFutureDays();
            int curlistidx = listlen - 1 - j;
            Double[] list = listList.get(0).get(id)[0];
            if (list[newlistidx] == null || list[curlistidx] == null) {
                continue;
            }
	    // TODO change filtering?
            double change = (list[newlistidx]/list[curlistidx] - 1);
            Double[] momrsi = entry.getValue();
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                if (disableList.contains(key)) {
                    continue;
                }
                // TODO temp fix
                CalcNode node = (CalcNode) conf.configValueMap.get(key);
                double value = momrsi[i];
                recommend += node.calc(value, 0) * change;
            }
        }
        return recommend;
    }
 
    @Override
    public void mutate(Map<String, Object> configValueMap, List<String> keys) {
        for (String key : keys) {
            if (disableList.contains(key)) {
                continue;
            }
            CalcNode node = (CalcNode) configValueMap.get(key);
            node.mutate();
        }
    }

    @Override
    public void getRandom(Map<String, Object> configValueMap, List<String> keys) throws JsonParseException, JsonMappingException, IOException {
        List<Double>[] macdrsiMinMax = (List<Double>[]) retObj[1];
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            if (disableList.contains(key)) {
                continue;
            }
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
        List<Double>[] minMax = (List<Double>[]) retObj[1];
        CalcNodeUtils.transformToNode(conf, keys, useMax, minMax, disableList);
    }

    @Override
    public void transformFromNode(MyConfig conf, List<String> keys) throws JsonParseException, JsonMappingException, IOException {
        CalcNodeUtils.transformFromNode(conf, keys, disableList);
    }

    @Override
    public void normalize(Map<String, Object> map, List<String> keys) {
        int total = 0;
        for (String key : keys) {
            if (disableList.contains(key)) {
                continue;
            }
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
            if (disableList.contains(key) || total == 0) {
                continue;
            }
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

        double testRecommendQualBuySell = 0;
        for (int j = conf.getTestIndicatorRecommenderComplexFutureDays(); j < macdlen; j += conf.getTestIndicatorRecommenderComplexIntervalDays()) {
            testRecommendQualBuySell += getEvaluations(testBuySellConfig, j);
        }
        return testRecommendQualBuySell;
    }

}
