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
import roart.model.LearnTestClassifyResult;
import roart.util.EurekaUtil;

public class MLClassifyTensorflowAccess extends MLClassifyAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private MyMyConfig conf;

    private String tensorflowServer;

    public MLClassifyTensorflowAccess(MyMyConfig conf) {
        this.conf = conf;
        findModels();
        tensorflowServer = conf.getTensorflowServer();
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
        // not used?
        List<List<Object>> listlist = getListList(map);
        Object[][] objobj = new Object[map.size()][];
        Object[] cat = new Object[map.size()];
        getTrainingSet(map, objobj, cat);
        LearnTestClassify param = new LearnTestClassify();
        param.setTrainingarray(objobj);
        param.setTrainingcatarray(cat);
        param.setModelInt(model.getId());
        param.setSize(size);
        param.setPeriod(period);
        param.setMapname(mapname);
        param.setOutcomes(outcomes);
        log.info("evalin {} {} {}", param.getModelInt(), period, mapname);
        LearnTestClassify test = EurekaUtil.sendMe(LearnTestClassify.class, param, tensorflowServer + "/learntest");
        return test.getAccuracy();
    }

    private void getTrainingSet(Map<double[], Double> map, Object[][] objobj, Object[] cat) {
        int i = 0;
        for (Entry<double[], Double> entry : map.entrySet()) {
            double[] key = entry.getKey();
            Object[] obj = new Object[key.length/* + 1*/];
            for (int j = 0; j < key.length; j ++) {
                obj[j] = key[j];
            }
            cat[i] = entry.getValue();
            objobj[i++] = obj;
        }
    }

    private List<List<Object>> getListList(Map<double[], Double> map) {
        List<List<Object>> listlist = new ArrayList<>();
        for (Entry<double[], Double> entry : map.entrySet()) {
            double[] key = entry.getKey();
            List<Object> list = new ArrayList<>();
            list.addAll(Arrays.asList(key));
            list.add(entry.getValue());
            listlist.add(list);
        }
        return listlist;
    }

    @Override
    public Double eval(int modelInt, String period, String mapname) {
        LearnTestClassify param = new LearnTestClassify();
        param.setModelInt(modelInt);
        param.setPeriod(period);
        param.setMapname(mapname);
        log.info("evalout {} {} {}", modelInt, period, mapname);
        LearnTestClassify test = EurekaUtil.sendMe(LearnTestClassify.class, param, tensorflowServer + "/eval");
        return test.getAccuracy();
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
        LearnTestClassify param = new LearnTestClassify();
        List<String> retList = new ArrayList<>();
        Object[][] objobj = new Object[map.size()][];
        getClassifyArray(map, retList, objobj);
        param.setClassifyarray(objobj);
        param.setModelInt(model.getId());
        param.setSize(size);
        param.setPeriod(period);
        param.setMapname(mapname);
        param.setOutcomes(outcomes);
        for(Object[] obj : objobj) {
            log.info("inner {}", Arrays.asList(obj));
        }
        LearnTestClassify ret = null;
        try {
            ret = EurekaUtil.sendMe(LearnTestClassify.class, param, tensorflowServer + "/classify");
        } catch (Exception e) {
            log.error("Exception", e);
        }
        Map<String, Double[]> retMap = getCatMap(retList, ret);
        return retMap;
    }

    private Map<String, Double[]> getCatMap(List<String> retList, LearnTestClassify ret) {
        Object[] cat = ret.getClassifycatarray();
        Map<String, Double[]> retMap = new HashMap<>();
        for (int j = 0; j < retList.size(); j ++) {
            Double acat = Double.valueOf((Integer) cat[j]);
            retMap.put(retList.get(j), new Double[]{acat});
        }
        return retMap;
    }

    private void getClassifyArray(Map<String, double[]> map, List<String> retList, Object[][] objobj) {
        int i = 0;
        for (Entry<String, double[]> entry : map.entrySet()) {
            double[] value = entry.getValue();
            Object[] obj = new Object[value.length/* + 1*/];
            for (int j = 0; j < value.length; j ++) {
                obj[j] = value[j];
            }
            objobj[i++] = obj;
            retList.add(entry.getKey());
        }
    }

    @Override
    public String getName() {
        return ConfigConstants.TENSORFLOW;
    }

    @Override
    public LearnTestClassifyResult learntestclassify(Aggregator indicator, Map<double[], Double> map,
            MLClassifyModel model, int size, String period, String mapname, int outcomes, Map<String, double[]> map2,
            Map<Double, String> shortMap) {
        LearnTestClassifyResult result = new LearnTestClassifyResult();
        if (map2 == null) {
            result.setCatMap(new HashMap<>());
            return result;
        }
        Object[][] trainingArray = new Object[map.size()][];
        Object[] trainingCatArray = new Object[map.size()];
        getTrainingSet(map, trainingArray, trainingCatArray);
        LearnTestClassify param = new LearnTestClassify();
        param.setTrainingarray(trainingArray);
        param.setTrainingcatarray(trainingCatArray);
        param.setModelInt(model.getId());
        param.setSize(size);
        param.setPeriod(period);
        param.setMapname(mapname);
        param.setOutcomes(outcomes);
        List<String> retList = new ArrayList<>();
        Object[][] classifyArray = new Object[map2.size()][];
        getClassifyArray(map2, retList, classifyArray);
        param.setClassifyarray(classifyArray);
        for(Object[] obj : classifyArray) {
            log.info("inner {}", Arrays.asList(obj));
        }
        LearnTestClassify ret = null;
        try {
            ret = EurekaUtil.sendMe(LearnTestClassify.class, param, tensorflowServer + "/learntestclassify");
        } catch (Exception e) {
            log.error("Exception", e);
        }
        result.setAccuracy(ret.getAccuracy());
        Map<String, Double[]> retMap = getCatMap(retList, ret);
        result.setCatMap(retMap);
        return result;
    }

}

