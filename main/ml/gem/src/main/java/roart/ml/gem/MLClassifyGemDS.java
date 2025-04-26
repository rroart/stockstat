package roart.ml.gem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.GemEWCConfig;
import roart.common.ml.GemGEMConfig;
import roart.common.ml.GemIConfig;
import roart.common.ml.GemIcarlConfig;
import roart.common.ml.GemMMConfig;
import roart.common.ml.GemSConfig;
import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.webflux.WebFluxUtil;
import roart.common.model.ContItem;
import roart.ml.common.MLClassifyDS;
import roart.ml.common.MLClassifyModel;
import roart.ml.common.MLMeta;
import roart.ml.model.LearnClassify;
import roart.ml.model.LearnTestClassify;
import roart.ml.model.LearnTestClassifyResult;
import roart.pipeline.common.aggregate.Aggregator;
import roart.db.dao.IclijDbDao;

public class MLClassifyGemDS extends MLClassifyDS {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private IclijConfig conf;

    private String gemServer;

    private WebFluxUtil webFluxUtil = new WebFluxUtil();
    
    public MLClassifyGemDS(IclijConfig conf) {
        this.conf = conf;
        findModels();
        gemServer = conf.getGEMServer();
    }

    private static final int LIMIT = 100;
    
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
    public Double learntest(NeuralNetConfigs nnconfigs, Aggregator indicator, List<LearnClassify> map, MLClassifyModel model, int size,
            int classes, String filename) {
        return learntestInner(nnconfigs, map, size, classes, model);
    }

    @Override
    public List<MLClassifyModel> getModels() {
        return models;
    }

    private Double learntestInner(NeuralNetConfigs nnconfigs, List<LearnClassify> map, int size, int classes,
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
        LearnTestClassify test = webFluxUtil.sendMe(LearnTestClassify.class, param, gemServer + "/learntest");
        return test.getAccuracy();
    }

    private void getTrainingSet(List<LearnClassify> list, Object[][] objobj, Object[] cat) {
        int i = 0;
        for (LearnClassify entry : list) {
            double[] key = (double[]) entry.getArray();
            Object[] obj = new Object[key.length/* + 1*/];
            for (int j = 0; j < key.length; j ++) {
                obj[j] = key[j];
            }
            cat[i] = entry.getClassification();
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
        LearnTestClassify test = webFluxUtil.sendMe(LearnTestClassify.class, param, gemServer + "/eval");
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
        Object[][] objobj = new Object[map.size()][];
        getClassifyArray(map, retList, objobj);
        param.setClassifyarray(objobj);
        param.setModelInt(model.getId());
        param.setSize(size);
        param.setClasses(classes);
        for(Object[] obj : objobj) {
            //log.info("inner {}", Arrays.asList(obj));
        }
        LearnTestClassify ret = null;
        try {
            ret = webFluxUtil.sendMe(LearnTestClassify.class, param, gemServer + "/classify");
        } catch (Exception e) {
            log.error("Exception", e);
        }
        Map<String, Double[]> retMap = getCatMap(retList, map, ret);
        return retMap;
    }

    private Map<String, Double[]> getCatMap(List<String> retList, List<LearnClassify> classifyMap, LearnTestClassify ret) {
        Object[] cat = ret.getClassifycatarray();
        Object[] prob = ret.getClassifyprobarray();
        Map<String, Double[]> retMap = new HashMap<>();
        for (int j = 0; j < retList.size(); j ++) {
            Double acat = Double.valueOf((Integer) cat[j]);
            Double aprob = null; //(Double) prob[j];
            String id = retList.get(j);
            retMap.put(id, new Double[]{ acat, aprob });
            //MutablePair pair = (MutablePair) classifyMap.get(id);
            //pair.setRight(acat);
            LearnClassify triple = classifyMap.get(j);
            if (triple.getClassification() != null) {
                int jj = 0;
            }
            LearnClassify mutableList = new LearnClassify(triple.getId(), triple.getArray(), acat);
            //triple.setRight(acat);
            classifyMap.set(j, mutableList);
        }
        return retMap;
    }

    private void getClassifyArray(List<LearnClassify> list, List<String> retList, Object[][] objobj) {
        int i = 0;
        for (LearnClassify entry : list) {
            double[] value = (double[]) entry.getArray();
            Object[] obj = new Object[value.length/* + 1*/];
            for (int j = 0; j < value.length; j ++) {
                obj[j] = value[j];
            }
            objobj[i++] = obj;
            retList.add((String) entry.getId());
        }
    }

    @Override
    public String getName() {
        return MLConstants.GEM;
    }

    @Override
    public String getShortName() {
        return MLConstants.GEM;
    }

    @Override
    public LearnTestClassifyResult learntestclassify(NeuralNetConfigs nnconfigs, Aggregator indicator, List<LearnClassify> learnMap,
            MLClassifyModel model, int size, int classes, List<LearnClassify> classifyMap,
            Map<Double, String> shortMap, String path, String filename, NeuralNetCommand neuralnetcommand, MLMeta mlmeta, boolean classify) {
        LearnTestClassifyResult result = new LearnTestClassifyResult();
        if (neuralnetcommand.isMldynamic()) {
            return result;
        }
        if (neuralnetcommand.isMlclassify() && (classifyMap == null || classifyMap.isEmpty())) {
            result.setCatMap(new HashMap<>());
            return result;
        }
        boolean persist = model.wantPersist();
        if (neuralnetcommand.isMldynamic() || !persist) {
            return result;
        }
        LearnTestClassify param = new LearnTestClassify();
        param.setPath(path);
        param.setFilename(filename);
        param.setNeuralnetcommand(neuralnetcommand);
        try {
            LearnTestClassify ret = null;
            ret = webFluxUtil.sendMe(LearnTestClassify.class, param, gemServer + "/filename");
            boolean exists = ret.getExists();
            if (neuralnetcommand.isMldynamic() == true) {
                return result;
            }
            if (!exists && !neuralnetcommand.isMldynamic()) {
                //return result;
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
        Object[][] trainingArray = new Object[learnMap.size()][];
        Object[] trainingCatArray = new Object[learnMap.size()];
        getTrainingSet(learnMap, trainingArray, trainingCatArray);
        List<ContItem> newConts = new ArrayList<>();
        log.info("First training array size {}", trainingArray.length);
        if (neuralnetcommand.isMllearn()) {
            trainingArray = filter(trainingArray, filename, newConts);
            log.info("Filtered training array size {}, limit {}", trainingArray.length, LIMIT);        
            if (newConts.size() < LIMIT) {
                return result;
            }
        }
        Map<String, String> configMap = new NeuralNetConfigs().getConfigMapRev();
        String config = configMap.get(model.getKey());
        if (nnconfigs == null) {
            nnconfigs = new NeuralNetConfigs();
        }
        nnconfigs.getAndSet(config);
        /*
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
            sconfig = new GemSConfig(1000, 2, 2, 0.001);
        }
        if (iconfig == null) {
            iconfig = new GemIConfig(1000, 2, 2, 0.001, false, false);
        }
        if (mconfig == null) {
            mconfig = new GemMMConfig(1000, 2, 2, 0.001);
        }
        if (icarlconfig == null) {
            icarlconfig = new GemIcarlConfig(1000, 2, 2, 0.001, 10, 1, 10, false);
        }
        if (ewcconfig == null) {
            ewcconfig = new GemEWCConfig(1000, 2, 2, 0.001, 10, 1);
        }
        if (gemconfig == null) {
            gemconfig = new GemGEMConfig(1000, 2, 2, 0.001, 256, 0.5, false);
        }
        param.setGemSConfig(sconfig);
        param.setGemIConfig(iconfig);
        param.setGemMMConfig(mconfig);
        param.setGemIcarlConfig(icarlconfig);
        param.setGemEWCConfig(ewcconfig);
        param.setGemGEMConfig(gemconfig);
        */
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
            //log.info("inner {}", Arrays.asList(obj));
        }
        LearnTestClassify ret = null;
        try {
            ret = webFluxUtil.sendMe(LearnTestClassify.class, param, gemServer + "/learntestclassify");
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }
        if (neuralnetcommand.isMllearn()) {
            saveme(newConts);
        }
        result.setAccuracy(ret.getAccuracy());
        if (ret.getClassifycatarray() != null) {
            Map<String, Double[]> retMap = getCatMap(retList, classifyMap, ret);
            result.setCatMap(retMap);
        }
        return result;
    }

    private void saveme(List<ContItem> newConts) {
        long millis0 = System.currentTimeMillis();
        for (ContItem cont : newConts) {
            try {
                IclijDbDao.badDS.save(cont);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        log.info("Time spent {}", (System.currentTimeMillis() - millis0) / 1000);
    }

    private synchronized Object[][] filter(Object[][] trainingArray, String filename, List<ContItem> newConts ) {
        long millis0 = System.currentTimeMillis();
        List<ContItem> conts = null;
        try {
             conts = IclijDbDao.badDS.getAllConts();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        Set<String> md5s = new HashSet<>();
        for (ContItem cont : conts) {
            if (filename.equals(cont.getMd5())) {
                md5s.add(cont.getMd5());
            }
        }
        List<Object[]> filtered = new ArrayList<>();
        for (Object[] anArray : trainingArray) {
            String str = Arrays.toString(anArray);
            log.info("md5str {}", str);
            String md5 = DigestUtils.md5Hex(str);
            if (md5s.contains(md5)) {
                continue;
            }
            filtered.add(anArray);
            ContItem cont = new ContItem();
            cont.setMd5(md5);
            cont.setFilename(filename);
            cont.setDate(LocalDate.now());
            newConts.add(cont);
        }
        Object[][] newfiltered = new Object[filtered.size()][];
        for (int i = 0; i < filtered.size(); i++) {
            newfiltered[i] = filtered.get(i);
        }
        log.info("Time spent {}", (System.currentTimeMillis() - millis0) / 1000);
        return newfiltered;
    }

    @Override
    public LearnTestClassifyResult dataset(NeuralNetConfigs nnconfigs, MLClassifyModel model,
            NeuralNetCommand neuralnetcommand,
            MLMeta mlmeta, String dataset) {
        LearnTestClassifyResult result = new LearnTestClassifyResult();
        if (neuralnetcommand.isMldynamic()) {
            return result;
        }
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
        NeuralNetConfig m = ((MLClassifyGemModel) model).getModelAndSet(nnconfigs, param);
        param.setModelInt(model.getId());
        param.setDataset(dataset);
        param.setZero(true);
        LearnTestClassify ret = null;
        try {
            ret = webFluxUtil.sendMe(LearnTestClassify.class, param, gemServer + "/dataset");
        } catch (Exception e) {
            log.error("Exception", e);
            return result;
        }
        result.setAccuracy(ret.getAccuracy());
        return result;
    }

    @Override
    public void clean() {        
    }

}

