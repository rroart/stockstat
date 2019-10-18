package roart.ml.tensorflow;

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

import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;
import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.TensorflowCNNConfig;
import roart.common.ml.TensorflowDNNConfig;
import roart.common.ml.TensorflowGRUConfig;
import roart.common.ml.TensorflowLICConfig;
import roart.common.ml.TensorflowLIRConfig;
import roart.common.ml.TensorflowLSTMConfig;
import roart.common.ml.TensorflowMLPConfig;
import roart.common.ml.TensorflowRNNConfig;
import roart.eureka.util.EurekaUtil;
import roart.ml.common.MLClassifyAccess;
import roart.ml.common.MLClassifyModel;
import roart.ml.common.MLMeta;
import roart.ml.model.LearnTestClassify;
import roart.ml.model.LearnTestClassifyResult;
import roart.pipeline.common.aggregate.Aggregator;

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
        if (conf.wantTensorflowDNN()) {
            MLClassifyModel model = new MLClassifyTensorflowDNNModel(conf);
            models.add(model);
        }
        if (conf.wantTensorflowLIC()) {
            MLClassifyModel model = new MLClassifyTensorflowLICModel(conf);
            models.add(model);
        }	    
        if (conf.wantTensorflowLIR()) {
            MLClassifyModel model = new MLClassifyTensorflowLIRModel(conf);
            models.add(model);
        }           
        if (conf.wantTensorflowMLP()) {
            MLClassifyModel model = new MLClassifyTensorflowMLPModel(conf);
            models.add(model);
        }           
        if (conf.wantTensorflowCNN()) {
            MLClassifyModel model = new MLClassifyTensorflowCNNModel(conf);
            models.add(model);
        }           
        if (conf.wantTensorflowRNN()) {
            MLClassifyModel model = new MLClassifyTensorflowRNNModel(conf);
            models.add(model);
        }           
        if (conf.wantTensorflowGRU()) {
            MLClassifyModel model = new MLClassifyTensorflowGRUModel(conf);
            models.add(model);
        }           
        if (conf.wantTensorflowLSTM()) {
            MLClassifyModel model = new MLClassifyTensorflowLSTMModel(conf);
            models.add(model);
        }           
    }

    @Override
    public Double learntest(NeuralNetConfigs nnconfigs, Aggregator indicator, Map<String, Pair<Object, Double>> map, MLClassifyModel model, int size,
            int classes, String filename) {
        return learntestInner(nnconfigs, map, size, classes, model);
    }

    @Override
    public List<MLClassifyModel> getModels() {
        return models;
    }

    private Double learntestInner(NeuralNetConfigs nnconfigs, Map<String, Pair<Object, Double>> map, int size, int classes,
            MLClassifyModel model) {
        // not used?
        //List<List<Object>> listlist = getListList(map);
        Object[] objobj = new Object[map.size()];
        Object[] cat = new Object[map.size()];
        getTrainingSet(map, objobj, cat);
        LearnTestClassify param = new LearnTestClassify();
        param.setTensorflowDNNConfig(nnconfigs.getTensorflowConfig().getTensorflowDNNConfig());
        param.setTensorflowLICConfig(nnconfigs.getTensorflowConfig().getTensorflowLICConfig());
        param.setTensorflowLIRConfig(nnconfigs.getTensorflowConfig().getTensorflowLIRConfig());
        param.setTensorflowMLPConfig(nnconfigs.getTensorflowConfig().getTensorflowMLPConfig());
        param.setTensorflowCNNConfig(nnconfigs.getTensorflowConfig().getTensorflowCNNConfig());
        param.setTensorflowRNNConfig(nnconfigs.getTensorflowConfig().getTensorflowRNNConfig());
        param.setTensorflowGRUConfig(nnconfigs.getTensorflowConfig().getTensorflowGRUConfig());
        param.setTensorflowLSTMConfig(nnconfigs.getTensorflowConfig().getTensorflowLSTMConfig());
        param.setTrainingarray(objobj);
        param.setTrainingcatarray(cat);
        param.setModelInt(model.getId());
        param.setSize(size);
        param.setClasses(classes);
        log.info("evalin {} {} {}", param.getModelInt());
        LearnTestClassify test = EurekaUtil.sendMe(LearnTestClassify.class, param, tensorflowServer + "/learntest");
        return test.getAccuracy();
    }

    private void getTrainingSet(Map<String, Pair<Object, Double>> map, Object[] objobj, Object[] cat) {
        int i = 0;
        for (Entry<String, Pair<Object, Double>> entry : map.entrySet()) {
            Pair<Object, Double> pair = entry.getValue();
            Object key = pair.getLeft();
            cat[i] = pair.getRight();
            objobj[i++] = key;
        }
    }

    private void getTrainingSetOld(Map<String, Pair<Object, Double>> map, Object[][] objobj, Object[] cat) {
        int i = 0;
        for (Entry<String, Pair<Object, Double>> entry : map.entrySet()) {
            double[] key = (double[]) entry.getValue().getLeft();
            Object[] obj = new Object[key.length/* + 1*/];
            for (int j = 0; j < key.length; j ++) {
                obj[j] = key[j];
            }
            Pair<Object, Double> pair = entry.getValue();
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
    public Map<String, Double[]> classify(Aggregator indicator, Map<String, Pair<Object, Double>> map, MLClassifyModel model, int size,
            int classes, Map<Double, String> shortMap) {
        Map<Integer, Map<String, Double[]>> retMap = new HashMap<>();
        if (map.isEmpty()) {
            return new HashMap<>();
        }
        return classifyInner(map, model, size, classes);
    }

    private Map<String, Double[]> classifyInner(Map<String, Pair<Object, Double>> map, MLClassifyModel model, int size,
            int classes) {
        LearnTestClassify param = new LearnTestClassify();
        List<String> retList = new ArrayList<>();
        Object[] objobj = new Object[map.size()];
        getClassifyArray(map, retList, objobj);
        param.setClassifyarray(objobj);
        param.setModelInt(model.getId());
        param.setSize(size);
        param.setClasses(classes);
        for(Object obj : objobj) {
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

    private Map<String, Double[]> getCatMap(List<String> retList, Map<String, Pair<Object, Double>> classifyMap, LearnTestClassify ret) {
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

    private void getClassifyArray(Map<String, Pair<Object, Double>> map2, List<String> retList, Object[] objobj) {
        int i = 0;
        for (Entry<String, Pair<Object, Double>> entry : map2.entrySet()) {
            Pair<Object, Double> pair = entry.getValue();
            Object value = pair.getLeft();
            objobj[i++] = value;
            retList.add(entry.getKey());
        }
    }

    private void getClassifyArrayOld(Map<String, Pair<Object, Double>> map2, List<String> retList, Object[][] objobj) {
        int i = 0;
        for (Entry<String, Pair<Object, Double>> entry : map2.entrySet()) {
            double[] value = (double[]) entry.getValue().getLeft();
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
        return MLConstants.TENSORFLOW;
    }

    @Override
    public LearnTestClassifyResult learntestclassify(NeuralNetConfigs nnconfigs, Aggregator indicator, Map<String, Pair<Object, Double>> learnMap,
            MLClassifyModel model, int size, int classes, Map<String, Pair<Object, Double>> classifyMap,
            Map<Double, String> shortMap, String path, String filename, NeuralNetCommand neuralnetcommand, MLMeta mlmeta) {
        LearnTestClassifyResult result = new LearnTestClassifyResult();
        if (neuralnetcommand.isMlclassify() && (classifyMap == null || classifyMap.isEmpty())) {
            result.setCatMap(new HashMap<>());
            return result;
        }
        boolean persist = model.wantPersist();
        if (!neuralnetcommand.isMldynamic() != persist) {
            return result;
        }
        LearnTestClassify param = new LearnTestClassify();
        param.setPath(path);
        param.setFilename(filename);
        param.setNeuralnetcommand(neuralnetcommand);
        try {
            LearnTestClassify ret = null;
            ret = EurekaUtil.sendMe(LearnTestClassify.class, param, tensorflowServer + "/filename");
            boolean exists = ret.getExists();
            if (!exists && (!neuralnetcommand.isMldynamic() && neuralnetcommand.isMlclassify())) {
                return result;
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
        Object[] trainingArray = new Object[learnMap.size()];
        Object[] trainingCatArray = new Object[learnMap.size()];
        getTrainingSet(learnMap, trainingArray, trainingCatArray);
        Map<String, String> configMap = new NeuralNetConfigs().getConfigMapRev();
        String config = configMap.get(model.getKey());
        if (nnconfigs == null) {
            nnconfigs = new NeuralNetConfigs();
        }
        nnconfigs.getAndSet(config);
        /*
        TensorflowDNNConfig dnnConfig = null;
        TensorflowLICConfig licconfig = null;
        TensorflowLIRConfig lirconfig = null;
        TensorflowMLPConfig mlpconfig = null;
        TensorflowCNNConfig cnnconfig = null;
        TensorflowRNNConfig rnnconfig = null;
        TensorflowGRUConfig gruconfig = null;
        TensorflowLSTMConfig lstmconfig = null;
        if (nnconfigs != null) {
            dnnConfig = nnconfigs.getTensorflowConfig().getTensorflowDNNConfig();
            licconfig = nnconfigs.getTensorflowConfig().getTensorflowLICConfig();
            lirconfig = nnconfigs.getTensorflowConfig().getTensorflowLIRConfig();
            mlpconfig = nnconfigs.getTensorflowConfig().getTensorflowMLPConfig();
            cnnconfig = nnconfigs.getTensorflowConfig().getTensorflowCNNConfig();
            rnnconfig = nnconfigs.getTensorflowConfig().getTensorflowRNNConfig();
            gruconfig = nnconfigs.getTensorflowConfig().getTensorflowGRUConfig();
            lstmconfig = nnconfigs.getTensorflowConfig().getTensorflowLSTMConfig();
       }
        if (dnnConfig == null && licconfig == null) {
            int jj = 0;
        }
        if (dnnConfig == null) {
            dnnConfig = new TensorflowDNNConfig(1000, 2, 100);
        }
        if (licconfig == null) {
            licconfig = new TensorflowLICConfig(1000);
        }
        if (lirconfig == null) {
            lirconfig = new TensorflowLIRConfig(1000);
        }
        if (mlpconfig == null) {
            mlpconfig = new TensorflowMLPConfig(1000, 2, 1000, 0.01);
        }
        if (cnnconfig == null) {
            cnnconfig = new TensorflowCNNConfig(1000, 4, 1, 0.5);
        }
        if (rnnconfig == null) {
            rnnconfig = new TensorflowRNNConfig(1000, 2, 1000, 0.01, 1, 0, 0);
        }
        if (gruconfig == null) {
            gruconfig = new TensorflowGRUConfig(1000, 2, 100, 0.01, 1, 0, 0);
        }
        if (lstmconfig == null) {
            lstmconfig = new TensorflowLSTMConfig(1000, 2, 100, .01, 1, 0, 0);
        }
        param.setTensorflowDNNConfig(dnnConfig);
        param.setTensorflowLICConfig(licconfig);
        param.setTensorflowLIRConfig(lirconfig);
        param.setTensorflowMLPConfig(mlpconfig);
        param.setTensorflowCNNConfig(cnnconfig);
        param.setTensorflowRNNConfig(rnnconfig);
        param.setTensorflowGRUConfig(gruconfig);
        param.setTensorflowLSTMConfig(lstmconfig);
        */
        NeuralNetConfig m = ((MLClassifyTensorflowModel) model).getModelAndSet(nnconfigs, param);
        param.setTrainingarray(trainingArray);
        param.setTrainingcatarray(trainingCatArray);
        param.setModelInt(model.getId());
        param.setSize(size);
        param.setClasses(classes);
        List<String> retList = new ArrayList<>();
        Object[] classifyArray = new Object[classifyMap.size()];
        getClassifyArray(classifyMap, retList, classifyArray);
        param.setClassifyarray(classifyArray);
        for(Object obj : classifyArray) {
            log.info("inner {}", Arrays.asList(obj));
        }
        LearnTestClassify ret = null;
        /*
        if (model.getId() == 1) {
            log.info("Used ML config {}", dnnConfig);
        } else {
            log.info("Used ML config {}", licconfig);
        }
        */
        try {
            ret = EurekaUtil.sendMe(LearnTestClassify.class, param, tensorflowServer + "/learntestclassify");
        } catch (Exception e) {
            log.error("Exception", e);
            return result;
        }
        result.setAccuracy(ret.getAccuracy());
        if (ret.getClassifycatarray() != null) {
            Map<String, Double[]> retMap = getCatMap(retList, classifyMap, ret);
            result.setCatMap(retMap);
        }
        return result;
    }

    @Override
    public void clean() {        
    }

}

