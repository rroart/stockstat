package roart.ml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.aggregate.Aggregator;
import roart.config.ConfigConstants;
import roart.config.MyMyConfig;
import roart.model.LearnTestClassify;
import roart.util.EurekaUtil;

public class MLClassifyTensorflowAccess extends MLClassifyAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private MyMyConfig conf;

    public MLClassifyTensorflowAccess(MyMyConfig conf) {
        this.conf = conf;
        findModels();
    }

    private void findModels() {
        models = new ArrayList<>();
        if (conf.wantDNN()) {
            MLClassifyModel model = new MLClassifyTensorflowDNNModel();
            models.add(model);
        }
        if (conf.wantL()) {
            MLClassifyModel model = new MLClassifyTensorflowLModel();
            models.add(model);
        }	    
    }

    @Override
    public Double learntest(Aggregator indicator, Map<double[], Double> map, MLClassifyModel model, int size, String period, String mapname,
            int outcomes) {
        return learntestInner(map, size, period, mapname, outcomes, model);
    }

    @Override
    public List<MLClassifyModel> getModels() {
        return models;
    }

    private Double learntestInner(Map<double[], Double> map, int size, String period, String mapname, int outcomes,
            MLClassifyModel model) {
        List<List<Object>> listlist = new ArrayList<>();
        for (Entry<double[], Double> entry : map.entrySet()) {
            double[] key = entry.getKey();
            List<Object> list = new ArrayList<>();
            list.addAll(Arrays.asList(key));
            list.add(entry.getValue());
            listlist.add(list);
        }
        int i = 0;
        Object[][] objobj = new Object[map.size()][];
        Object[] cat = new Object[map.size()];
        for (Entry<double[], Double> entry : map.entrySet()) {
            double[] key = entry.getKey();
            Object[] obj = new Object[key.length/* + 1*/];
            for (int j = 0; j < key.length; j ++) {
                obj[j] = key[j];
            }
            cat[i] = entry.getValue();
            objobj[i++] = obj;
        }
        LearnTestClassify param = new LearnTestClassify();
        param.array = objobj;
        param.cat = cat;
        param.listlist = listlist;
        param.modelInt = model.getId();
        param.size = size;
        param.period = period;
        param.mapname = mapname;
        param.outcomes = outcomes;
        log.info("evalin {} {} {}", param.modelInt, period, mapname);
        LearnTestClassify test = EurekaUtil.sendMe(LearnTestClassify.class, param, "http://localhost:8000/learntest");
        return test.prob;
    }

    @Override
    public Double eval(int modelInt, String period, String mapname) {
        LearnTestClassify param = new LearnTestClassify();
        param.modelInt = modelInt;
        param.period = period;
        param.mapname = mapname;
        log.info("evalout {} {} {}", modelInt, period, mapname);
        LearnTestClassify test = EurekaUtil.sendMe(LearnTestClassify.class, param, "http://localhost:8000/eval");
        return test.prob;
    }

    @Override
    public Map<String, Double[]> classify(Aggregator indicator, Map<String, double[]> map, MLClassifyModel model, int size,
            String period, String mapname, int outcomes, Map<Double, String> shortMap) {
        Map<Integer, Map<String, Double[]>> retMap = new HashMap<>();
        if (map.isEmpty()) {
            return new HashMap<>();
        }
        return classifyInner(map, model, size, period, mapname, outcomes);
    }

    private Map<String, Double[]> classifyInner(Map<String, double[]> map, MLClassifyModel model, int size, String period,
            String mapname, int outcomes) {
        List<String> retList = new ArrayList<>();
        LearnTestClassify param = new LearnTestClassify();
        int i = 0;
        Object[][] objobj = new Object[map.size()][];
        for (Entry<String, double[]> entry : map.entrySet()) {
            double[] value = entry.getValue();
            Object[] obj = new Object[value.length/* + 1*/];
            for (int j = 0; j < value.length; j ++) {
                obj[j] = value[j];
            }
            objobj[i++] = obj;
            retList.add(entry.getKey());
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
        LearnTestClassify ret = EurekaUtil.sendMe(LearnTestClassify.class, param, "http://localhost:8000/classify");
        Object[] cat = ret.cat;
        Map<String, Double[]> retMap = new HashMap<>();
        for (int j = 0; j < retList.size(); j ++) {
            Double acat = Double.valueOf((Integer) cat[j]);
            retMap.put(retList.get(j), new Double[]{acat});
        }
        return retMap;
    }

    @Override
    public String getName() {
        return ConfigConstants.TENSORFLOW;
    }

}

