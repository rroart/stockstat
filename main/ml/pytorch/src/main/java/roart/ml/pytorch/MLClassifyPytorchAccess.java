package roart.ml.pytorch;

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
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.PytorchCNNConfig;
import roart.common.ml.PytorchGRUConfig;
import roart.common.ml.PytorchLSTMConfig;
import roart.common.ml.PytorchMLPConfig;
import roart.common.ml.PytorchRNNConfig;
import roart.eureka.util.EurekaUtil;
import roart.ml.common.MLClassifyAccess;
import roart.ml.common.MLClassifyModel;
import roart.ml.model.LearnTestClassify;
import roart.ml.model.LearnTestClassifyResult;
import roart.pipeline.common.aggregate.Aggregator;

public class MLClassifyPytorchAccess extends MLClassifyAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private MyMyConfig conf;

    private String tensorflowServer;

    public MLClassifyPytorchAccess(MyMyConfig conf) {
        this.conf = conf;
        findModels();
        tensorflowServer = conf.getPytorchServer();
    }

    private void findModels() {
        models = new ArrayList<>();
        if (conf.wantPytorchMLP()) {
            MLClassifyModel model = new MLClassifyPytorchMLPModel(conf);
            models.add(model);
        }
        if (conf.wantPytorchCNN()) {
            MLClassifyModel model = new MLClassifyPytorchCNNModel(conf);
            models.add(model);
        }
        if (conf.wantPytorchRNN()) {
            MLClassifyModel model = new MLClassifyPytorchRNNModel(conf);
            models.add(model);
        }	
        if (conf.wantPytorchLSTM()) {
            MLClassifyModel model = new MLClassifyPytorchLSTMModel(conf);
            models.add(model);
        }       
        if (conf.wantPytorchGRU()) {
            MLClassifyModel model = new MLClassifyPytorchGRUModel(conf);
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
        //param.setPytorchDNNConfig(nnconfigs.getPytorchDNNConfig());
        //param.setPytorchLConfig(nnconfigs.getPytorchLConfig());
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
        return MLConstants.PYTORCH;
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
        PytorchMLPConfig mlpconfig = null;
        PytorchCNNConfig cnnconfig = null;
        PytorchRNNConfig rnnconfig = null;
        PytorchGRUConfig gruconfig = null;
        PytorchLSTMConfig lstmconfig = null;
        if (nnconfigs != null) {
            //dnnConfig = nnconfigs.getPytorchDNNConfig();
            //lconfig = nnconfigs.getPytorchLConfig();
        }
        if (nnconfigs != null) {
            mlpconfig = nnconfigs.getPytorchConfig().getPytorchMLPConfig();
            cnnconfig = nnconfigs.getPytorchConfig().getPytorchCNNConfig();
            rnnconfig = nnconfigs.getPytorchConfig().getPytorchRNNConfig();
            gruconfig = nnconfigs.getPytorchConfig().getPytorchGRUConfig();
            lstmconfig = nnconfigs.getPytorchConfig().getPytorchLSTMConfig();
       }
        if (mlpconfig == null) {
            mlpconfig = new PytorchMLPConfig(1000, 3, 20, 0.1);
        }
        if (cnnconfig == null) {
            cnnconfig = new PytorchCNNConfig(1000, 4, 1);
        }
        if (rnnconfig == null) {
            rnnconfig = new PytorchRNNConfig(100, 2, 100, 0.001, 1);
        }
        if (gruconfig == null) {
            gruconfig = new PytorchGRUConfig(100, 2, 100, 0.001, 1);
        }
        if (lstmconfig == null) {
            lstmconfig = new PytorchLSTMConfig(100, 2, 100, 0.001, 1);
        }
        param.setPytorchMLPConfig(mlpconfig);
        param.setPytorchCNNConfig(cnnconfig);
        param.setPytorchRNNConfig(rnnconfig);
        param.setPytorchGRUConfig(gruconfig);
        param.setPytorchLSTMConfig(lstmconfig);
        NeuralNetConfig m = ((MLClassifyPytorchModel) model).getModelAndSet(nnconfigs, param);
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
