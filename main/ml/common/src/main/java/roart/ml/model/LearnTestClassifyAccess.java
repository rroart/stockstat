package roart.ml.model;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Triple;

import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.common.MLMeta;
import roart.ml.common.MLClassifyModel;

public class LearnTestClassifyAccess {
    public NeuralNetConfigs nnconfigs;
    public List<Triple<String, Object, Double>> learnTestMap;
    public MLClassifyModel model;
    public int size;
    public int outcomes;
    public List<Triple<String, Object, Double>> classifyMap;
    public Map<Double, String> shortMap;
    public String path;
    public String filename;
    public NeuralNetCommand neuralnetcommand;
    public MLMeta mlmeta;
    public boolean classify;

}
