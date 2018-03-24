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

    public Double[] predictone(Predictor predictor, Double[] list, MLPredictModel modeln, int size, String period, int outcomes, Map<MLPredictModel, Long> mapTime, int windowsize, int horizon, int epochs) {
        Double[] predict = null;
        for (MLPredictModel model : getModels()) {
            long time1 = System.currentTimeMillis();
            predict = access.predictone(predictor, list, model, size, period, outcomes, windowsize, horizon, epochs);
            long time = (System.currentTimeMillis() - time1);
            log.info("time {} {} {} {}", model, period, time, predict);
            PredictorLSTM.mapAdder(mapTime, model, time);
            return predict;
        }
        return null;
    }

    // TODO delete?

    @Deprecated
    public Double eval(int modelInt, String period, String mapname) {
        return access.eval(modelInt, period, mapname);
    }

    public Map<String, Double[]> predict(Predictor indicator, Map<String, Double[]> map, MLPredictModel model, int size, String period, int outcomes, Map<MLPredictModel, Long> mapTime, int windowsize, int horizon, int epochs) {
        long time1 = System.currentTimeMillis();
        Map<String, Double[]> resultAccess = access.predict(indicator, map, model, size, period, outcomes, windowsize, horizon, epochs);
        long time = (System.currentTimeMillis() - time1);
        log.info("time {} {} {}", model, period, time);
        PredictorLSTM.mapAdder(mapTime, model, time);
        return resultAccess;
    }

    public int getSizes(Predictor indicator) {
        int size = 0;

        for (MLPredictModel model : getModels()) {
            size += model.getSizes(indicator);
        }
        return size;
    }

    public int addTitles(Object[] objs, int retindex, Predictor indicator, String title, String key, String subType) {
        for (MLPredictModel model : getModels()) {
            retindex = model.addTitles(objs, retindex, indicator, title, key, subType, null, null, this);
        }
        return retindex;
    }

    public List<MLPredictModel> getModels() {
        return access.getModels();
    }

    public int addResults(Object[] objs, int retindex, String id, MLPredictModel model, Predictor indicator, Map<String, Double[]> mapResult, Map<Double, String> labelMapShort) {
         retindex = model.addResults(objs, retindex, id, model, indicator, mapResult, labelMapShort);
         return retindex;
    }

}
