package roart.ml.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;
import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.model.LearnTestClassifyResult;
import roart.pipeline.common.aggregate.Aggregator;

public class MLClassifyRandomAccess extends MLClassifyAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private MyMyConfig conf;

    private Random random = new Random();

    public MLClassifyRandomAccess(MyMyConfig conf) {
        this.conf = conf;
        findModels();
    }

    private void findModels() {
        models = new ArrayList<>();
        MLClassifyModel model = new MLClassifyRandomModel(conf);
        models.add(model);
    }

    @Override
    public Double learntest(NeuralNetConfigs nnconfigs, Aggregator indicator, Map<String, Pair<Object, Double>> map, MLClassifyModel model, int size,
            int outcomes, String filename) {
        return random.nextDouble();
    }

    @Override
    public List<MLClassifyModel> getModels() {
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
    public Map<String, Double[]> classify(Aggregator indicator, Map<String, Pair<Object, Double>> map, MLClassifyModel model, int size,
            int outcomes, Map<Double, String> shortMap) {
        return new HashMap<>();
    }

    private Map<String, Double[]> getCatMap(List<String> retList, Map<String, Pair<Object, Double>> classifyMap, Object[] list) {
        Map<String, Double[]> retMap = new HashMap<>();
        for (int j = 0; j < retList.size(); j ++) {
            Double acat = (Double) list[random.nextInt(list.length)];
            Double aprob = random.nextDouble();
            String id = retList.get(j);
            retMap.put(id, new Double[]{ acat, aprob });
            Pair pair = (ImmutablePair) classifyMap.get(id);
            if (pair.getRight() != null) {
                int jj = 0;
            }
            MutablePair mutablePair = new MutablePair(pair.getLeft(), null);
            mutablePair.setRight(acat);
            classifyMap.put(id, mutablePair);
        }
        return retMap;
    }

    private void getClassifyArray(Map<String, Pair<Object, Double>> map2, List<String> retList, Object[][] objobj) {
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
        return MLConstants.RND;
    }

    @Override
    public String getShortName() {
        return MLConstants.RND;
    }

    @Override
    public LearnTestClassifyResult learntestclassify(NeuralNetConfigs nnconfigs, Aggregator indicator, Map<String, Pair<Object, Double>> learnMap,
            MLClassifyModel model, int size, int outcomes, Map<String, Pair<Object, Double>> classifyMap,
            Map<Double, String> shortMap, String path, String filename, NeuralNetCommand neuralnetcommand, MLMeta mlmeta) {
        LearnTestClassifyResult result = new LearnTestClassifyResult();
        if (classifyMap == null || classifyMap.isEmpty()) {
            result.setCatMap(new HashMap<>());
            return result;
        }
        Object[] list = null;
        Map<Object, Long> countMap = null;
        if (learnMap != null) {
            //IndicatorUtils.filterNonExistingClassifications2(labelMapShort, learnMap);
            countMap = learnMap.values().stream().collect(Collectors.groupingBy(e -> e.getRight(), Collectors.counting()));                            
            list = countMap.keySet().toArray();
        }
        List<String> retList = new ArrayList<>();
        Object[][] classifyArray = new Object[classifyMap.size()][];
        getClassifyArray(classifyMap, retList, classifyArray);
        result.setAccuracy(random.nextDouble());
        Map<String, Double[]> retMap = getCatMap(retList, classifyMap, list);
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

