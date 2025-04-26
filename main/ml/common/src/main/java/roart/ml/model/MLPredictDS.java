package roart.ml.model;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.ml.NeuralNetConfigs;
import roart.pipeline.common.predictor.AbstractPredictor;

public abstract class MLPredictDS {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    protected List<MLPredictModel> models;
    
    public abstract LearnTestPredictResult predictone(NeuralNetConfigs nnconfigs, AbstractPredictor predictor, Double[] list, MLPredictModel model, int size, String period, int outcomes);

    public abstract Double eval(int modelInt, String period, String mapname);

    public abstract LearnTestPredictResult predict(NeuralNetConfigs nnconfigs, AbstractPredictor predictor, Map<String, Double[]> map, MLPredictModel model, int size, String period, int outcomes);

    public abstract List<MLPredictModel> getModels();

}

