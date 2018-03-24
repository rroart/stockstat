package roart.ml;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.aggregate.Aggregator;
import roart.aggregate.MLMACD;
import roart.config.ConfigConstants;
import roart.config.MyMyConfig;
import roart.model.LearnTestClassifyResult;

public class MLClassifyDao {
    private static Logger log = LoggerFactory.getLogger(MLClassifyDao.class);

    private MLClassifyAccess access = null;

    public MLClassifyDao(String instance, MyMyConfig conf) {
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
                access = new MLClassifySparkAccess(conf);
            }
            if (type.equals(ConfigConstants.TENSORFLOW)) {
                access = new MLClassifyTensorflowAccess(conf);
            }
        }
    }

    public LearnTestClassifyResult learntestclassify(Aggregator indicator, Map<double[], Double> map, MLClassifyModel model, int size, String period, String mapname, int outcomes, Map<MLClassifyModel, Long> mapTime, Map<String, double[]> map2, Map<Double, String> shortMap) {
        long time1 = System.currentTimeMillis();
        LearnTestClassifyResult result = access.learntestclassify(indicator, map, model, size, period, mapname, outcomes, map2, shortMap);
        long time = (System.currentTimeMillis() - time1);
        log.info("time {} {} {} {}", model, period, mapname, time);
        MLMACD.mapAdder(mapTime, model, time);
        return result;
    }

    public Double learntest(Aggregator indicator, Map<double[], Double> map, MLClassifyModel model, int size, String period, String mapname, int outcomes, Map<MLClassifyModel, Long> mapTime) {
        long time1 = System.currentTimeMillis();
        Double prob = access.learntest(indicator, map, model, size, period, mapname, outcomes);
        long time = (System.currentTimeMillis() - time1);
        log.info("time {} {} {} {}", model, period, mapname, time);
        MLMACD.mapAdder(mapTime, model, time);
        return prob;
    }

    @Deprecated
    public Double eval(int modelInt, String period, String mapname) {
        return access.eval(modelInt, period, mapname);
    }

    public Map<String, Double[]> classify(Aggregator indicator, Map<String, double[]> map, MLClassifyModel model, int size, String period, String mapname, int outcomes, Map<Double, String> shortMap, Map<MLClassifyModel, Long> mapTime) {
        long time1 = System.currentTimeMillis();
        Map<String, Double[]> resultAccess = access.classify(indicator, map, model, size, period, mapname, outcomes, shortMap);
        long time = (System.currentTimeMillis() - time1);
        log.info("time {} {} {} {}", model, period, mapname, time);
        MLMACD.mapAdder(mapTime, model, time);
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
        retindex = model.addResults(objs, retindex, id, model, indicator, mapResult, labelMapShort);
        return retindex;
    }

    public String getName() {
        return access.getName();
    }
}
