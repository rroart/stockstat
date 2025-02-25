package roart.evolution.iclijconfigmap.common.gene.impl;

import java.util.List;
import java.util.Map;
import java.util.Random;

import roart.common.util.MathUtil;
import roart.iclij.config.IclijConfig;

public class IclijConfigMapMutateCommon {

    // not the same as in configmapgene
    public void generateConfigNum(Random rand, int confint, List<String> confList, IclijConfig conf, Map<String, Object> map) {
        String confName = confList.get(confint);
        Double[] range = conf.getConfigData().getConfigMaps().range.get(confName);
        Class type = conf.getConfigData().getConfigMaps().map.get(confName);
        if (type == Boolean.class) {
            Boolean b = rand.nextBoolean();
            map.put(confName, b);
            return;
        }
        if (type == Integer.class) {
            Integer i = (range[0].intValue()) + rand.nextInt(range[1].intValue() - range[0].intValue());
            map.put(confName, i);
            return;
        }
        if (type == Double.class) {
            Double d = (range[0]) + rand.nextDouble() * (range[1] - range[0]);
            if (range.length == 3) {
                d = MathUtil.round(d, range[2].intValue());
            }
            map.put(confName, d);
            return;
        }
        if (type == String.class) {
            Double d = (range[0]) + rand.nextDouble() * (range[1] - range[0]);
            d = MathUtil.round(d, range[2].intValue());
            map.put(confName, "[" + d + "]");
            return;
        }
        //log.error("Unknown type for {}", confName);
    }

}
