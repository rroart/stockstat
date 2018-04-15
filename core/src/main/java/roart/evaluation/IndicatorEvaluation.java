package roart.evaluation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.aggregate.CalcNodeFactory;
import roart.calculate.CalcComplexNode;
import roart.calculate.CalcDoubleNode;
import roart.calculate.CalcNode;
import roart.calculate.CalcNodeUtils;
import roart.config.MyConfig;
import roart.config.MyMyConfig;
import roart.evolution.Individual;
import roart.util.MarketData;

public class IndicatorEvaluation extends Evaluation {
    private List<String> keys;
    
    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }
        
    // TODO add deltadays?
    
    private MyMyConfig conf;

    private Object[] retObj;
    
    private boolean useMax;
    
    private List<String> disableList;
    
    public IndicatorEvaluation(MyMyConfig conf, List<String> keys, Object[] retObj, boolean b, List<String> disableList) {
        this.conf = conf.copy();
        setKeys(keys);
        this.retObj = retObj;
        this.useMax = b;
        this.disableList = disableList;
    }

    public MyMyConfig getConf() {
        return conf;
    }

    public void setConf(MyMyConfig conf) {
        this.conf = conf;
    }

    public Object[] getRetObj() {
        return retObj;
    }

    public void setRetObj(Object[] retObj) {
        this.retObj = retObj;
    }

    public boolean isUseMax() {
        return useMax;
    }

    public void setUseMax(boolean useMax) {
        this.useMax = useMax;
    }

    public List<String> getDisableList() {
        return disableList;
    }

    public void setDisableList(List<String> disableList) {
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
    public double getEvaluations(int j) throws JsonParseException, JsonMappingException, IOException {
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
            for (int i = 0; i < getKeys().size(); i++) {
                String key = getKeys().get(i);
                if (disableList.contains(key)) {
                    continue;
                }
                // TODO temp fix
                CalcNode node = (CalcNode) conf.getConfigValueMap().get(key);
                double value = momrsi[i];
                recommend += node.calc(value, 0) * change;
            }
        }
        return recommend;
    }
 
    @Override
    public void mutate() {        
        for (String key : keys) {
            if (disableList.contains(key)) {
                continue;
            }
            CalcNode node = (CalcNode) conf.getConfigValueMap().get(key);
            node.mutate();
        }
    }

    @Override
    public void getRandom() throws JsonParseException, JsonMappingException, IOException {
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
            conf.getConfigValueMap().put(key, node);
        }
        normalize();
    }

    @Override
    public void transformToNode() throws JsonParseException, JsonMappingException, IOException {
        List<Double>[] minMax = (List<Double>[]) retObj[1];
        CalcNodeUtils.transformToNode(conf, keys, useMax, minMax, disableList);
    }

    @Override
    public void transformFromNode() throws JsonParseException, JsonMappingException, IOException {
        CalcNodeUtils.transformFromNode(conf, keys, disableList);
    }

    @Override
    public void normalize() {
        int total = 0;
        for (String key : keys) {
            if (disableList.contains(key)) {
                continue;
            }
            CalcNode anode = (CalcNode) conf.getConfigValueMap().get(key);
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
            CalcNode anode = (CalcNode) conf.getConfigValueMap().get(key);
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
    public double getFitness() throws JsonParseException, JsonMappingException, IOException {
        int macdlen = conf.getTableDays();

        double testRecommendQualBuySell = 0;
        for (int j = conf.getTestIndicatorRecommenderComplexFutureDays(); j < macdlen; j += conf.getTestIndicatorRecommenderComplexIntervalDays()) {
            testRecommendQualBuySell += getEvaluations(j);
        }
        return testRecommendQualBuySell;
    }

    @Override
    public Individual crossover(Evaluation evaluation) {
        Random rand = new Random();
        Map<String, Object> configValueMap = new HashMap<>(((IndicatorEvaluation) evaluation).conf.getConfigValueMap());
        for (String key : keys) {
            Object value;
            if (rand.nextBoolean()) {
                value = conf.getConfigValueMap().get(key);
            } else {
                value = ((IndicatorEvaluation) evaluation).conf.getConfigValueMap().get(key);
            }
            configValueMap.put(key, value);
        }
        MyMyConfig config = new MyMyConfig(conf);
        evaluation.normalize();
        config.setConfigValueMap(configValueMap);

        return new Individual(evaluation);
    }

    @Override
    public Evaluation copy() {
        Evaluation newEval = new IndicatorEvaluation(new MyMyConfig(conf), new ArrayList<String>(keys), retObj, useMax, disableList);
        return newEval;
    }

}
