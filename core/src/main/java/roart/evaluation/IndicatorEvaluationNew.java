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
import roart.evolution.Individual;
import roart.util.MarketData;

public class IndicatorEvaluationNew extends Evaluation {
    
    // TODO add deltadays?
    
    private MyMyConfig conf;

    private Object[] retObj;
    
    private boolean useMax;
    
    private String key;
    
    private Integer index;
    
    public IndicatorEvaluationNew(MyMyConfig conf, String key, Object[] retObj, boolean b, Integer index) {
        this.conf = conf;
        this.key = key;
        this.retObj = retObj;
        this.useMax = b;
        this.index = index;
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
            // TODO temp fix
            CalcNode node = (CalcNode) conf.getConfigValueMap().get(key);
            double value = momrsi[index];
            recommend += node.calc(value, 0) * change;
        }
        return recommend;
    }
 
    @Override
    public void mutate() {
        CalcNode node = (CalcNode) conf.getConfigValueMap().get(key);
        node.mutate();
    }

    @Override
    public void getRandom() throws JsonParseException, JsonMappingException, IOException {
        List<Double>[] macdrsiMinMax = (List<Double>[]) retObj[1];
        String name = null;
        if (key.contains("simple")) {
            name = "Double";
        }
        CalcNode node = CalcNodeFactory.get(name, null, macdrsiMinMax, index, useMax);
        node.randomize();
        conf.getConfigValueMap().put(key, node);
        List<String> keys = new ArrayList<>();
        keys.add(key);
        normalize();
    }

    @Override
    public void transformToNode() throws JsonParseException, JsonMappingException, IOException {
        List<Double>[] minMax = (List<Double>[]) retObj[1];
        List<String> keys = new ArrayList<>();
        keys.add(key);
        CalcNodeUtils.transformToNode(conf, keys, useMax, minMax, new ArrayList<>());
    }

    @Override
    public void transformFromNode() throws JsonParseException, JsonMappingException, IOException {
        List<String> keys = new ArrayList<>();
        keys.add(key);
        CalcNodeUtils.transformFromNode(conf, keys, new ArrayList<>());
    }

    @Override
    public void normalize() {
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
        MyMyConfig config = new MyMyConfig(conf);
        List<String> keys = new ArrayList<>();
        keys.add(key);
        evaluation.normalize();
        return new Individual(evaluation);
    }

    @Override
    public Evaluation copy() {
        Evaluation newEval = new IndicatorEvaluationNew(new MyMyConfig(conf), key, retObj, useMax, index);
        return newEval;
    }

}
