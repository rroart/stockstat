package roart.ml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.config.MyMyConfig;
import roart.model.LearnTestPredict;
import roart.predictor.Predictor;
import roart.util.EurekaUtil;

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
    public LearnTestPredict learntestpredict(Predictor predictor, Double[] list, List<Double> next, Map<double[], Double> map, MLPredictModel model, int size, String period, String mapname,
            int outcomes, int windowsize, int horizon, int epochs) {
        return learntestInner(list, next, map, size, period, mapname, outcomes, model, horizon, epochs, epochs);
    }

    @Override
    public List<MLPredictModel> getModels() {
        return models;
    }

    private LearnTestPredict learntestInner(Double[] list, List<Double> next, Map<double[], Double> map, int size, String period, String mapname, int outcomes,
            MLPredictModel model, int windowsize,int horizon, int epochs) {
        LearnTestPredict param = new LearnTestPredict();
        param.modelInt = model.getId();
        param.size = size;
        param.period = period;
        param.mapname = mapname;
        param.outcomes = outcomes;
        param.array = list;
        param.windowsize = windowsize;
        param.horizon = horizon;
        param.epochs = epochs;
        log.info("evalin {} {} {}", param.modelInt, period, mapname);
        return EurekaUtil.sendMe(LearnTestPredict.class, param, tensorflowServer + "/learntestpredict");
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
    public Map<String, Double[]> predict(Predictor indicator, Map<String, double[]> map, MLPredictModel model, int size,
            String period, String mapname, int outcomes, Map<Double, String> shortMap) {
        if (map.isEmpty()) {
            return new HashMap<>();
        }
        return classifyInner(map, model, size, period, mapname, outcomes);
    }

    private Map<String, Double[]> classifyInner(Map<String, double[]> map, MLPredictModel model, int size, String period,
            String mapname, int outcomes) {
        List<String> retList = new ArrayList<>();
        LearnTestPredict param = new LearnTestPredict();
        int i = 0;
        Object[][] objobj = new Object[map.size()][];
        for (Entry<String, double[]> entry : map.entrySet()) {
            String key = entry.getKey();
            double[] value = entry.getValue();
            Object[] obj = new Object[value.length/* + 1*/];
            for (int j = 0; j < value.length; j ++) {
                obj[j] = value[j];
            }
            objobj[i++] = obj;
            retList.add(key);
        }
        param.array = objobj;
        param.modelInt = model.getId();
        param.size = size;
        param.period = period;
        param.mapname = mapname;
        param.outcomes = outcomes;
        for(Object[] obj : objobj) {
            log.info("inner {}", Arrays.asList(obj));
        }
        System.out.println("NOTHERE0");
        LearnTestPredict ret = EurekaUtil.sendMe(LearnTestPredict.class, param, tensorflowServer + "/classify");
        Object[] cat = ret.cat;
        Map<String, Double[]> retMap = new HashMap<>();
        for (int j = 0; j < retList.size(); j ++) {
            Double acat = Double.valueOf((Integer) cat[j]);
            retMap.put(retList.get(j), new Double[]{acat});
        }
        return retMap;
    }

}

