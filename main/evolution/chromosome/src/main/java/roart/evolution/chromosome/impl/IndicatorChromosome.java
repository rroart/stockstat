package roart.evolution.chromosome.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.calculate.CalcNodeFactory;
import roart.calculate.CalcComplexNode;
import roart.calculate.CalcDoubleNode;
import roart.calculate.CalcNode;
import roart.calculate.CalcNodeUtils;
import roart.common.config.MyConfig;
import roart.common.config.MyMyConfig;
import roart.common.pipeline.PipelineConstants;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.fitness.AbstractScore;
import roart.evolution.species.Individual;
import roart.model.data.MarketData;

public class IndicatorChromosome extends AbstractChromosome {
    protected static Logger log = LoggerFactory.getLogger(AbstractChromosome.class);
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

    private AbstractScore evalUtil;
    
    public IndicatorChromosome(MyMyConfig conf, List<String> keys, Object[] retObj, boolean b, List<String> disableList, AbstractScore evalUtil) {
        this.conf = conf.copy();
        setKeys(keys);
        this.retObj = retObj;
        this.useMax = b;
        this.disableList = disableList;
        this.evalUtil = evalUtil;
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
        int count = 0;
        int listlen = conf.getTableDays();
        List<Map<String, Double[][]>> listList = (List<Map<String, Double[][]>>) retObj[2];
        Map<Integer, Map<String, Double[]>> dayIndicatorMap = (Map<Integer, Map<String, Double[]>>) retObj[0];
        Map<String, Double[]> indicatorMap = dayIndicatorMap.get(j);
        // find recommendations
        if (indicatorMap == null) {
            return 0;
        }
        Map<String, List<Double>> resultMap = new HashMap<>();
        for (Entry<String, Double[]> entry : indicatorMap.entrySet()) {
            String id = entry.getKey();
            int newlistidx = listlen - 1 - j + conf.getTestIndicatorRecommenderComplexFutureDays();
            int curlistidx = listlen - 1 - j;
            Double[] list = listList.get(0).get(id)[0];
            if (list[newlistidx] == null || list[curlistidx] == null) {
                continue;
            }
            // TODO change filtering?
            double change = (list[newlistidx]/list[curlistidx] - 0);
            Double[] momrsi = entry.getValue();
            double recommend = 0;
            for (int i = 0; i < getKeys().size(); i++) {
                String key = getKeys().get(i);
                if (disableList.contains(key)) {
                    continue;
                }
                // TODO temp fix
                Object o = conf.getConfigValueMap().get(key);
                if (conf.getConfigValueMap().get(key) instanceof Integer) {
                    int jj = 0;
                }
                CalcNode node = (CalcNode) conf.getConfigValueMap().get(key);
                double value = momrsi[i];
                recommend += node.calc(value, 0); // (1 + change); // Math.pow(1 + change, 10);
                count++;
            }
            List<Double> resultList = new ArrayList<>();
            resultList.add(recommend);
            resultList.add(change);
            resultMap.put(id, resultList);
        }
        double finalRecommend = evalUtil.calculateResult(resultMap);
        //finalRecommend *= 0.1;
        //double reco = count * (1 - Math.abs(finalRecommend-count)/Math.max(Math.abs(finalRecommend), count));
        log.info("Recommend {}", finalRecommend);
        return finalRecommend;
    }

    class BuySellList implements Comparable<Double> {
        String id;
        Double change;
        Double score;
        
        @Override
        public int compareTo(Double score0) {
            return Double.compare(score0, score);
        }
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
            // scale down wrt max?
            testRecommendQualBuySell += getEvaluations(j);
        }
        return testRecommendQualBuySell;
    }

    @Override
    public Individual crossover(AbstractChromosome evaluation) {
        Random rand = new Random();
        Map<String, Object> configValueMap = new HashMap<>(((IndicatorChromosome) evaluation).conf.getConfigValueMap());
        for (String key : keys) {
            Object value;
            if (rand.nextBoolean()) {
                value = conf.getConfigValueMap().get(key);
            } else {
                value = ((IndicatorChromosome) evaluation).conf.getConfigValueMap().get(key);
            }
            configValueMap.put(key, value);
        }
        MyMyConfig config = new MyMyConfig(conf);
        evaluation.normalize();
        config.setConfigValueMap(configValueMap);

        return new Individual(evaluation);
    }

    @Override
    public AbstractChromosome copy() {
        AbstractChromosome newEval = new IndicatorChromosome(new MyMyConfig(conf), new ArrayList<String>(keys), retObj, useMax, disableList, evalUtil);
        return newEval;
    }

    @Override
    public boolean isEmpty() {
        for (String key : keys) {
            Object object = conf.getConfigValueMap().get(key);
            if (object == null) {
                return true;
            }
            boolean found = false;
            if (object instanceof String) {
                String value = (String) object;
                if (value.isEmpty()) {
                    return true;
                }
                found = true;
            }
            if (object instanceof Integer) {
                return false;
            }
            if (!found) {
                int jj = 0;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String ret = "";
        for (String key : keys) {
            ret = ret + conf.getConfigValueMap().get(key) + " ";
        }
        return ret;
    }
}
