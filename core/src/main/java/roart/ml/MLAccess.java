package roart.ml;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.indicator.Indicator;
import roart.ml.MLModel;

public abstract class MLAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public abstract void learntest(Indicator indicator, Map<double[], Double> map, MLModel model, int size, String period, String mapname, int outcomes);

    public abstract Double eval(int modelInt, String period, String mapname);

    public abstract Map<String, Double[]> classify(Indicator indicator, Map<String, double[]> map, MLModel model, int size, String period, String mapname, int outcomes, Map<Double, String> shortMap);

    public abstract List<MLModel> getModels();
        
}

