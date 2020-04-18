package roart.common.ml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;

public class MLMapsML extends MLMaps {

    @Override
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

    @Override
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

    @Override
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

}
