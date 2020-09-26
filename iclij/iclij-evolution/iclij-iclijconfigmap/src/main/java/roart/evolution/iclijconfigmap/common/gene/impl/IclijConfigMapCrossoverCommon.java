package roart.evolution.iclijconfigmap.common.gene.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class IclijConfigMapCrossoverCommon {

    public Map<String, Object> crossover(List<String> confList, Map<String, Object> one, Map<String, Object> two) {
        Random random = new Random();
        Map<String, Object> newMap = new HashMap<>(one);
        for (int conf = 0; conf < confList.size(); conf++) {
            String confName = confList.get(conf);
            if (random.nextBoolean()) {
                newMap.put(confName, newMap.get(confName));
            } else {
                newMap.put(confName, two.get(confName));
            }
        }
        /*
        if (!chromosome.validate()) {
            chromosome.fixValidation();
        }
        */
        return newMap;
    }
    
}
