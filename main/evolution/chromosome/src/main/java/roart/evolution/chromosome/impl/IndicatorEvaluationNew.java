package roart.evolution.chromosome.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.iclij.config.IclijConfig;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.species.Individual;
import roart.gene.CalcGene;
import roart.gene.impl.CalcComplexGene;
import roart.gene.impl.CalcDoubleGene;
import roart.gene.impl.CalcGeneFactory;
import roart.gene.impl.CalcGeneUtils;
import roart.model.data.MarketData;

@Deprecated
public class IndicatorEvaluationNew extends AbstractChromosome {
    
    private IclijConfig conf;

    private Object[] retObj;
    
    private boolean useMax;
    
    private String key;
    
    private Integer index;
    
    public IndicatorEvaluationNew(IclijConfig conf, String key, Object[] retObj, boolean b, Integer index) {
        this.conf = conf;
        this.key = key;
        this.retObj = retObj;
        this.useMax = b;
        this.index = index;
    }

    public IclijConfig getConf() {
        return conf;
    }

    public void setConf(IclijConfig conf) {
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
	    // change filtering?
            double change = (list[newlistidx]/list[curlistidx] - 1);
            Double[] momrsi = entry.getValue();
            // temp fix
            CalcGene node = (CalcGene) conf.getConfigValueMap().get(key);
            double value = momrsi[index];
            recommend += node.calc(value, 0) * change;
        }
        return recommend;
    }
 
    @Override
    public void mutate() {
        CalcGene node = (CalcGene) conf.getConfigValueMap().get(key);
        node.mutate();
    }

    @Override
    public void getRandom() throws JsonParseException, JsonMappingException, IOException {
        List<Double>[] macdrsiMinMax = (List<Double>[]) retObj[1];
        String name = null;
        if (key.contains("simple")) {
            name = "Double";
        }
        CalcGene node = CalcGeneFactory.get(name, null, macdrsiMinMax, index, useMax);
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
        CalcGeneUtils.transformToNode(conf, keys, useMax, minMax, new ArrayList<>());
    }

    @Override
    public void transformFromNode() throws JsonParseException, JsonMappingException, IOException {
        List<String> keys = new ArrayList<>();
        keys.add(key);
        CalcGeneUtils.transformFromNode(conf, keys, new ArrayList<>());
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
    public Individual crossover(AbstractChromosome evaluation) {
        IclijConfig config = new IclijConfig(conf);
        List<String> keys = new ArrayList<>();
        keys.add(key);
        evaluation.normalize();
        return new Individual(evaluation);
    }

    @Override
    public AbstractChromosome copy() {
        AbstractChromosome newEval = new IndicatorEvaluationNew(new IclijConfig(conf), key, retObj, useMax, index);
        return newEval;
    }
   
    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isAscending() {
        return true;
    }
}
