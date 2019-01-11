package roart.ml.model;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.pipeline.common.predictor.Predictor;

public abstract class MLPredictAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    protected List<MLPredictModel> models;
    
    public abstract Double[] predictone(Predictor predictor, Double[] list, MLPredictModel model, int size, String period, int outcomes, int windowsize, int horizon, int epochs);

    public abstract Double eval(int modelInt, String period, String mapname);

    public abstract Map<String, Double[]> predict(Predictor predictor, Map<String, Double[]> map, MLPredictModel model, int size, String period, int outcomes, int windowsize, int horizon, int epochs);

    public abstract List<MLPredictModel> getModels();

}

