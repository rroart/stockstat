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
import roart.ml.common.MLClassifyDS;
import roart.ml.common.MLClassifyModel;
import roart.ml.common.MLClassifyRandomDS;
import roart.ml.common.MLMeta;
import roart.ml.gem.MLClassifyGemDS;
import roart.ml.model.LearnClassify;
import roart.ml.model.LearnTestClassifyResult;
import roart.ml.pytorch.MLClassifyPytorchDS;
import roart.ml.spark.MLClassifySparkDS;
import roart.ml.tensorflow.MLClassifyTensorflowDS;
import roart.pipeline.common.aggregate.Aggregator;

public class MLClassifyDao {
    private static Logger log = LoggerFactory.getLogger(MLClassifyDao.class);

    private MLClassifyDS ds = null;

    public MLClassifyDao(String instance, IclijConfig conf) {
        instance(instance, conf);
    }

    private void instance(String type, IclijConfig conf) {
        log.info("instance {}", type);
        if (type == null) {
            return;
        }
        //type = MLConstants.RANDOM;
        if (Boolean.TRUE.equals(conf.wantsMachineLearningRandom())) {
            type = MLConstants.RANDOM;
            log.info("Config for random");
        }
        if (true || ds == null) {
            if (type.equals(MLConstants.SPARK)) {
                ds = new MLClassifySparkDS(conf);
            }
            if (type.equals(MLConstants.TENSORFLOW)) {
                ds = new MLClassifyTensorflowDS(conf);
            }
            if (type.equals(MLConstants.PYTORCH)) {
                ds = new MLClassifyPytorchDS(conf);
            }
            if (type.equals(MLConstants.GEM)) {
                ds = new MLClassifyGemDS(conf);
            }
            if (type.equals(MLConstants.RANDOM)) {
                ds = new MLClassifyRandomDS(conf);
            }
        }
    }

    public LearnTestClassifyResult learntestclassify(NeuralNetConfigs nnconfigs, Aggregator indicator, List<LearnClassify> learnTestMap, MLClassifyModel model, int size, int outcomes, Map<MLClassifyModel, Long> mapTime, List<LearnClassify> classifyMap, Map<Double, String> shortMap, String path, String filename, NeuralNetCommand neuralnetcommand, MLMeta mlmeta, boolean classify) {
        long time1 = System.currentTimeMillis();
        LearnTestClassifyResult result = ds.learntestclassify(nnconfigs, indicator, learnTestMap, model, size, outcomes, classifyMap, shortMap, path, filename, neuralnetcommand, mlmeta, classify);
        long time = (System.currentTimeMillis() - time1);
        log.info("time {} {}", model, time);
        MLClassifyModel.mapAdder(mapTime, model, time);
        return result;
    }

    // not used?
    public Double learntest(NeuralNetConfigs nnconfigs, Aggregator indicator, List<LearnClassify> map, MLClassifyModel model, int size, int outcomes, Map<MLClassifyModel, Long> mapTime, String filename) {
        long time1 = System.currentTimeMillis();
        Double prob = ds.learntest(nnconfigs, indicator, map, model, size, outcomes, filename);
        long time = (System.currentTimeMillis() - time1);
        log.info("time {} {}", model, time);
        MLClassifyModel.mapAdder(mapTime, model, time);
        return prob;
    }

    @Deprecated
    public Double eval(int modelInt) {
        return ds.eval(modelInt);
    }

    // not used?
    public Map<String, Double[]> classify(Aggregator indicator, List<LearnClassify> classifyMLMap, MLClassifyModel model, int size, int outcomes, Map<Double, String> shortMap, Map<MLClassifyModel, Long> mapTime) {
        long time1 = System.currentTimeMillis();
        Map<String, Double[]> resultAccess = ds.classify(indicator, classifyMLMap, model, size, outcomes, shortMap);
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
        return ds.getModels();
    }

    public List<MLClassifyModel> getModels(String model) {
        return ds.getModels(model);
    }

    public String getName() {
        return ds.getName();
    }
    
    public String getShortName() {
        return ds.getShortName();
    }
    
    public void clean() {
        ds.clean();
    }

    public LearnTestClassifyResult dataset(NeuralNetConfigs nnconfigs,
            MLClassifyModel model, Map<MLClassifyModel, Long> mapTime, NeuralNetCommand neuralnetcommand, MLMeta mlmeta, String dataset) {
        long time1 = System.currentTimeMillis();
        LearnTestClassifyResult result = ds.dataset(nnconfigs, model, neuralnetcommand, mlmeta, dataset);
        long time = (System.currentTimeMillis() - time1);
        log.info("time {} {}", model, time);
        MLClassifyModel.mapAdder(mapTime, model, time);
        return result;
    }
}
