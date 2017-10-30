package roart.ml;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;

import roart.aggregate.Aggregator;
import roart.aggregate.MLMACD;
import roart.config.ConfigConstantMaps;
import roart.config.ConfigConstants;
import roart.config.MyMyConfig;
import roart.indicator.Indicator;
import roart.indicator.IndicatorMACD;
import roart.model.StockItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MLClassifyDao {
    private static Logger log = LoggerFactory.getLogger(MLClassifyDao.class);

    private MLClassifyAccess access = null;

    public MLClassifyDao(String instance, MyMyConfig conf) {
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
                access = new MLClassifySparkAccess(conf);
                //new MlSpark();
            }
            if (type.equals(ConfigConstants.TENSORFLOW)) {
                access = new MLClassifyTensorflowAccess(conf);
            }
        }
    }

    public Double learntest(Aggregator indicator, Map<double[], Double> map, MLClassifyModel model, int size, String period, String mapname, int outcomes, Map<MLClassifyModel, Long> mapTime) {
        Map<String, Double> probMap = new HashMap<>();
        int i = 0;
        //for (MLClassifyModel model : getModels()) {
            long time1 = System.currentTimeMillis();
            Double prob = access.learntest(indicator, map, model, size, period, mapname, outcomes);
            //probMap.put(model.getName(), prob);
            long time = (System.currentTimeMillis() - time1);
            log.info("time " + model + " " + period + " " + mapname + " " + time);
            MLMACD.mapAdder(mapTime, model, time);
        //}
        return prob;
    }

    @Deprecated
    public Double eval(int modelInt, String period, String mapname) {
        return access.eval(modelInt, period, mapname);
    }

    public Map<String, Double[]> classify(Aggregator indicator, Map<String, double[]> map, MLClassifyModel model, int size, String period, String mapname, int outcomes, Map<Double, String> shortMap, Map<MLClassifyModel, Long> mapTime) {
        //Map<MLModel, Map<String, Double[]>> result = new HashMap<>();
        //for (MLModel model : getModels()) {
            long time1 = System.currentTimeMillis();
            Map<String, Double[]> resultAccess = access.classify(indicator, map, model, size, period, mapname, outcomes, shortMap);
            long time = (System.currentTimeMillis() - time1);
            log.info("time " + model + " " + period + " " + mapname + " " + time);
            MLMACD.mapAdder(mapTime, model, time);
            //result.put(model, resultAccess);
        //}
        return resultAccess;
    }

    public int getSizes(Aggregator indicator) {
        int size = 0;

        for (MLClassifyModel model : getModels()) {
            size += model.getSizes(indicator);
        }
        return size;
    }

    @Deprecated
    //public int addTitles(Object[] objs, int retindex, String title, String key, String subType, List<Integer> typeList, Map<Integer, String> mapTypes, MLDao dao) {
    public int addTitles(Object[] objs, int retindex, Aggregator indicator, String title, String key, String subType) {
        for (MLClassifyModel model : getModels()) {
            retindex = model.addTitles(objs, retindex, indicator, title, key, subType, null, null, this);
        }
        return retindex;
    }

    public List<MLClassifyModel> getModels() {
        return access.getModels();
    }

    @Deprecated
    public int addResults(Object[] objs, int retindex, String id, MLClassifyModel model, Aggregator indicator, Map<String, Map<String, Double[]>> mapResult, Map<Double, String> labelMapShort) {
        //for (MLModel model : getModels()) {
            retindex = model.addResults(objs, retindex, id, model, indicator, mapResult, labelMapShort);
        //}
            return retindex;
    }

    public String getName() {
        return access.getName();
    }
}
