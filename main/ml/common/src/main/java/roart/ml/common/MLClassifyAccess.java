package roart.ml.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.model.LearnClassify;
import roart.ml.model.LearnTestClassifyResult;
import roart.pipeline.common.aggregate.Aggregator;

public abstract class MLClassifyAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static final Integer LEFT = 0;
    public static final Integer MIDDLE = 1;
    public static final Integer RIGHT = 2;
    
    protected List<MLClassifyModel> models;
    
    public abstract Double learntest(NeuralNetConfigs nnconfigs, Aggregator indicator, List<LearnClassify> map, MLClassifyModel model, int size, int outcomes, String filename);

    public abstract Double eval(int modelInt);

    public abstract Map<String, Double[]> classify(Aggregator indicator, List<LearnClassify> map, MLClassifyModel model, int size, int outcomes, Map<Double, String> shortMap);

    public abstract LearnTestClassifyResult learntestclassify(NeuralNetConfigs nnconfigs, Aggregator indicator, List<LearnClassify> learnTestMap, MLClassifyModel model, int size, int outcomes, List<LearnClassify> classifyMap, Map<Double, String> shortMap, String path, String filename, NeuralNetCommand neuralnetcommand, MLMeta mlmeta, boolean classify);

    public abstract List<MLClassifyModel> getModels();

    public abstract String getName();
    
    public abstract void clean();

    public abstract String getShortName();
    
    public abstract LearnTestClassifyResult dataset(NeuralNetConfigs nnconfigs, MLClassifyModel model, NeuralNetCommand neuralnetcommand, MLMeta mlmeta, String dataset);

    public List<MLClassifyModel> getModels(String model) {
        return new ArrayList<>();
    }
}

