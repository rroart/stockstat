package roart.ml.dao;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;

import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.ml.model.MLPredictAccess;
import roart.ml.model.MLPredictModel;
import roart.ml.spark.MLPredictSparkAccess;
import roart.ml.tensorflow.MLPredictTensorflowAccess;
import roart.pipeline.common.predictor.AbstractPredictor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MLPredictDao {
    private static Logger log = LoggerFactory.getLogger(MLPredictDao.class);

    private MLPredictAccess access = null;

    public MLPredictDao(String instance, MyMyConfig conf) {
        instance(instance, conf);
    }

    private void instance(String type, MyMyConfig conf) {
        log.info("instance {}", type);
        if (type == null) {
            return;
        }
        // TODO temp fix
        if (true || access == null) {
            if (type.equals(ConfigConstants.SPARK)) {
                access = new MLPredictSparkAccess(conf);
            }
            if (type.equals(ConfigConstants.TENSORFLOW)) {
                access = new MLPredictTensorflowAccess(conf);
            }
        }
    }

    public Double[] predictone(AbstractPredictor predictor, Double[] list, MLPredictModel modeln, int size, String period, int outcomes, Map<MLPredictModel, Long> mapTime, int windowsize, int horizon, int epochs) {
        Double[] predict = null;
        for (MLPredictModel model : getModels()) {
            long time1 = System.currentTimeMillis();
            predict = access.predictone(predictor, list, model, size, period, outcomes, windowsize, horizon, epochs);
            long time = (System.currentTimeMillis() - time1);
            log.info("time {} {} {} {}", model, period, time, predict);
            MLPredictModel.mapAdder(mapTime, model, time);
            return predict;
        }
        return null;
    }

    // TODO delete?

    @Deprecated
    public Double eval(int modelInt, String period, String mapname) {
        return access.eval(modelInt, period, mapname);
    }

    public Map<String, Double[]> predict(AbstractPredictor indicator, Map<String, Double[]> map, MLPredictModel model, int size, String period, int outcomes, Map<MLPredictModel, Long> mapTime, int windowsize, int horizon, int epochs) {
        long time1 = System.currentTimeMillis();
        Map<String, Double[]> resultAccess = access.predict(indicator, map, model, size, period, outcomes, windowsize, horizon, epochs);
        long time = (System.currentTimeMillis() - time1);
        log.info("time {} {} {}", model, period, time);
        MLPredictModel.mapAdder(mapTime, model, time);
        return resultAccess;
    }

    public int getSizes(AbstractPredictor indicator) {
        int size = 0;

        for (MLPredictModel model : getModels()) {
            size += model.getSizes(indicator);
        }
        return size;
    }

    public int addTitles(Object[] objs, int retindex, AbstractPredictor indicator, String title, String key, String subType) {
        for (MLPredictModel model : getModels()) {
            //retindex = model.addTitles(objs, retindex, indicator, title, key, subType, null, null, this);
        }
        return retindex;
    }

    public List<MLPredictModel> getModels() {
        return access.getModels();
    }

    public int addResults(Object[] objs, int retindex, String id, MLPredictModel model, AbstractPredictor indicator, Map<String, Double[]> mapResult, Map<Double, String> labelMapShort) {
         retindex = model.addResults(objs, retindex, id, model, indicator, mapResult, labelMapShort);
         return retindex;
    }

}
