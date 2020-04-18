package roart.iclij.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.EvolveMLConfig;
import roart.iclij.config.MLConfigs;

public class MLUtil {
    @Deprecated
    private Map<String, String> getMapOld() {
        Map<String, String> map = new HashMap<>();
        map.put(MLConstants.MLPC, ConfigConstants.MACHINELEARNINGSPARKMLMLPC);
        map.put(MLConstants.LOR, ConfigConstants.MACHINELEARNINGSPARKMLLOR);
        map.put(MLConstants.OVR, ConfigConstants.MACHINELEARNINGSPARKMLOVR);
        map.put(MLConstants.LSVC, ConfigConstants.MACHINELEARNINGSPARKMLLSVC);
        map.put(MLConstants.DNN, ConfigConstants.MACHINELEARNINGTENSORFLOWDNN);
        map.put(MLConstants.LIC, ConfigConstants.MACHINELEARNINGTENSORFLOWLIC);
        map.put(MLConstants.LSTM, ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTM);
        return map;
    }

    public Map<String, String> getMlMap() {
        Map<String, String> map = new HashMap<>();
        map.put(MLConstants.SPARK, ConfigConstants.MACHINELEARNINGSPARKML);
        map.put(MLConstants.TENSORFLOW, ConfigConstants.MACHINELEARNINGTENSORFLOW);
        map.put(MLConstants.PYTORCH, ConfigConstants.MACHINELEARNINGPYTORCH);
        map.put(MLConstants.GEM, ConfigConstants.MACHINELEARNINGGEM);
        return map;
    }

    public void disabler(Map<String, Object> valueMap) {
        valueMap.put(ConfigConstants.AGGREGATORS, Boolean.FALSE);
        valueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);
        valueMap.put(ConfigConstants.MACHINELEARNINGPREDICTORS, Boolean.FALSE);
        valueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);
        valueMap.put(ConfigConstants.INDICATORSRSIRECOMMEND, Boolean.FALSE);
        valueMap.put(ConfigConstants.MACHINELEARNINGMLLEARN, Boolean.FALSE);
        valueMap.put(ConfigConstants.MACHINELEARNINGMLCLASSIFY, Boolean.FALSE);
        valueMap.put(ConfigConstants.MACHINELEARNINGMLDYNAMIC, Boolean.FALSE);
    }

}
