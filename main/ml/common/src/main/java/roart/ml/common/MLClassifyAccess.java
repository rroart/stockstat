package roart.ml.common;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.ml.NeuralNetConfigs;
import roart.ml.model.LearnTestClassifyResult;
import roart.pipeline.common.aggregate.Aggregator;

public abstract class MLClassifyAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    protected List<MLClassifyModel> models;
    
    public abstract Double learntest(NeuralNetConfigs nnconfigs, Aggregator indicator, Map<String, Pair<double[], Double>> map, MLClassifyModel model, int size, String period, String mapname, int outcomes);

    public abstract Double eval(int modelInt, String period, String mapname);

    public abstract Map<String, Double[]> classify(Aggregator indicator, Map<String, Pair<double[], Double>> map, MLClassifyModel model, int size, String period, String mapname, int outcomes, Map<Double, String> shortMap);

    public abstract LearnTestClassifyResult learntestclassify(NeuralNetConfigs nnconfigs, Aggregator indicator, Map<String, Pair<double[], Double>> map, MLClassifyModel model, int size, String period, String mapname, int outcomes, Map<String, Pair<double[], Double>> map2, Map<Double, String> shortMap);

    public abstract List<MLClassifyModel> getModels();

    public abstract String getName();
    
    public abstract void clean();
}

