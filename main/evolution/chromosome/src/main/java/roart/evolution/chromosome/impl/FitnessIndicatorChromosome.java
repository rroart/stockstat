package roart.evolution.chromosome.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.fitness.AbstractScore;
import roart.evolution.fitness.Fitness;
import roart.gene.CalcGene;
//public class FitnessIndicatorChromosome {}

public class FitnessIndicatorChromosome extends Fitness {
    protected static Logger log = LoggerFactory.getLogger(AbstractChromosome.class);

    private IclijConfig conf;

    private Object[] retObj;

    private List<String> disableList;

    private AbstractScore evalUtil;
    
    private int listlen;
    
    private double threshold;
    
    public FitnessIndicatorChromosome(IclijConfig conf, Object[] retObj, AbstractScore evalUtil, int listlen, Double threshold) {
        this.conf = conf.copy();
        this.retObj = retObj;
        this.evalUtil = evalUtil;
        this.listlen = listlen;
        this.threshold = threshold;
    }

    @Override
    public double fitness(AbstractChromosome chromosome) {
        double testRecommendQualBuySell = 0;
        for (int j = conf.getTestIndicatorRecommenderComplexFutureDays(); j < listlen; j += conf.getTestIndicatorRecommenderComplexIntervalDays()) {
            // scale down wrt max?
            try {
                testRecommendQualBuySell += getEvaluations(j, (IndicatorChromosome2) chromosome);
            } catch (IOException e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        return testRecommendQualBuySell;
    }

    //@Override
    public double getEvaluations(int j, IndicatorChromosome2 chromosome) throws JsonParseException, JsonMappingException, IOException {
        int count = 0;
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
            // change filtering?
            double change = (list[newlistidx]/list[curlistidx] - 0);
            Double[] momrsi = entry.getValue();
            double recommend = 0;
            for (int i = 0; i < chromosome.getKeys().size(); i++) {
                String key = chromosome.getKeys().get(i);
                if (disableList.contains(key)) {
                    continue;
                }
                // temp fix
                Object o = conf.getConfigData().getConfigValueMap().get(key);
                if (conf.getConfigData().getConfigValueMap().get(key) instanceof Integer) {
                    int jj = 0;
                }
                CalcGene node = (CalcGene) conf.getConfigData().getConfigValueMap().get(key);
                double value = momrsi[i];
                double calc = node.calc(value, 0); // (1 + change); // Math.pow(1 + change, 10);
                if (Double.isNaN(calc)) {
                    int jj = 0;
                } else {
                    recommend += calc;
                }
                count++;
            }
            List<Double> resultList = new ArrayList<>();
            resultList.add(recommend);
            resultList.add(change);
            resultMap.put(id, resultList);
        }
        double finalRecommend = evalUtil.calculateResult(resultMap, threshold);
        //finalRecommend *= 0.1;
        //double reco = count * (1 - Math.abs(finalRecommend-count)/Math.max(Math.abs(finalRecommend), count));
        log.debug("Recommend {}", finalRecommend);
        return finalRecommend;
    }

}

