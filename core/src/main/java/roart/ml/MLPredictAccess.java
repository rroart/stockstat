package roart.ml;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.model.LearnTestPredict;
import roart.predictor.Predictor;

public abstract class MLPredictAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    protected List<MLPredictModel> models;
    
    public abstract LearnTestPredict learntestpredict(Predictor predictor, Double[] list, List<Double> next, Map<double[], Double> map, MLPredictModel model, int size, String period, String mapname, int outcomes, int windowsize, int horizon, int epochs);

    public abstract Double eval(int modelInt, String period, String mapname);

    public abstract Map<String, Double[]> predict(Predictor predictor, Map<String, double[]> map, MLPredictModel model, int size, String period, String mapname, int outcomes, Map<Double, String> shortMap);

    public abstract List<MLPredictModel> getModels();
        
}

