package roart.ml.pytorch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.MutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.PytorchCNNConfig;
import roart.common.ml.PytorchCNN2Config;
import roart.common.ml.PytorchGRUConfig;
import roart.common.ml.PytorchLSTMConfig;
import roart.common.ml.PytorchMLPConfig;
import roart.common.ml.PytorchRNNConfig;
import roart.common.util.InetUtil;
import roart.common.webflux.WebFluxUtil;
import roart.ml.common.MLClassifyDS;
import roart.ml.common.MLClassifyModel;
import roart.ml.common.MLMeta;
import roart.ml.model.LearnClassify;
import roart.ml.model.LearnTestClassify;
import roart.ml.model.LearnTestClassifyResult;
import roart.pipeline.common.aggregate.Aggregator;

public class MLClassifyPytorchDS extends MLClassifyDS {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private IclijConfig conf;

    private List<String> pytorchServers;

    private List<MLClassifyModel>  mymodels;

    private WebFluxUtil webFluxUtil = new WebFluxUtil();
    
    public MLClassifyPytorchDS(IclijConfig conf) {
        this.conf = conf;
        findModels();
        String serverString = conf.getPytorchServer();
        pytorchServers = new InetUtil().getServers(serverString);
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
        if (conf.wantPytorchCNN2()) {
            MLClassifyModel model = new MLClassifyPytorchCNN2Model(conf);
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
        if (model.equals(MLConstants.MLP) && conf.wantPredictorPytorchMLP()) {
            MLClassifyModel amodel = new MLClassifyPytorchMLPModel(conf);
            mymodels.add(amodel);
        }           
        if (model.equals(MLConstants.RNN) && conf.wantPredictorPytorchRNN()) {
            MLClassifyModel amodel = new MLClassifyPytorchRNNModel(conf);
            mymodels.add(amodel);
        }           
        if (model.equals(MLConstants.LSTM) && conf.wantPredictorPytorchLSTM()) {
            MLClassifyModel amodel = new MLClassifyPytorchLSTMModel(conf);
            mymodels.add(amodel);
        }           
        if (model.equals(MLConstants.GRU) && conf.wantPredictorPytorchGRU()) {
            MLClassifyModel amodel = new MLClassifyPytorchGRUModel(conf);
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
        //param.setPytorchDNNConfig(nnconfigs.getPytorchDNNConfig());
        //param.setPytorchLConfig(nnconfigs.getPytorchLConfig());
        param.setTrainingarray(objobj);
        param.setTrainingcatarray(cat);
        param.setModelInt(model.getId());
        param.setSize(size);
        param.setClasses(classes);
        log.info("evalin {} {} {}", param.getModelInt());
        LearnTestClassify test = webFluxUtil.sendMe(LearnTestClassify.class, param, pytorchServers.get(0) + "/learntest");
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
        LearnTestClassify test = webFluxUtil.sendMe(LearnTestClassify.class, param, pytorchServers.get(0) + "/eval");
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
            ret = webFluxUtil.sendMe(LearnTestClassify.class, param, pytorchServers.get(0) + "/classify");
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
                Double aprob = null; // (Double) prob[j];
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
        return MLConstants.PYTORCH;
    }

    @Override
    public String getShortName() {
        return MLConstants.PT;
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
        for (String pytorchServer : pytorchServers) {
        try {
            LearnTestClassify ret = null;
            ret = webFluxUtil.sendMe(LearnTestClassify.class, param, pytorchServer + "/filename");
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
        boolean binary = indicator.getConf().wantUseBinary() && classify && classes == 2; // TODO
        Map<String, String> configMap = new NeuralNetConfigs().getConfigMapRev(binary);
        String config = configMap.get(model.getKey(binary));
        if (nnconfigs == null) {
            nnconfigs = new NeuralNetConfigs();
        }
        nnconfigs.getAndSet(config);
        NeuralNetConfig m = ((MLClassifyPytorchModel) model).getModelAndSet(nnconfigs, param, binary);
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
        param.setZero(true);
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
        for (String pytorchServer : pytorchServers) {
        try {
            ret = webFluxUtil.sendMe(LearnTestClassify.class, param, pytorchServer + "/learntestclassify");
            boolean exception = ret.getException() != null && ret.getException();
            boolean gpu = ret.getGpu() != null && ret.getGpu();
            boolean memory = ret.getMemory() != null && ret.getMemory();
            if (exception) {
                if (gpu && memory) {
                    log.error("CUDA out of memory for {}", filename);
                } else {
                    break;
                }
            } else {
                log.info("Completed {} on {}", filename, pytorchServer);
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
        result.setValaccuracy(ret.getValaccuracy());
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
        //boolean binary = indicator.getConf().wantUseBinary() && classify && classes == 2; // TODO
        boolean binary = false; // TODO
        Map<String, String> configMap = new NeuralNetConfigs().getConfigMapRev(binary);
        String config = configMap.get(model.getKey(binary));
        if (nnconfigs == null) {
            nnconfigs = new NeuralNetConfigs();
        }
        nnconfigs.getAndSet(config);
        NeuralNetConfig m = ((MLClassifyPytorchModel) model).getModelAndSet(nnconfigs, param, binary);
        param.setModelInt(model.getId());
        param.setDataset(dataset);
        param.setZero(true);
        LearnTestClassify ret = null;
        for (String pytorchServer : pytorchServers) {
        try {
            ret = webFluxUtil.sendMe(LearnTestClassify.class, param, pytorchServer + "/dataset");
            boolean exception = ret.getException() != null && ret.getException();
            boolean gpu = ret.getGpu() != null && ret.getGpu();
            boolean memory = ret.getMemory() != null && ret.getMemory();
            if (exception) {
                if (gpu && memory) {
                    log.error("CUDA out of memory for {}", dataset);
                } else {
                    break;
                }
            } else {
                log.info("Completed {} on {}", dataset, pytorchServer);
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
        result.setValaccuracy(ret.getValaccuracy());
        result.setClassify(ret.getClassify());
        return result;
    }

    @Override
    public void clean() {        
    }

}

