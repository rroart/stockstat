package roart.evolution.chromosome.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

class IndicatorChromosome3 {
    
}

public class IndicatorChromosome2 extends AbstractChromosome {
    protected static Logger log = LoggerFactory.getLogger(AbstractChromosome.class);

    private List<String> keys;
    
    private IclijConfig conf;

    private boolean useMax;

    private Object[] retObj;

    private List<String> disableList = new ArrayList<>();

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public IndicatorChromosome2(List<String> keys, IclijConfig conf, Object[] retObj) {
        setKeys(keys);
        this.conf = conf;
        this.retObj = retObj;
    }

    public  List<String> getBuyList() {
        return new ArrayList<>();
    }

    public  List<String> getSellList() {
        return new ArrayList<>();
    }

    public static void addToLists(Map<String, MarketData> marketdatamap, int category, List<Double> macdLists[], String market, Double[] momentum) throws Exception {
        for (int i = 0; i < macdLists.length; i ++) {
            List<Double> macdList = macdLists[i];
            if (momentum[i] != null) {
                macdList.add(momentum[i]);
            }
        }
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
            /*
            if (disableList.contains(key)) {
                continue;
            }
            */
            CalcGene node = (CalcGene) conf.getConfigData().getConfigValueMap().get(key);
            node.mutate();
        }
    }

    @Override
    public void getRandom() throws JsonParseException, JsonMappingException, IOException {
        List<Double>[] macdrsiMinMax = (List<Double>[]) retObj[1];
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            /*
            if (disableList.contains(key)) {
                continue;
            }
            */
            String name = null;
            if (key.contains("simple")) {
                name = "Double";
            }
            CalcGene node = CalcGeneFactory.get(name, null, macdrsiMinMax, i, useMax);
            node.randomize();
            conf.getConfigData().getConfigValueMap().put(key, node);
        }
        normalize();
    }

    @Override
    public void transformToNode() throws JsonParseException, JsonMappingException, IOException {
        List<Double>[] minMax = (List<Double>[]) retObj[1];
        CalcGeneUtils.transformToNode(conf, keys, useMax, minMax, disableList);
    }

    @Override
    public void transformFromNode() throws JsonParseException, JsonMappingException, IOException {
        CalcGeneUtils.transformFromNode(conf, keys, disableList );
    }

    @Override
    public void normalize() {
        int total = 0;
        for (String key : keys) {
            /*
            if (disableList.contains(key)) {
                continue;
            }
            */
            CalcGene anode = (CalcGene) conf.getConfigData().getConfigValueMap().get(key);
            int tmpNum = 0;
            if (anode instanceof CalcComplexGene) {
                CalcComplexGene node = (CalcComplexGene) anode;
                tmpNum = node.getWeight();
            } else {
                CalcDoubleGene node = (CalcDoubleGene) anode;
                tmpNum = node.getWeight();               
            }
            total += tmpNum;
        }
        for (String key : keys) {
            if (total == 0) {
                continue;
            }
            /*
            if (disableList.contains(key) || total == 0) {
                continue;
            }
            */
            log.info("Class cast for key {}", key);
            CalcGene anode = (CalcGene) conf.getConfigData().getConfigValueMap().get(key);
            int tmpNum = 0;
            if (anode instanceof CalcComplexGene) {
                CalcComplexGene node = (CalcComplexGene) anode;
                tmpNum = node.getWeight();
                node.setWeight(tmpNum * 100 / total);
            } else {
                CalcDoubleGene node = (CalcDoubleGene) anode;
                tmpNum = node.getWeight();               
                node.setWeight(tmpNum * 100 / total);
            }
        }
    }

    @Override
    public Individual crossover(AbstractChromosome evaluation) {
        Random rand = new Random();
        Map<String, Object> configValueMap = new HashMap<>(((IndicatorChromosome2) evaluation).conf.getConfigData().getConfigValueMap());
        for (String key : keys) {
            Object value;
            if (rand.nextBoolean()) {
                value = conf.getConfigData().getConfigValueMap().get(key);
            } else {
                value = ((IndicatorChromosome2) evaluation).conf.getConfigData().getConfigValueMap().get(key);
            }
            configValueMap.put(key, value);
        }
        IclijConfig config = new IclijConfig(conf);
        evaluation.normalize();
        config.getConfigData().setConfigValueMap(configValueMap);

        return new Individual(evaluation);
    }

    @Override
    public AbstractChromosome copy() {
        AbstractChromosome newEval = new IndicatorChromosome2(new ArrayList<String>(keys), conf, retObj);
        return newEval;
    }

    @Override
    public boolean isEmpty() {
        for (String key : keys) {
            Object object = conf.getConfigData().getConfigValueMap().get(key);
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
            ret = ret + conf.getConfigData().getConfigValueMap().get(key) + " ";
        }
        return ret;
    }

    @Override
    public double getEvaluations(int j) throws JsonParseException, JsonMappingException, IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getFitness() throws JsonParseException, JsonMappingException, IOException {
        // TODO Auto-generated method stub
        return 0;
    }

}
