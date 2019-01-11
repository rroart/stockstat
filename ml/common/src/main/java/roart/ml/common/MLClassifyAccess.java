package roart.ml.common;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.ml.NNConfigs;
import roart.ml.model.LearnTestClassifyResult;
import roart.pipeline.common.aggregate.Aggregator;

public abstract class MLClassifyAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    protected List<MLClassifyModel> models;
    
    public abstract Double learntest(NNConfigs nnconfigs, Aggregator indicator, Map<double[], Double> map, MLClassifyModel model, int size, String period, String mapname, int outcomes);

    public abstract Double eval(int modelInt, String period, String mapname);

    public abstract Map<String, Double[]> classify(Aggregator indicator, Map<String, double[]> map, MLClassifyModel model, int size, String period, String mapname, int outcomes, Map<Double, String> shortMap);

    public abstract LearnTestClassifyResult learntestclassify(NNConfigs nnconfigs, Aggregator indicator, Map<double[], Double> map, MLClassifyModel model, int size, String period, String mapname, int outcomes, Map<String, double[]> map2, Map<Double, String> shortMap);

    public abstract List<MLClassifyModel> getModels();

    public abstract String getName();
    
    public abstract void clean();
}

