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
import roart.eureka.util.EurekaUtil;
import roart.ml.model.LearnTestPredict;
import roart.ml.model.MLPredictAccess;
import roart.ml.model.MLPredictModel;
import roart.pipeline.common.predictor.Predictor;

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
        if (conf.wantDNN()) {
            MLPredictModel model = new MLPredictTensorflowLSTMModel();
            models.add(model);
        }
    }

    @Override
    public Double[] predictone(Predictor predictor, Double[] list, MLPredictModel model, int size, String period,
            int outcomes, int windowsize, int horizon, int epochs) {
        return predictInner(list, size, period, outcomes, model, horizon, epochs, epochs);
    }

    @Override
    public List<MLPredictModel> getModels() {
        return models;
    }

    private Double[] predictInner(Double[] list, int size, String period, int outcomes,
            MLPredictModel model, int windowsize,int horizon, int epochs) {
        LearnTestPredict param = new LearnTestPredict();
        param.modelInt = model.getId();
        param.size = size;
        param.period = period;
        param.outcomes = outcomes;
        param.array = list;
        param.windowsize = windowsize;
        param.horizon = horizon;
        param.epochs = epochs;
        log.info("evalin {} {}", param.modelInt, period);
        LearnTestPredict result = EurekaUtil.sendMe(LearnTestPredict.class, param, tensorflowServer + "/predictone");
        return result.predicted;
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
    public Map<String, Double[]> predict(Predictor predictor, Map<String, Double[]> map, MLPredictModel model, int size,
            String period, int outcomes, int windowsize, int horizon, int epochs) {
        if (map.isEmpty()) {
            return new HashMap<>();
        }
        return predictInner(map, model, size, period, horizon, epochs, epochs);
    }

    private Map<String, Double[]> predictInner(Map<String, Double[]> map, MLPredictModel model, int size, String period,
            int windowsize, int horizon, int epochs) {
        List<String> retList = new ArrayList<>();
        LearnTestPredict param = new LearnTestPredict();
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
        param.windowsize = windowsize;
        param.horizon = horizon;
        param.epochs = epochs;
        log.info("evalin {} {}", param.modelInt, size);
        LearnTestPredict ret = EurekaUtil.sendMe(LearnTestPredict.class, param, tensorflowServer + "/predict");
        List<Double[]> arraylist = ret.predictedlist;
        Map<String, Double[]> retMap = new HashMap<>();
        int count = 0;
        for (Entry<String, Double[]> entry : map.entrySet()) {
            String key = entry.getKey();
            Double[] value = arraylist.get(count++);
            retMap.put(key, value);
        }
        return retMap;
    }

}

