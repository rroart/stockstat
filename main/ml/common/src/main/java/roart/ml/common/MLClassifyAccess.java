package roart.ml.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.model.LearnTestClassifyResult;
import roart.pipeline.common.aggregate.Aggregator;

public abstract class MLClassifyAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    protected List<MLClassifyModel> models;
    
    public abstract Double learntest(NeuralNetConfigs nnconfigs, Aggregator indicator, List<Triple<String, Object, Double>> map, MLClassifyModel model, int size, int outcomes, String filename);

    public abstract Double eval(int modelInt);

    public abstract Map<String, Double[]> classify(Aggregator indicator, List<Triple<String, Object, Double>> map, MLClassifyModel model, int size, int outcomes, Map<Double, String> shortMap);

    public abstract LearnTestClassifyResult learntestclassify(NeuralNetConfigs nnconfigs, Aggregator indicator, List<Triple<String, Object, Double>> learnTestMap, MLClassifyModel model, int size, int outcomes, List<Triple<String, Object, Double>> classifyMap, Map<Double, String> shortMap, String path, String filename, NeuralNetCommand neuralnetcommand, MLMeta mlmeta, boolean classify);

    public abstract List<MLClassifyModel> getModels();

    public abstract String getName();
    
    public abstract void clean();

    public abstract String getShortName();
    
    public abstract LearnTestClassifyResult dataset(NeuralNetConfigs nnconfigs, MLClassifyModel model, NeuralNetCommand neuralnetcommand, MLMeta mlmeta, String dataset);

    public List<MLClassifyModel> getModels(String model) {
        return new ArrayList<>();
    }
}

