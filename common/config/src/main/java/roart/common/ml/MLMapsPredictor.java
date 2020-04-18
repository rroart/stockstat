package roart.common.ml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;

public class MLMapsPredictor extends MLMapsML {

    @Override
    public Map<Pair<String, String>, String> getMap() {
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

    @Override
    public List<String> getOtherList() {
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

    @Override
    public Map<Pair<String, String>, String> getMapPersist() {
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
