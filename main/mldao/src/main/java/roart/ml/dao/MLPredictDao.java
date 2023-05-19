package roart.ml.dao;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;

import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.model.LearnTestPredictResult;
import roart.ml.model.MLPredictAccess;
import roart.ml.model.MLPredictModel;
import roart.ml.spark.MLPredictSparkAccess;
import roart.ml.tensorflow.MLPredictTensorflowAccess;
import roart.pipeline.common.predictor.AbstractPredictor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class MLPredictDao {
    private static Logger log = LoggerFactory.getLogger(MLPredictDao.class);

    private MLPredictAccess access = null;

    public MLPredictDao(String instance, IclijConfig conf) {
        instance(instance, conf);
    }

    private void instance(String type, IclijConfig conf) {
        log.info("instance {}", type);
        if (type == null) {
            return;
        }
        // temp fix
        if (true || access == null) {
            if (type.equals(MLConstants.SPARK)) {
                access = new MLPredictSparkAccess(conf);
            }
            if (type.equals(MLConstants.TENSORFLOW)) {
                access = new MLPredictTensorflowAccess(conf);
            }
        }
    }

    public LearnTestPredictResult predictone(NeuralNetConfigs nnconfigs, AbstractPredictor predictor, Double[] list, MLPredictModel modeln, int size, String period, int outcomes, Map<MLPredictModel, Long> mapTime) {
        LearnTestPredictResult predict = null;
        for (MLPredictModel model : getModels()) {
            long time1 = System.currentTimeMillis();
            predict = access.predictone(nnconfigs, predictor, list, model, size, period, outcomes);
            long time = (System.currentTimeMillis() - time1);
            log.info("time {} {} {} {}", model, period, time, predict);
            MLPredictModel.mapAdder(mapTime, model, time);
            return predict;
        }
        return null;
    }

    // delete?

    @Deprecated
    public Double eval(int modelInt, String period, String mapname) {
        return access.eval(modelInt, period, mapname);
    }

    public LearnTestPredictResult predict(AbstractPredictor indicator, NeuralNetConfigs nnconfigs, Map<String, Double[]> map, MLPredictModel model, int size, String period, int outcomes, Map<MLPredictModel, Long> mapTime) {
        long time1 = System.currentTimeMillis();
        LearnTestPredictResult resultAccess = access.predict(nnconfigs, indicator, map, model, size, period, outcomes);
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

    @Deprecated
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
