package roart.ml;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;

import roart.config.ConfigConstants;
import roart.indicator.Indicator;
import roart.model.StockItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MLDao {
    private static Logger log = LoggerFactory.getLogger(MLDao.class);

    private MLAccess access = null;

    public MLDao(String instance) {
        instance(instance);
    }

    private void instance(String type) {
        System.out.println("instance " + type);
        log.info("instance " + type);
        if (type == null) {
            return;
        }
        // TODO temp fix
        if (true || access == null) {
            if (type.equals(ConfigConstants.SPARK)) {
                access = new MLSparkAccess();
                //new MlSpark();
            }
            if (type.equals(ConfigConstants.TENSORFLOW)) {
                access = new MLTensorflowAccess();
            }
        }
    }

    public void learntest(Indicator indicator, Map<double[], Double> map, MLModel modeln, int size, String period, String mapname, int outcomes) {
        for (MLModel model : getModels()) {
            long time1 = System.currentTimeMillis();
            access.learntest(indicator, map, model, size, period, mapname, outcomes);
            log.info("time " + model + " " + period + " " + mapname + " " + (System.currentTimeMillis() - time1));
        }
    }

    public Double eval(int modelInt, String period, String mapname) {
        return access.eval(modelInt, period, mapname);
    }

    public Map<String, Double[]> classify(Indicator indicator, Map<String, double[]> map, MLModel model, int size, String period, String mapname, int outcomes, Map<Double, String> shortMap) {
        //Map<MLModel, Map<String, Double[]>> result = new HashMap<>();
        //for (MLModel model : getModels()) {
            long time1 = System.currentTimeMillis();
            Map<String, Double[]> resultAccess = access.classify(indicator, map, model, size, period, mapname, outcomes, shortMap);
            log.info("time " + model + " " + period + " " + mapname + " " + (System.currentTimeMillis() - time1));
            //result.put(model, resultAccess);
        //}
        return resultAccess;
    }

    public int getSizes(Indicator indicator) {
        int size = 0;

        for (MLModel model : getModels()) {
            size += model.getSizes(indicator);
        }
        return size;
    }

    //public int addTitles(Object[] objs, int retindex, String title, String key, String subType, List<Integer> typeList, Map<Integer, String> mapTypes, MLDao dao) {
    public int addTitles(Object[] objs, int retindex, Indicator indicator, String title, String key, String subType) {
        for (MLModel model : getModels()) {
            retindex = model.addTitles(objs, retindex, indicator, title, key, subType, null, null, this);
        }
        return retindex;
    }

    public List<MLModel> getModels() {
        return access.getModels();
    }

    public int addResults(Object[] objs, int retindex, String id, MLModel model, Indicator indicator, Map<String, Map<String, Double[]>> mapResult, Map<Double, String> labelMapShort) {
        //for (MLModel model : getModels()) {
            retindex = model.addResults(objs, retindex, id, model, indicator, mapResult, labelMapShort);
        //}
            return retindex;
    }

}
