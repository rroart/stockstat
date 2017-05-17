package roart.ml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.db.DbSpark;
import roart.indicator.Indicator;
import roart.indicator.IndicatorMACD;
import roart.ml.MLModel;
import roart.ml.MLSparkLRModel;
import roart.ml.MLSparkMCPModel;
import roart.util.Constants;

public class MLSparkAccess extends MLAccess {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public MLSparkAccess() {
        findModels();	    
	}
    private void findModels() {
        models = new ArrayList<>();
        if (IndicatorMACD.wantDNN()) {
            MLModel model = new MLSparkMCPModel();
            models.add(model);
        }
        if (IndicatorMACD.wantL()) {
            MLModel model = new MLSparkLRModel();
            models.add(model);
        }
    }
    @Override
    public void learntest(Indicator indicator, Map<double[], Double> map, MLModel model, int size, String period,
            String mapname, int outcomes) {
        //List<MLModel> models = model.getModels();
        //for (MLModel modelInt : models) {
       DbSpark.learntest(map, model.getId(), size, period, mapname, outcomes);       
    //}
    }

    @Override
    public Double eval(int modelInt, String period, String mapname) {
        return DbSpark.eval(modelInt, period, mapname);
    }

    @Override
    public Map<String, Double[]> classify(Indicator indicator, Map<String, double[]> map, MLModel model, int size,
            String period, String mapname, int outcomes, Map<Double, String> shortMap) {
        Map<Integer, Map<String, Double[]>> retMap = new HashMap<>();
        //List<MLModel> models = getModels();
        //for (MLModel modelInt : models) {
         return DbSpark.classify(map, new Integer(model.getId()), size, period, mapname, outcomes, shortMap);
        //}
        //return retMap;
    }

    @Override
    public List<MLModel> getModels() {
        return models;
    }

    /*
    private Map<Integer, String> getModelsMap() {
        Map<Integer, String> models = new HashMap<>();
        if (IndicatorMACD.wantMCP()) {
            int model = IndicatorMACD.MULTILAYERPERCEPTRONCLASSIFIER;
            models.put(model, "MPC");
        }
        if (IndicatorMACD.wantLR()) {
            int model = IndicatorMACD.LOGISTICREGRESSION;
            models.put(model, "LR");
        }
        return models;
    }
*/
    
 }

