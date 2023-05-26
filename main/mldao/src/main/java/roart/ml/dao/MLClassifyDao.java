package roart.ml.dao;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.common.MLClassifyAccess;
import roart.ml.common.MLClassifyModel;
import roart.ml.common.MLClassifyRandomAccess;
import roart.ml.common.MLMeta;
import roart.ml.gem.MLClassifyGemAccess;
import roart.ml.model.LearnTestClassifyResult;
import roart.ml.pytorch.MLClassifyPytorchAccess;
import roart.ml.spark.MLClassifySparkAccess;
import roart.ml.tensorflow.MLClassifyTensorflowAccess;
import roart.pipeline.common.aggregate.Aggregator;

public class MLClassifyDao {
    private static Logger log = LoggerFactory.getLogger(MLClassifyDao.class);

    private MLClassifyAccess access = null;

    public MLClassifyDao(String instance, IclijConfig conf) {
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
                access = new MLClassifySparkAccess(conf);
            }
            if (type.equals(MLConstants.TENSORFLOW)) {
                access = new MLClassifyTensorflowAccess(conf);
            }
            if (type.equals(MLConstants.PYTORCH)) {
                access = new MLClassifyPytorchAccess(conf);
            }
            if (type.equals(MLConstants.GEM)) {
                access = new MLClassifyGemAccess(conf);
            }
            if (type.equals(MLConstants.RANDOM)) {
                access = new MLClassifyRandomAccess(conf);
            }
        }
    }

    public LearnTestClassifyResult learntestclassify(NeuralNetConfigs nnconfigs, Aggregator indicator, List<Triple<String, Object, Double>> learnTestMap, MLClassifyModel model, int size, int outcomes, Map<MLClassifyModel, Long> mapTime, List<Triple<String, Object, Double>> classifyMap, Map<Double, String> shortMap, String path, String filename, NeuralNetCommand neuralnetcommand, MLMeta mlmeta, boolean classify) {
        long time1 = System.currentTimeMillis();
        LearnTestClassifyResult result = access.learntestclassify(nnconfigs, indicator, learnTestMap, model, size, outcomes, classifyMap, shortMap, path, filename, neuralnetcommand, mlmeta, classify);
        long time = (System.currentTimeMillis() - time1);
        log.info("time {} {}", model, time);
        MLClassifyModel.mapAdder(mapTime, model, time);
        return result;
    }

    // not used?
    public Double learntest(NeuralNetConfigs nnconfigs, Aggregator indicator, List<Triple<String, Object, Double>> map, MLClassifyModel model, int size, int outcomes, Map<MLClassifyModel, Long> mapTime, String filename) {
        long time1 = System.currentTimeMillis();
        Double prob = access.learntest(nnconfigs, indicator, map, model, size, outcomes, filename);
        long time = (System.currentTimeMillis() - time1);
        log.info("time {} {}", model, time);
        MLClassifyModel.mapAdder(mapTime, model, time);
        return prob;
    }

    @Deprecated
    public Double eval(int modelInt) {
        return access.eval(modelInt);
    }

    // not used?
    public Map<String, Double[]> classify(Aggregator indicator, List<Triple<String, Object, Double>> classifyMLMap, MLClassifyModel model, int size, int outcomes, Map<Double, String> shortMap, Map<MLClassifyModel, Long> mapTime) {
        long time1 = System.currentTimeMillis();
        Map<String, Double[]> resultAccess = access.classify(indicator, classifyMLMap, model, size, outcomes, shortMap);
        long time = (System.currentTimeMillis() - time1);
        log.info("time {} {}", model, time);
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

    public List<MLClassifyModel> getModels(String model) {
        return access.getModels(model);
    }

    public String getName() {
        return access.getName();
    }
    
    public String getShortName() {
        return access.getShortName();
    }
    
    public void clean() {
        access.clean();
    }

    public LearnTestClassifyResult dataset(NeuralNetConfigs nnconfigs,
            MLClassifyModel model, Map<MLClassifyModel, Long> mapTime, NeuralNetCommand neuralnetcommand, MLMeta mlmeta, String dataset) {
        long time1 = System.currentTimeMillis();
        LearnTestClassifyResult result = access.dataset(nnconfigs, model, neuralnetcommand, mlmeta, dataset);
        long time = (System.currentTimeMillis() - time1);
        log.info("time {} {}", model, time);
        MLClassifyModel.mapAdder(mapTime, model, time);
        return result;
    }
}
