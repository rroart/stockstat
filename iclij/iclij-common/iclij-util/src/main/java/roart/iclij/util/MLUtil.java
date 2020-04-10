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
import roart.common.util.JsonUtil;
import roart.iclij.config.EvolveMLConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.MLConfigs;
import roart.iclij.config.Market;
import roart.iclij.model.config.ActionComponentConfig;

public class MLUtil {
    public Map<String, EvolveMLConfig> getMLConfig(Market market, IclijConfig config, String mlmarket, ActionComponentConfig componentConfig) {
        System.out.println(config.getEvolveMLMLConfig());
        String localMl = componentConfig.getLocalMLConfig(config);
        String ml = config.getEvolveMLMLConfig();
        MLConfigs marketMlConfig = market.getMlconfig();
        MLConfigs mlConfig = JsonUtil.convert(ml, MLConfigs.class);
        MLConfigs localMLConfig = JsonUtil.convert(localMl, MLConfigs.class);
        // special
        mlConfig.merge(localMLConfig);
        mlConfig.merge(marketMlConfig);
        Map<String, EvolveMLConfig> mlConfigMap = getMLConfigs(mlConfig);
        return mlConfigMap;
    }

    private Map<String, EvolveMLConfig> getMLConfigs(MLConfigs mlConfig) {
        return mlConfig.getAll();
    }
    
    public List<String> getSubComponents(Market market, IclijConfig config, String mlmarket, ActionComponentConfig componentConfig) {
        List<String> subComponents = new ArrayList<>();
        Map<String, Pair<String, String>> revMap = getMapRev();
        Map<String, EvolveMLConfig> mlConfigs = getMLConfig(market, config, mlmarket, componentConfig);
        for (Entry<String, EvolveMLConfig> entry : mlConfigs.entrySet()) {
            EvolveMLConfig mlConfig = entry.getValue();
            if (mlConfig.getEnable()) {
                String key = entry.getKey();
                Pair<String, String> subComponent = revMap.get(key);
                subComponents.add(subComponent.getLeft() + " " + subComponent.getRight());
            } else {
                int jj = 0;
            }
        }
        return subComponents;
    }

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

    public Map<Pair<String, String>, String> getMap() {
        Map<Pair <String, String>, String> map = new HashMap<>();
        map.put(new ImmutablePair(MLConstants.SPARK, MLConstants.MLPC), ConfigConstants.MACHINELEARNINGSPARKMLMLPC);
        map.put(new ImmutablePair(MLConstants.SPARK, MLConstants.LOR), ConfigConstants.MACHINELEARNINGSPARKMLLOR);
        map.put(new ImmutablePair(MLConstants.SPARK, MLConstants.OVR), ConfigConstants.MACHINELEARNINGSPARKMLOVR);
        map.put(new ImmutablePair(MLConstants.SPARK, MLConstants.LSVC), ConfigConstants.MACHINELEARNINGSPARKMLLSVC);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.DNN), ConfigConstants.MACHINELEARNINGTENSORFLOWDNN);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.LIC), ConfigConstants.MACHINELEARNINGTENSORFLOWLIC);
        //map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.LIR), ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLIR);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.MLP), ConfigConstants.MACHINELEARNINGTENSORFLOWMLP);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.CNN), ConfigConstants.MACHINELEARNINGTENSORFLOWCNN);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.CNN2), ConfigConstants.MACHINELEARNINGTENSORFLOWCNN2);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.RNN), ConfigConstants.MACHINELEARNINGTENSORFLOWRNN);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.LSTM), ConfigConstants.MACHINELEARNINGTENSORFLOWLSTM);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.GRU), ConfigConstants.MACHINELEARNINGTENSORFLOWGRU);
        map.put(new ImmutablePair(MLConstants.PYTORCH, MLConstants.MLP), ConfigConstants.MACHINELEARNINGPYTORCHMLP);
        map.put(new ImmutablePair(MLConstants.PYTORCH, MLConstants.CNN), ConfigConstants.MACHINELEARNINGPYTORCHCNN);
        map.put(new ImmutablePair(MLConstants.PYTORCH, MLConstants.CNN2), ConfigConstants.MACHINELEARNINGPYTORCHCNN2);
        map.put(new ImmutablePair(MLConstants.PYTORCH, MLConstants.RNN), ConfigConstants.MACHINELEARNINGPYTORCHRNN);
        map.put(new ImmutablePair(MLConstants.PYTORCH, MLConstants.LSTM), ConfigConstants.MACHINELEARNINGPYTORCHLSTM);
        map.put(new ImmutablePair(MLConstants.PYTORCH, MLConstants.GRU), ConfigConstants.MACHINELEARNINGPYTORCHGRU);
        map.put(new ImmutablePair(MLConstants.GEM, MLConstants.EWC), ConfigConstants.MACHINELEARNINGGEMEWC);
        map.put(new ImmutablePair(MLConstants.GEM, MLConstants.GEM), ConfigConstants.MACHINELEARNINGGEMGEM);
        map.put(new ImmutablePair(MLConstants.GEM, MLConstants.I), ConfigConstants.MACHINELEARNINGGEMINDEPENDENT);
        map.put(new ImmutablePair(MLConstants.GEM, MLConstants.ICARL), ConfigConstants.MACHINELEARNINGGEMICARL);
        map.put(new ImmutablePair(MLConstants.GEM, MLConstants.MM), ConfigConstants.MACHINELEARNINGGEMMULTIMODAL);
        map.put(new ImmutablePair(MLConstants.GEM, MLConstants.S), ConfigConstants.MACHINELEARNINGGEMSINGLE);
        return map;
    }

    public List<String> getOtherList() {
        List<String> map = new ArrayList<>();
        map.add(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLIR);
        map.add(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWMLP);
        map.add(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWRNN);
        map.add(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTM);
        map.add(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWGRU);
        map.add(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHMLP);
        map.add(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHRNN);
        map.add(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHLSTM);
        map.add(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHGRU);
        return map;
    }

    public Map<Pair<String, String>, String> getMapPersist() {
        Map<Pair <String, String>, String> map = new HashMap<>();
        map.put(new ImmutablePair(MLConstants.SPARK, MLConstants.MLPC), ConfigConstants.MACHINELEARNINGSPARKMLMLPCPERSIST);
        map.put(new ImmutablePair(MLConstants.SPARK, MLConstants.LOR), ConfigConstants.MACHINELEARNINGSPARKMLLORPERSIST);
        map.put(new ImmutablePair(MLConstants.SPARK, MLConstants.OVR), ConfigConstants.MACHINELEARNINGSPARKMLOVRPERSIST);
        map.put(new ImmutablePair(MLConstants.SPARK, MLConstants.LSVC), ConfigConstants.MACHINELEARNINGSPARKMLLSVCPERSIST);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.DNN), ConfigConstants.MACHINELEARNINGTENSORFLOWDNNPERSIST);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.LIC), ConfigConstants.MACHINELEARNINGTENSORFLOWLICPERSIST);
        //map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.LIR), ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLIRPERSIST);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.MLP), ConfigConstants.MACHINELEARNINGTENSORFLOWMLPPERSIST);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.CNN), ConfigConstants.MACHINELEARNINGTENSORFLOWCNNPERSIST);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.CNN2), ConfigConstants.MACHINELEARNINGTENSORFLOWCNN2PERSIST);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.RNN), ConfigConstants.MACHINELEARNINGTENSORFLOWRNNPERSIST);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.LSTM), ConfigConstants.MACHINELEARNINGTENSORFLOWLSTMPERSIST);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.GRU), ConfigConstants.MACHINELEARNINGTENSORFLOWGRUPERSIST);
        map.put(new ImmutablePair(MLConstants.PYTORCH, MLConstants.MLP), ConfigConstants.MACHINELEARNINGPYTORCHMLPPERSIST);
        map.put(new ImmutablePair(MLConstants.PYTORCH, MLConstants.CNN), ConfigConstants.MACHINELEARNINGPYTORCHCNNPERSIST);
        map.put(new ImmutablePair(MLConstants.PYTORCH, MLConstants.CNN2), ConfigConstants.MACHINELEARNINGPYTORCHCNN2PERSIST);
        map.put(new ImmutablePair(MLConstants.PYTORCH, MLConstants.RNN), ConfigConstants.MACHINELEARNINGPYTORCHRNNPERSIST);
        map.put(new ImmutablePair(MLConstants.PYTORCH, MLConstants.LSTM), ConfigConstants.MACHINELEARNINGPYTORCHLSTMPERSIST);
        map.put(new ImmutablePair(MLConstants.PYTORCH, MLConstants.GRU), ConfigConstants.MACHINELEARNINGPYTORCHGRUPERSIST);
        map.put(new ImmutablePair(MLConstants.GEM, MLConstants.EWC), ConfigConstants.MACHINELEARNINGGEMEWCPERSIST);
        map.put(new ImmutablePair(MLConstants.GEM, MLConstants.GEM), ConfigConstants.MACHINELEARNINGGEMGEMPERSIST);
        map.put(new ImmutablePair(MLConstants.GEM, MLConstants.I), ConfigConstants.MACHINELEARNINGGEMINDEPENDENTPERSIST);
        map.put(new ImmutablePair(MLConstants.GEM, MLConstants.ICARL), ConfigConstants.MACHINELEARNINGGEMICARLPERSIST);
        map.put(new ImmutablePair(MLConstants.GEM, MLConstants.MM), ConfigConstants.MACHINELEARNINGGEMMULTIMODALPERSIST);
        map.put(new ImmutablePair(MLConstants.GEM, MLConstants.S), ConfigConstants.MACHINELEARNINGGEMSINGLEPERSIST);
        return map;
    }

    protected Map<String, Pair<String, String>> getMapRev() {
        Map<Pair<String, String>, String> aMap = getMap();
        Map<String, Pair<String, String>> retMap = new HashMap<>();
        for (Entry<Pair<String, String>, String> entry : aMap.entrySet()) {
            retMap.put(entry.getValue(), entry.getKey());
        }
        return retMap;
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

    public Map<Pair<String, String>, String> getMapPred() {
        Map<Pair <String, String>, String> map = new HashMap<>();
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.LIR), ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLIR);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.MLP), ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWMLP);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.RNN), ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWRNN);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.LSTM), ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTM);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.GRU), ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWGRU);
        map.put(new ImmutablePair(MLConstants.PYTORCH, MLConstants.MLP), ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHMLP);
        map.put(new ImmutablePair(MLConstants.PYTORCH, MLConstants.RNN), ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHRNN);
        map.put(new ImmutablePair(MLConstants.PYTORCH, MLConstants.LSTM), ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHLSTM);
        map.put(new ImmutablePair(MLConstants.PYTORCH, MLConstants.GRU), ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHGRU);
        return map;
    }

    public List<String> getOtherListPred() {
        List<String> map = new ArrayList<>();
        map.add(ConfigConstants.MACHINELEARNINGTENSORFLOWDNN);
        map.add(ConfigConstants.MACHINELEARNINGTENSORFLOWLIC);
        map.add(ConfigConstants.MACHINELEARNINGTENSORFLOWMLP);
        map.add(ConfigConstants.MACHINELEARNINGTENSORFLOWCNN);
        map.add(ConfigConstants.MACHINELEARNINGTENSORFLOWCNN2);
        map.add(ConfigConstants.MACHINELEARNINGTENSORFLOWRNN);
        map.add(ConfigConstants.MACHINELEARNINGTENSORFLOWLSTM);
        map.add(ConfigConstants.MACHINELEARNINGTENSORFLOWGRU);
        map.add(ConfigConstants.MACHINELEARNINGPYTORCHMLP);
        map.add(ConfigConstants.MACHINELEARNINGPYTORCHCNN);
        map.add(ConfigConstants.MACHINELEARNINGPYTORCHCNN2);
        map.add(ConfigConstants.MACHINELEARNINGPYTORCHRNN);
        map.add(ConfigConstants.MACHINELEARNINGPYTORCHLSTM);
        map.add(ConfigConstants.MACHINELEARNINGPYTORCHGRU);
        return map;
    }

    public Map<Pair<String, String>, String> getMapPersistPred() {
        Map<Pair <String, String>, String> map = new HashMap<>();
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.LIR), ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLIRPERSIST);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.MLP), ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWMLPPERSIST);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.RNN), ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWRNNPERSIST);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.LSTM), ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTMPERSIST);
        map.put(new ImmutablePair(MLConstants.TENSORFLOW, MLConstants.GRU), ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWGRUPERSIST);
        map.put(new ImmutablePair(MLConstants.PYTORCH, MLConstants.MLP), ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHMLPPERSIST);
        map.put(new ImmutablePair(MLConstants.PYTORCH, MLConstants.RNN), ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHRNNPERSIST);
        map.put(new ImmutablePair(MLConstants.PYTORCH, MLConstants.LSTM), ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHLSTMPERSIST);
        map.put(new ImmutablePair(MLConstants.PYTORCH, MLConstants.GRU), ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHGRUPERSIST);
        return map;
    }

}
