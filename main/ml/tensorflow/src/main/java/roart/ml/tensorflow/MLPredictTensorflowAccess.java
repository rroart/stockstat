package roart.ml.tensorflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.MyMyConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.eureka.util.EurekaUtil;
import roart.ml.model.LearnTestPredict;
import roart.ml.model.LearnTestPredictResult;
import roart.ml.model.MLPredictAccess;
import roart.ml.model.MLPredictModel;
import roart.pipeline.common.predictor.AbstractPredictor;

public class MLPredictTensorflowAccess extends MLPredictAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private MyMyConfig conf;

    private String tensorflowServer;
    
    public MLPredictTensorflowAccess(MyMyConfig conf) {
        this.conf = conf;
        findModels();
        tensorflowServer = conf.getTensorflowServer();
    }

    private void findModels() {
        models = new ArrayList<>();
        if (conf.wantLSTM()) {
            MLPredictModel model = new MLPredictTensorflowLSTMModel(conf);
            models.add(model);
        }
    }

    @Override
    public LearnTestPredictResult predictone(NeuralNetConfigs nnconfigs, AbstractPredictor predictor, Double[] list, MLPredictModel model, int size,
            String period, int outcomes) {
        return predictInner(nnconfigs, list, size, period, outcomes, model);
    }

    @Override
    public List<MLPredictModel> getModels() {
        return models;
    }

    private LearnTestPredictResult predictInner(NeuralNetConfigs nnconfigs, Double[] list, int size, String period,
            int outcomes, MLPredictModel model) {
        LearnTestPredict param = new LearnTestPredict();
        model.getModelAndSet(nnconfigs, param);
        param.modelInt = model.getId();
        param.size = size;
        param.period = period;
        param.outcomes = outcomes;
        param.array = list;
        log.info("evalin {} {}", param.modelInt, period);
        LearnTestPredictResult result = EurekaUtil.sendMe(LearnTestPredictResult.class, param, tensorflowServer + "/predictone");
        return result;
    }

    @Override
    public Double eval(int modelInt, String period, String mapname) {
        LearnTestPredict param = new LearnTestPredict();
        param.modelInt = modelInt;
        param.period = period;
        param.mapname = mapname;
        log.info("evalout {} {} {}", modelInt, period, mapname);
        System.out.println("NOTHERE0");
        LearnTestPredict test = EurekaUtil.sendMe(LearnTestPredict.class, param, tensorflowServer + "/eval");
        return test.prob;
    }

    @Override
    public LearnTestPredictResult predict(NeuralNetConfigs nnconfigs, AbstractPredictor predictor, Map<String, Double[]> map, MLPredictModel model,
            int size, String period, int outcomes) {
        if (map.isEmpty()) {
            return null;
        }
        return predictInner(nnconfigs, map, model, size, period);
    }

    private LearnTestPredictResult predictInner(NeuralNetConfigs nnconfigs, Map<String, Double[]> map, MLPredictModel model, int size,
            String period) {
        List<String> retList = new ArrayList<>();
        LearnTestPredict param = new LearnTestPredict();
        model.getModelAndSet(nnconfigs, param);
        int i = 0;
        List<Object[]> objobj = new ArrayList<>();
        for (Entry<String, Double[]> entry : map.entrySet()) {
            String key = entry.getKey();
            Double[] value = entry.getValue();
            Object[] obj = new Object[value.length/* + 1*/];
            for (int j = 0; j < value.length; j ++) {
                obj[j] = value[j];
            }
            objobj.add(obj);
            retList.add(key);
        }
        for(Object[] obj : objobj) {
            log.info("inner {}", Arrays.asList(obj));
        }
        param.arraylist = objobj;
        log.info("evalin {} {}", param.modelInt, size);
        LearnTestPredictResult ret = EurekaUtil.sendMe(LearnTestPredictResult.class, param, tensorflowServer + "/predict");
        List<Double[]> arraylist = ret.predictedlist;
        List<Double> accuracylist = ret.accuracylist;
        Map<String, Double[]> predictMap = new HashMap<>();
        Map<String, Double> accuracyMap = new HashMap<>();
        int count = 0;
        for (Entry<String, Double[]> entry : map.entrySet()) {
            String key = entry.getKey();
            Double accuracy = accuracylist.get(count);
            Double[] value = arraylist.get(count++);
            if (accuracy < 0) {
                continue;
            }
            accuracyMap.put(key, accuracy);
            predictMap.put(key, value);
        }
        ret.accuracyMap = accuracyMap;
        ret.predictMap = predictMap;
        return ret;
    }

}

