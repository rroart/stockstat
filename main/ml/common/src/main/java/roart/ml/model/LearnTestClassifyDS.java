package roart.ml.model;

import java.util.List;
import java.util.Map;

import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.common.MLMeta;
import roart.ml.common.MLClassifyModel;

public class LearnTestClassifyDS {
    public NeuralNetConfigs nnconfigs;
    public List<LearnClassify> learnTestMap;
    public int modelid;
    public int size;
    public int outcomes;
    public List<LearnClassify> classifyMap;
    public Map<Double, String> shortMap;
    public String path;
    public String filename;
    public NeuralNetCommand neuralnetcommand;
    public MLMeta mlmeta;
    public boolean classify;

}
