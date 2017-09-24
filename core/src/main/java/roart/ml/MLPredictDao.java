package roart.ml;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;

import roart.config.ConfigConstantMaps;
import roart.config.ConfigConstants;
import roart.config.MyMyConfig;
import roart.model.LearnTestPredict;
import roart.predictor.Predictor;
import roart.predictor.PredictorLSTM;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MLPredictDao {
    private static Logger log = LoggerFactory.getLogger(MLPredictDao.class);

    private MLPredictAccess access = null;

    public MLPredictDao(String instance, MyMyConfig conf) {
        instance(instance, conf);
    }

    private void instance(String type, MyMyConfig conf) {
        System.out.println("instance " + type);
        log.info("instance " + type);
        if (type == null) {
            return;
        }
        // TODO temp fix
        if (true || access == null) {
            if (type.equals(ConfigConstants.SPARK)) {
                access = new MLPredictSparkAccess(conf);
                //new MlSpark();
            }
            if (type.equals(ConfigConstants.TENSORFLOW)) {
                access = new MLPredictTensorflowAccess(conf);
            }
        }
    }

    public LearnTestPredict learntestpredict(Predictor predictor, Double[] list, List<Double> next, Map<double[], Double> map, MLPredictModel modeln, int size, String period, String mapname, int outcomes, Map<MLPredictModel, Long> mapTime, int windowsize, int horizon, int epochs) {
        LearnTestPredict predict = null;
        for (MLPredictModel model : getModels()) {
            long time1 = System.currentTimeMillis();
            predict = access.learntestpredict(predictor, list, next, map, model, size, period, mapname, outcomes, windowsize, horizon, epochs);
            long time = (System.currentTimeMillis() - time1);
            log.info("time " + model + " " + period + " " + mapname + " " + time + " " + predict.predicted);
            PredictorLSTM.mapAdder(mapTime, model, time);
            return predict;
        }
        return predict;
    }

    // TODO delete?
    
    @Deprecated
    public Double eval(int modelInt, String period, String mapname) {
        return access.eval(modelInt, period, mapname);
    }

    public Map<String, Double[]> predict(Predictor indicator, Map<String, double[]> map, MLPredictModel model, int size, String period, String mapname, int outcomes, Map<Double, String> shortMap, Map<MLPredictModel, Long> mapTime) {
        //Map<MLModel, Map<String, Double[]>> result = new HashMap<>();
        //for (MLModel model : getModels()) {
            long time1 = System.currentTimeMillis();
            Map<String, Double[]> resultAccess = access.predict(indicator, map, model, size, period, mapname, outcomes, shortMap);
            long time = (System.currentTimeMillis() - time1);
            log.info("time " + model + " " + period + " " + mapname + " " + time);
            PredictorLSTM.mapAdder(mapTime, model, time);
            //result.put(model, resultAccess);
        //}
        return resultAccess;
    }

    public int getSizes(Predictor indicator) {
        int size = 0;

        for (MLPredictModel model : getModels()) {
            size += model.getSizes(indicator);
        }
        return size;
    }

    //public int addTitles(Object[] objs, int retindex, String title, String key, String subType, List<Integer> typeList, Map<Integer, String> mapTypes, MLDao dao) {
    public int addTitles(Object[] objs, int retindex, Predictor indicator, String title, String key, String subType) {
        for (MLPredictModel model : getModels()) {
            retindex = model.addTitles(objs, retindex, indicator, title, key, subType, null, null, this);
        }
        return retindex;
    }

    public List<MLPredictModel> getModels() {
        return access.getModels();
    }

    public int addResults(Object[] objs, int retindex, String id, MLPredictModel model, Predictor indicator, Map<String, LearnTestPredict> mapResult, Map<Double, String> labelMapShort) {
        //for (MLModel model : getModels()) {
            retindex = model.addResults(objs, retindex, id, model, indicator, mapResult, labelMapShort);
        //}
            return retindex;
    }

}
