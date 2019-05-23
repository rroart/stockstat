package roart.ml.dao;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.common.MLClassifyAccess;
import roart.ml.common.MLClassifyModel;
import roart.ml.model.LearnTestClassifyResult;
import roart.ml.spark.MLClassifySparkAccess;
import roart.ml.tensorflow.MLClassifyTensorflowAccess;
import roart.pipeline.common.aggregate.Aggregator;

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
        // temp fix
        if (true || access == null) {
            if (type.equals(ConfigConstants.SPARK)) {
                access = new MLClassifySparkAccess(conf);
            }
            if (type.equals(ConfigConstants.TENSORFLOW)) {
                access = new MLClassifyTensorflowAccess(conf);
            }
        }
    }

    public LearnTestClassifyResult learntestclassify(NeuralNetConfigs nnconfigs, Aggregator indicator, Map<double[], Double> map, MLClassifyModel model, int size, String period, String mapname, int outcomes, Map<MLClassifyModel, Long> mapTime, Map<String, double[]> map2, Map<Double, String> shortMap) {
        long time1 = System.currentTimeMillis();
        LearnTestClassifyResult result = access.learntestclassify(nnconfigs, indicator, map, model, size, period, mapname, outcomes, map2, shortMap);
        long time = (System.currentTimeMillis() - time1);
        log.info("time {} {} {} {}", model, period, mapname, time);
        MLClassifyModel.mapAdder(mapTime, model, time);
        return result;
    }

    public Double learntest(NeuralNetConfigs nnconfigs, Aggregator indicator, Map<double[], Double> map, MLClassifyModel model, int size, String period, String mapname, int outcomes, Map<MLClassifyModel, Long> mapTime) {
        long time1 = System.currentTimeMillis();
        Double prob = access.learntest(nnconfigs, indicator, map, model, size, period, mapname, outcomes);
        long time = (System.currentTimeMillis() - time1);
        log.info("time {} {} {} {}", model, period, mapname, time);
        MLClassifyModel.mapAdder(mapTime, model, time);
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
        MLClassifyModel.mapAdder(mapTime, model, time);
        return resultAccess;
    }

    public int getSizes(Aggregator indicator) {
        int size = 0;

        for (MLClassifyModel model : getModels()) {
            size += model.getSizes(indicator);
        }
        return size;
    }

    public List<MLClassifyModel> getModels() {
        return access.getModels();
    }

    public String getName() {
        return access.getName();
    }
    
    public void clean() {
        access.clean();
    }
}
