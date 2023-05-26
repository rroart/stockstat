package roart.ml.spark;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.apache.commons.lang3.tuple.Triple;

import roart.ml.common.MLClassifyAccess;
import roart.common.webflux.WebFluxUtil;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.model.LearnTestClassifyResult;
import roart.pipeline.common.aggregate.Aggregator;
import roart.ml.common.MLClassifyModel;
import roart.common.config.MLConstants;
import roart.common.constants.EurekaConstants;
import roart.ml.common.MLClassifyModel;
import roart.ml.common.MLMeta;
import roart.ml.model.LearnTestClassifyAccess;

public class MLClassifySparkAccess extends MLClassifyAccess {

    private IclijConfig conf;

    public MLClassifySparkAccess(IclijConfig conf) {
        this.conf = conf;
        findModels();   
    }

    private void findModels() {
        models = new ArrayList<>();
        if (conf.wantSparkMLPC()) {
            MLClassifyModel model = new MLClassifySparkMLPCModel(conf);
            models.add(model);
        }
        if (conf.wantSparkLOR()) {
            MLClassifyModel model = new MLClassifySparkLORModel(conf);
            models.add(model);
        }
        if (conf.wantSparkOVR()) {
            MLClassifyModel model = new MLClassifySparkOVRModel(conf);
            models.add(model);
        }
        if (conf.wantSparkLSVC()) {
            MLClassifyModel model = new MLClassifySparkLSVCModel(conf);
            models.add(model);
        }
    }

    @Override
    public Double learntest(NeuralNetConfigs nnconfigs, Aggregator indicator, List<Triple<String, Object, Double>> map, MLClassifyModel model, int size, int outcomes, String filename) {
        return null;
    }

    @Override
    public Double eval(int modelInt) {
        return null;
    }

    @Override
    public Map<String, Double[]> classify(Aggregator indicator, List<Triple<String, Object, Double>> map, MLClassifyModel model, int size, int outcomes, Map<Double, String> shortMap) {
        return null;
    }

    @Override
    public LearnTestClassifyResult learntestclassify(NeuralNetConfigs nnconfigs, Aggregator indicator, List<Triple<String, Object, Double>> learnTestMap, MLClassifyModel model, int size, int outcomes, List<Triple<String, Object, Double>> classifyMap, Map<Double, String> shortMap, String path, String filename, NeuralNetCommand neuralnetcommand, MLMeta mlmeta, boolean classify) {
        LearnTestClassifyAccess param = new LearnTestClassifyAccess();
        param.nnconfigs = nnconfigs;
        param.learnTestMap = learnTestMap;
        param.model = model;
        param.size = size;
        param.outcomes = outcomes;
        param.classifyMap = classifyMap;
        param.shortMap = shortMap;
        param.neuralnetcommand = neuralnetcommand;
        param.mlmeta = mlmeta;
        param.classify = classify;
        param.model.setConf(null);
        LearnTestClassifyResult result = WebFluxUtil.sendMMe(LearnTestClassifyResult.class, param, EurekaConstants.LEARNTESTCLASSIFY);
        return null;
    }

    @Override
    public List<MLClassifyModel> getModels() {
        return models;
    }

    @Override
    public String getName() {
        return MLConstants.SPARK;
    }

    @Override
    public void clean() {
        LearnTestClassifyResult result = WebFluxUtil.sendMMe(LearnTestClassifyResult.class, null, EurekaConstants.CLEAN);
    }

    @Override
    public String getShortName() {
        return  MLConstants.SP;
    }

    @Override
    public LearnTestClassifyResult dataset(NeuralNetConfigs nnconfigs, MLClassifyModel model, NeuralNetCommand neuralnetcommand, MLMeta mlmeta, String dataset) {
        return null;
    }

    @Override
    public List<MLClassifyModel> getModels(String model) {
        return new ArrayList<>();
    }

}
