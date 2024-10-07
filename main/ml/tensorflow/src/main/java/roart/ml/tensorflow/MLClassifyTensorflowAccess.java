package roart.ml.tensorflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;

import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
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
import roart.common.util.InetUtil;
import roart.common.webflux.WebFluxUtil;
import roart.ml.common.MLClassifyAccess;
import roart.ml.common.MLClassifyModel;
import roart.ml.common.MLMeta;
import roart.ml.model.LearnClassify;
import roart.ml.model.LearnTestClassify;
import roart.ml.model.LearnTestClassifyResult;
import roart.pipeline.common.aggregate.Aggregator;

public class MLClassifyTensorflowAccess extends MLClassifyAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private IclijConfig conf;

    private List<String> tensorflowServers;

    private List<MLClassifyModel>  mymodels;

    public MLClassifyTensorflowAccess(IclijConfig conf) {
        this.conf = conf;
        findModels();
        String serverString = conf.getTensorflowServer();
        tensorflowServers = new InetUtil().getServers(serverString);
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
        if (conf.wantTensorflowMLP()) {
            MLClassifyModel model = new MLClassifyTensorflowMLPModel(conf);
            models.add(model);
        }           
        if (conf.wantTensorflowCNN()) {
            MLClassifyModel model = new MLClassifyTensorflowCNNModel(conf);
            models.add(model);
        }           
        if (conf.wantTensorflowCNN2()) {
            MLClassifyModel model = new MLClassifyTensorflowCNN2Model(conf);
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
    public Double learntest(NeuralNetConfigs nnconfigs, Aggregator indicator, List<LearnClassify> map, MLClassifyModel model, int size,
            int classes, String filename) {
        return learntestInner(nnconfigs, map, size, classes, model);
    }

    @Override
    public List<MLClassifyModel> getModels() {
        return models;
    }

    @Override
    public List<MLClassifyModel> getModels(String model) {
        if (mymodels != null) {
            return mymodels;
        }
        mymodels = new ArrayList<>();
        if (model.equals(MLConstants.LIR) && conf.wantPredictorTensorflowLIR()) {
            MLClassifyModel amodel = new MLClassifyTensorflowLIRModel(conf);
            mymodels.add(amodel);
        }           
        if (model.equals(MLConstants.MLP) && conf.wantPredictorTensorflowMLP()) {
            MLClassifyModel amodel = new MLClassifyTensorflowMLPModel(conf);
            mymodels.add(amodel);
        }           
        if (model.equals(MLConstants.RNN) && conf.wantPredictorTensorflowRNN()) {
            MLClassifyModel amodel = new MLClassifyTensorflowRNNModel(conf);
            mymodels.add(amodel);
        }           
        if (model.equals(MLConstants.LSTM) && conf.wantPredictorTensorflowLSTM()) {
            MLClassifyModel amodel = new MLClassifyTensorflowLSTMModel(conf);
            mymodels.add(amodel);
        }           
        if (model.equals(MLConstants.GRU) && conf.wantPredictorTensorflowGRU()) {
            MLClassifyModel amodel = new MLClassifyTensorflowGRUModel(conf);
            mymodels.add(amodel);
        }           
        return mymodels;
    }
    
    private Double learntestInner(NeuralNetConfigs nnconfigs, List<LearnClassify> map, int size, int classes,
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
        LearnTestClassify test = WebFluxUtil.sendMe(LearnTestClassify.class, param, tensorflowServers.get(0) + "/learntest");
        return test.getAccuracy();
    }

    private void getTrainingSet(List<LearnClassify> list, Object[] objobj, Object[] cat) {
        int i = 0;
        for (LearnClassify entry : list) {
            Object key = entry.getArray();
            cat[i] = entry.getClassification();
            objobj[i++] = key;
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
        LearnTestClassify test = WebFluxUtil.sendMe(LearnTestClassify.class, param, tensorflowServers.get(0) + "/eval");
        return test.getAccuracy();
    }

    @Override
    public Map<String, Double[]> classify(Aggregator indicator, List<LearnClassify> map, MLClassifyModel model, int size,
            int classes, Map<Double, String> shortMap) {
        Map<Integer, Map<String, Double[]>> retMap = new HashMap<>();
        if (map.isEmpty()) {
            return new HashMap<>();
        }
        return classifyInner(map, model, size, classes);
    }

    private Map<String, Double[]> classifyInner(List<LearnClassify> map, MLClassifyModel model, int size,
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
            //log.info("inner {}", Arrays.asList(obj));
        }
        LearnTestClassify ret = null;
        try {
            ret = WebFluxUtil.sendMe(LearnTestClassify.class, param, tensorflowServers.get(0) + "/classify");
        } catch (Exception e) {
            log.error("Exception", e);
        }
        Map<String, Double[]> retMap = getCatMap(retList, map, ret, true);
        return retMap;
    }

    private Map<String, Double[]> getCatMap(List<String> retList, List<LearnClassify> classifyMap, LearnTestClassify ret, boolean classify) {
        Object[] cat = ret.getClassifycatarray();
        Object[] prob = ret.getClassifyprobarray();
        Map<String, Double[]> retMap = new HashMap<>();
        for (int j = 0; j < retList.size(); j ++) {
            String id = retList.get(j);
            if (classify) {
                Double acat = Double.valueOf((Integer) cat[j]);
                Double aprob = (Double) prob[j];
                retMap.put(id, new Double[]{ acat, aprob });
            } else {
                ArrayList list = (ArrayList) cat[j];
                retMap.put(id, Arrays.copyOf((list).toArray(), list.size(), Double[].class));
            }
            LearnClassify triple = classifyMap.get(j);
            if (triple.getClassification() != null) {
                int jj = 0;
            }
            if (classify) {
            LearnClassify mutableList = new LearnClassify(triple.getId(), triple.getArray(), (Integer) cat[j]);
            //triple.setRight(acat);
            classifyMap.set(j, mutableList);
            }
        }
        return retMap;
    }

    private void getClassifyArray(List<LearnClassify> list, List<String> retList, Object[] objobj) {
        int i = 0;
        for (LearnClassify entry : list) {
            Object value = entry.getArray();
            objobj[i++] = value;
            retList.add((String) entry.getId());
        }
    }

    @Override
    public String getName() {
        return MLConstants.TENSORFLOW;
    }

    @Override
    public String getShortName() {
        return MLConstants.TF;
    }

    @Override
    public LearnTestClassifyResult learntestclassify(NeuralNetConfigs nnconfigs, Aggregator indicator, List<LearnClassify> learnMap,
            MLClassifyModel model, int size, int classes, List<LearnClassify> classifyMap,
            Map<Double, String> shortMap, String path, String filename, NeuralNetCommand neuralnetcommand, MLMeta mlmeta, boolean classify) {
        LearnTestClassifyResult result = new LearnTestClassifyResult();
        if (classify && neuralnetcommand.isMlclassify() && (classifyMap == null || classifyMap.isEmpty())) {
            result.setCatMap(new HashMap<>());
            return result;
        }
        // return if
        // persist is false and dynamic is false
        boolean persist = model.wantPersist();
        if (!neuralnetcommand.isMldynamic() && !persist) {
            return result;
        }
        LearnTestClassify param = new LearnTestClassify();
        param.setPath(path);
        param.setFilename(filename);
        param.setNeuralnetcommand(neuralnetcommand);
        for (String tensorflowServer : tensorflowServers) {
        try {
            LearnTestClassify ret = null;
            ret = WebFluxUtil.sendMe(LearnTestClassify.class, param, tensorflowServer + "/filename");
            boolean exists = ret.getExists();
            if (!exists && (!neuralnetcommand.isMldynamic() && neuralnetcommand.isMlclassify())) {
                return result;
            }
            break;
        } catch (Exception e) {
            log.error("Exception", e);
        }
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
        NeuralNetConfig m = ((MLClassifyTensorflowModel) model).getModelAndSet(nnconfigs, param);
        param.setTrainingarray(trainingArray);
        param.setClassify(classify);
          if (classify) {
            param.setTrainingcatarray(trainingCatArray);
        } else {
            param.setTrainingcatarray(new Object[0]);
        }
        param.setModelInt(model.getId());
        param.setSize(size);
        param.setClasses(classes);
        List<String> retList = new ArrayList<>();
        if (true || classify) {
            Object[] classifyArray = new Object[classifyMap.size()];
            getClassifyArray(classifyMap, retList, classifyArray);
            param.setClassifyarray(classifyArray);
            for(Object obj : classifyArray) {
                //log.info("inner {}", Arrays.asList(obj));
            }
        }
        LearnTestClassify ret = null;
        for (String tensorflowServer : tensorflowServers) {
        try {
            ret = WebFluxUtil.sendMe(LearnTestClassify.class, param, tensorflowServer + "/learntestclassify");
            boolean exception = ret.getException() != null && ret.getException();
            boolean gpu = ret.getGpu() != null && ret.getGpu();
            boolean cudnn = ret.getCudnn() != null && ret.getCudnn();
            boolean memory = ret.getMemory() != null && ret.getMemory();
            if (exception) {
                if (gpu) { 
                    if (memory) {
                        log.error("CUDA out of memory for {}", filename);
                        continue;
                    }
                    if (cudnn) {
                        log.error("CUDNN initialization for {}", filename);
                        continue;
                    }
                }
                log.error("Tensorflow aborted?");
            } else {
                log.info("Completed {} on {}", filename, tensorflowServer);
                break;
            }
        } catch (ResourceAccessException e) {
            log.error("Exception", e);
        } catch (Exception e) {
            log.error("Exception", e);
            return result;
        }
        }
        result.setAccuracy(ret.getAccuracy());
        result.setTrainaccuracy(ret.getTrainaccuracy());
        result.setLoss(ret.getLoss());
        if (ret.getClassifycatarray() != null) {
            Map<String, Double[]> retMap = getCatMap(retList, classifyMap, ret, classify);
            result.setCatMap(retMap);
        }
        result.setClassify(param.getClassify());
        return result;
    }

    @Override
    public LearnTestClassifyResult dataset(NeuralNetConfigs nnconfigs, MLClassifyModel model,
            NeuralNetCommand neuralnetcommand,
            MLMeta mlmeta, String dataset) {
        LearnTestClassifyResult result = new LearnTestClassifyResult();
        // return if
        // persist is false and dynamic is false
        boolean persist = model.wantPersist();
        if (!neuralnetcommand.isMldynamic() && !persist) {
            return result;
        }
        LearnTestClassify param = new LearnTestClassify();
        param.setNeuralnetcommand(neuralnetcommand);
        Map<String, String> configMap = new NeuralNetConfigs().getConfigMapRev();
        String config = configMap.get(model.getKey());
        if (nnconfigs == null) {
            nnconfigs = new NeuralNetConfigs();
        }
        nnconfigs.getAndSet(config);
        NeuralNetConfig m = ((MLClassifyTensorflowModel) model).getModelAndSet(nnconfigs, param);
        param.setModelInt(model.getId());
        param.setDataset(dataset);
        param.setZero(true);
        LearnTestClassify ret = null;
        for (String tensorflowServer : tensorflowServers) {
        try {
            ret = WebFluxUtil.sendMe(LearnTestClassify.class, param, tensorflowServer + "/dataset");
            boolean exception = ret.getException() != null && ret.getException();
            boolean gpu = ret.getGpu() != null && ret.getGpu();
            boolean cudnn = ret.getCudnn() != null && ret.getCudnn();
            boolean memory = ret.getMemory() != null && ret.getMemory();
            if (exception) {
                if (gpu) { 
                    if (memory) {
                        log.error("CUDA out of memory for {}", dataset);
                        continue;
                    }
                    if (cudnn) {
                        log.error("CUDNN initialization for {}", dataset);
                        continue;
                    }
                }
                log.error("Tensorflow aborted?");
            } else {
                log.info("Completed {} on {}", dataset, tensorflowServer);
                break;
            }
        } catch (ResourceAccessException e) {
            log.error("Exception", e);
        } catch (Exception e) {
            log.error("Exception", e);
            return result;
        }
        }
        result.setAccuracy(ret.getAccuracy());
        result.setTrainaccuracy(ret.getTrainaccuracy());
        result.setClassify(ret.getClassify());
        return result;
    }

    @Override
    public void clean() {        
    }

}

