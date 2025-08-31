package roart.ml.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.model.LearnClassify;
import roart.ml.model.LearnTestClassifyResult;
import roart.pipeline.common.aggregate.Aggregator;

public class MLClassifyRandomDS extends MLClassifyDS {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private IclijConfig conf;

    private Random random = new Random();

    public MLClassifyRandomDS(IclijConfig conf) {
        this.conf = conf;
        findModels();
    }

    private void findModels() {
        models = new ArrayList<>();
        models.add(new MLClassifyRandomDim2Model(conf));
        models.add(new MLClassifyRandomDim3Model(conf));
        models.add(new MLClassifyRandomDim4Model(conf));
    }

    @Override
    public Double learntest(NeuralNetConfigs nnconfigs, Aggregator indicator, List<LearnClassify> map, MLClassifyModel model, int size,
            int outcomes, String filename) {
        return random.nextDouble();
    }

    @Override
    public List<MLClassifyModel> getModels() {
        return models;
    }

    @Override
    public List<MLClassifyModel> getModels(String model) {
        return models;
    }
    
    @Override
    public Double eval(int modelInt) {
        return random.nextDouble();
    }

    private List<List<Object>> getListList(Map<Object, Double> map) {
        List<List<Object>> listlist = new ArrayList<>();
        for (Entry<Object, Double> entry : map.entrySet()) {
            Object key = entry.getKey();
            List<Object> list = new ArrayList<>();
            list.addAll(Arrays.asList(key));
            list.add(entry.getValue());
            listlist.add(list);
        }
        return listlist;
    }

    @Override
    public Map<String, Double[]> classify(Aggregator indicator, List<LearnClassify> map, MLClassifyModel model, int size,
            int outcomes, Map<Double, String> shortMap) {
        return new HashMap<>();
    }

    private Map<String, Double[]> getCatMap(List<String> retList, List<LearnClassify> classifyMap, Object[] list, int outcomes, boolean classify) {
        Map<String, Double[]> retMap = new HashMap<>();
        // TODO list emply
        if (list != null) {
        log.info("list empty {} {}", retList.size(), list.length);
        }
        for (int j = 0; j < retList.size(); j ++) {
            String id = retList.get(j);
            if (classify) {
            Double acat = (Double) list[random.nextInt(list.length)];
            Double aprob = random.nextDouble();
            retMap.put(id, new Double[]{ acat, aprob });
            } else {
                LearnClassify triple = classifyMap.get(j);
                double[] l = (double[]) triple.getArray();
                Double last = l[l.length - 1];
                List<Double> cat = new ArrayList<>();
                for (int i = 0; i < outcomes; i++) {
                    last = last * (0.99 + 0.02 * random.nextDouble());
                    cat.add(last);
                }
                ArrayList list2 = (ArrayList) cat;
                retMap.put(id, Arrays.copyOf((list2).toArray(), list2.size(), Double[].class));
            }
            LearnClassify triple = classifyMap.get(j);
            if (triple.getClassification() != null) {
                int jj = 0;
            }
            if (classify) {
            LearnClassify mutableList = new LearnClassify(triple.getId(), triple.getArray(), retMap.get(id)[0]);
            //triple.setRight(acat);
            classifyMap.set(j, mutableList);
            }
        }
        return retMap;
    }

    private void getClassifyArray(List<LearnClassify> list, List<String> retList, Object[] objobj) {
        int i = 0;
        for (LearnClassify entry : list) {
            log.info("MMM" + entry.getArray().getClass().getName());
            Object value = entry.getArray();
            objobj[i++] = value;
            retList.add((String) entry.getId());
        }
    }

    @Override
    public String getName() {
        return MLConstants.RND;
    }

    @Override
    public String getShortName() {
        return MLConstants.RND;
    }

    @Override
    public LearnTestClassifyResult learntestclassify(NeuralNetConfigs nnconfigs, Aggregator indicator, List<LearnClassify> learnMap,
            MLClassifyModel model, int size, int outcomes, List<LearnClassify> classifyMap,
            Map<Double, String> shortMap, String path, String filename, NeuralNetCommand neuralnetcommand, MLMeta mlmeta, boolean classify) {
        LearnTestClassifyResult result = new LearnTestClassifyResult();
        if (classifyMap == null || classifyMap.isEmpty()) {
            result.setCatMap(new HashMap<>());
            return result;
        }
        //log.info("filename {}", );
        Object[] list = null;
        Map<Object, Long> countMap = null;
        if (classify && learnMap != null) {
            //IndicatorUtils.filterNonExistingClassifications2(labelMapShort, learnMap);
            countMap = learnMap.stream().collect(Collectors.groupingBy(e -> e.getClassification(), Collectors.counting()));                            
            list = countMap.keySet().toArray();
        }
        List<String> retList = new ArrayList<>();
        if (true || classify) {
        Object[] classifyArray = new Object[classifyMap.size()];
        getClassifyArray(classifyMap, retList, classifyArray);
        }
        result.setAccuracy(random.nextDouble());
        result.setTrainaccuracy(random.nextDouble());
        Map<String, Double[]> retMap = getCatMap(retList, classifyMap, list, outcomes, classify);
        result.setCatMap(retMap);
        return result;
    }

    @Override
    public void clean() {        
    }

    @Override
    public LearnTestClassifyResult dataset(NeuralNetConfigs nnconfigs, MLClassifyModel model,
            NeuralNetCommand neuralnetcommand, MLMeta mlmeta, String dataset) {
        return null;
    }

}

