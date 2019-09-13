package roart.ml.gem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;
import roart.common.ml.GemEWCConfig;
import roart.common.ml.GemGEMConfig;
import roart.common.ml.GemIConfig;
import roart.common.ml.GemIcarlConfig;
import roart.common.ml.GemMMConfig;
import roart.common.ml.GemSConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.eureka.util.EurekaUtil;
import roart.ml.common.MLClassifyAccess;
import roart.ml.common.MLClassifyModel;
import roart.ml.model.LearnTestClassify;
import roart.ml.model.LearnTestClassifyResult;
import roart.pipeline.common.aggregate.Aggregator;

public class MLClassifyGemAccess extends MLClassifyAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private MyMyConfig conf;

    private String tensorflowServer;

    public MLClassifyGemAccess(MyMyConfig conf) {
        this.conf = conf;
        findModels();
        tensorflowServer = conf.getGEMServer();
    }

    private void findModels() {
        models = new ArrayList<>();
        if (conf.wantGemSingle()) {
            MLClassifyModel model = new MLClassifyGemSingleModel(conf);
            models.add(model);
        }
        if (conf.wantGemIndependent()) {
            MLClassifyModel model = new MLClassifyGemIndependentModel(conf);
            models.add(model);
        }
        if (conf.wantGemMultiModal()) {
            MLClassifyModel model = new MLClassifyGemMultimodalModel(conf);
            models.add(model);
        }
        if (conf.wantGemEWC()) {
            MLClassifyModel model = new MLClassifyGemEWCModel(conf);
            models.add(model);
        }
        if (conf.wantGemGEM()) {
            MLClassifyModel model = new MLClassifyGemGEMModel(conf);
            models.add(model);
        }
        if (conf.wantGemIcarl()) {
            MLClassifyModel model = new MLClassifyGemIcarlModel(conf);
            models.add(model);
        }
    }

    @Override
    public Double learntest(NeuralNetConfigs nnconfigs, Aggregator indicator, Map<String, Pair<double[], Double>> map, MLClassifyModel model, int size,
            int classes, String filename) {
        return learntestInner(nnconfigs, map, size, classes, model);
    }

    @Override
    public List<MLClassifyModel> getModels() {
        return models;
    }

    private Double learntestInner(NeuralNetConfigs nnconfigs, Map<String, Pair<double[], Double>> map, int size, int classes,
            MLClassifyModel model) {
        // not used?
        //List<List<Object>> listlist = getListList(map);
        Object[][] objobj = new Object[map.size()][];
        Object[] cat = new Object[map.size()];
        getTrainingSet(map, objobj, cat);
        LearnTestClassify param = new LearnTestClassify();
        //param.setTensorflowDNNConfig(nnconfigs.getTensorflowDNNConfig());
        //param.setTensorflowLConfig(nnconfigs.getTensorflowLConfig());
        param.setTrainingarray(objobj);
        param.setTrainingcatarray(cat);
        param.setModelInt(model.getId());
        param.setSize(size);
        param.setClasses(classes);
        log.info("evalin {} {} {}", param.getModelInt());
        LearnTestClassify test = EurekaUtil.sendMe(LearnTestClassify.class, param, tensorflowServer + "/learntest");
        return test.getAccuracy();
    }

    private void getTrainingSet(Map<String, Pair<double[], Double>> map, Object[][] objobj, Object[] cat) {
        int i = 0;
        for (Entry<String, Pair<double[], Double>> entry : map.entrySet()) {
            double[] key = entry.getValue().getLeft();
            Object[] obj = new Object[key.length/* + 1*/];
            for (int j = 0; j < key.length; j ++) {
                obj[j] = key[j];
            }
            Pair<double[], Double> pair = entry.getValue();
            cat[i] = pair.getRight();
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
    public Double eval(int modelInt) {
        LearnTestClassify param = new LearnTestClassify();
        param.setModelInt(modelInt);
        log.info("evalout {}", modelInt);
        LearnTestClassify test = EurekaUtil.sendMe(LearnTestClassify.class, param, tensorflowServer + "/eval");
        return test.getAccuracy();
    }

    @Override
    public Map<String, Double[]> classify(Aggregator indicator, Map<String, Pair<double[], Double>> map, MLClassifyModel model, int size,
            int classes, Map<Double, String> shortMap) {
        Map<Integer, Map<String, Double[]>> retMap = new HashMap<>();
        if (map.isEmpty()) {
            return new HashMap<>();
        }
        return classifyInner(map, model, size, classes);
    }

    private Map<String, Double[]> classifyInner(Map<String, Pair<double[], Double>> map, MLClassifyModel model, int size,
            int classes) {
        LearnTestClassify param = new LearnTestClassify();
        List<String> retList = new ArrayList<>();
        Object[][] objobj = new Object[map.size()][];
        getClassifyArray(map, retList, objobj);
        param.setClassifyarray(objobj);
        param.setModelInt(model.getId());
        param.setSize(size);
        param.setClasses(classes);
        for(Object[] obj : objobj) {
            log.info("inner {}", Arrays.asList(obj));
        }
        LearnTestClassify ret = null;
        try {
            ret = EurekaUtil.sendMe(LearnTestClassify.class, param, tensorflowServer + "/classify");
        } catch (Exception e) {
            log.error("Exception", e);
        }
        Map<String, Double[]> retMap = getCatMap(retList, map, ret);
        return retMap;
    }

    private Map<String, Double[]> getCatMap(List<String> retList, Map<String, Pair<double[], Double>> classifyMap, LearnTestClassify ret) {
        Object[] cat = ret.getClassifycatarray();
        Object[] prob = ret.getClassifyprobarray();
        Map<String, Double[]> retMap = new HashMap<>();
        for (int j = 0; j < retList.size(); j ++) {
            Double acat = Double.valueOf((Integer) cat[j]);
            Double aprob = (Double) prob[j];
            String id = retList.get(j);
            retMap.put(id, new Double[]{ acat, aprob });
            //MutablePair pair = (MutablePair) classifyMap.get(id);
            //pair.setRight(acat);
            Pair pair = classifyMap.get(id);
            if (pair.getRight() != null) {
                int jj = 0;
            }
            MutablePair mutablePair = new MutablePair(pair.getLeft(), null);
            mutablePair.setRight(acat);
            classifyMap.put(id, mutablePair);
        }
        return retMap;
    }

    private void getClassifyArray(Map<String, Pair<double[], Double>> map2, List<String> retList, Object[][] objobj) {
        int i = 0;
        for (Entry<String, Pair<double[], Double>> entry : map2.entrySet()) {
            double[] value = entry.getValue().getLeft();
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
        return MLConstants.GEM;
    }

    @Override
    public LearnTestClassifyResult learntestclassify(NeuralNetConfigs nnconfigs, Aggregator indicator, Map<String, Pair<double[], Double>> learnMap,
            MLClassifyModel model, int size, int classes, Map<String, Pair<double[], Double>> classifyMap,
            Map<Double, String> shortMap, String path, String filename) {
        LearnTestClassifyResult result = new LearnTestClassifyResult();
        if (classifyMap == null || classifyMap.isEmpty()) {
            result.setCatMap(new HashMap<>());
            return result;
        }
        Object[][] trainingArray = new Object[learnMap.size()][];
        Object[] trainingCatArray = new Object[learnMap.size()];
        getTrainingSet(learnMap, trainingArray, trainingCatArray);
        LearnTestClassify param = new LearnTestClassify();
        GemSConfig sconfig = null;
        GemIConfig iconfig = null;
        GemMMConfig mconfig = null;
        GemIcarlConfig icarlconfig = null;
        GemEWCConfig ewcconfig = null;
        GemGEMConfig gemconfig = null;
        if (nnconfigs != null) {
             sconfig = nnconfigs.getGemConfig().getGemSConfig();
             iconfig = nnconfigs.getGemConfig().getGemIConfig();
             mconfig = nnconfigs.getGemConfig().getGemMMConfig();
             icarlconfig = nnconfigs.getGemConfig().getGemIcarlConfig();
             ewcconfig = nnconfigs.getGemConfig().getGemEWCConfig();
             gemconfig = nnconfigs.getGemConfig().getGemGEMConfig();
        }
        if (sconfig == null) {
            sconfig = new GemSConfig(1000, 2, 2, 0.1);
        }
        if (iconfig == null) {
            iconfig = new GemIConfig(1000, 2, 2, 0.1, false, false);
        }
        if (mconfig == null) {
            mconfig = new GemMMConfig(1000, 2, 2, 0.1);
        }
        if (icarlconfig == null) {
            icarlconfig = new GemIcarlConfig(1000, 2, 2, 0.1, 10, 1, 10);
        }
        if (ewcconfig == null) {
            ewcconfig = new GemEWCConfig(1000, 2, 2, 0.1, 10, 1);
        }
        if (gemconfig == null) {
            gemconfig = new GemGEMConfig(1000, 2, 2, 0.1, 256, 0.5);
        }
        param.setGemSConfig(sconfig);
        param.setGemIConfig(iconfig);
        param.setGemMMConfig(mconfig);
        param.setGemIcarlConfig(icarlconfig);
        param.setGemEWCConfig(ewcconfig);
        param.setGemGEMConfig(gemconfig);
        NeuralNetConfig m = ((MLClassifyGemModel) model).getModelAndSet(nnconfigs, param);
        param.setTrainingarray(trainingArray);
        param.setTrainingcatarray(trainingCatArray);
        param.setModelInt(model.getId());
        param.setSize(size);
        param.setClasses(classes);
        List<String> retList = new ArrayList<>();
        Object[][] classifyArray = new Object[classifyMap.size()][];
        getClassifyArray(classifyMap, retList, classifyArray);
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
        Map<String, Double[]> retMap = getCatMap(retList, classifyMap, ret);
        result.setCatMap(retMap);
        return result;
    }

    @Override
    public void clean() {        
    }

}

