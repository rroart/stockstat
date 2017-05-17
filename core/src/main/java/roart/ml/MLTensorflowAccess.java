package roart.ml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.indicator.Indicator;
import roart.indicator.IndicatorMACD;
import roart.model.LearnTest;
import roart.ml.MLModel;
import roart.ml.MLTensorflowDNNModel;
import roart.ml.MLTensorflowLModel;
import roart.util.Constants;
import roart.util.EurekaUtil;

public class MLTensorflowAccess extends MLAccess {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public MLTensorflowAccess() {
	    findModels();
	}
	
	private void findModels() {
        models = new ArrayList<>();
        if (IndicatorMACD.wantDNN()) {
            MLModel model = new MLTensorflowDNNModel();
            models.add(model);
        }
        if (IndicatorMACD.wantL()) {
            MLModel model = new MLTensorflowLModel();
            models.add(model);
        }	    
	}
	
    @Override
    public void learntest(Indicator indicator, Map<double[], Double> map, MLModel model, int size, String period, String mapname,
            int outcomes) {
        //List<MLModel> models = getModels();
        //for (MLModel modelInt : models) {
        learntestInner(map, size, period, mapname, outcomes, model);
    //}
    }

    @Override
    public List<MLModel> getModels() {
        return models;
    }

    /*
    private Map<Integer, String> getModelsMap() {
        Map<Integer, String> models = new HashMap<>();
        if (IndicatorMACD.wantDNN()) {
            int model = IndicatorMACD.MULTILAYERPERCEPTRONCLASSIFIER;
            models.put(model, "DNN");
        }
        if (IndicatorMACD.wantL()) {
            int model = IndicatorMACD.LOGISTICREGRESSION;
            models.put(model, "L");
        }
        return models;
    }
*/
    
    private void learntestInner(Map<double[], Double> map, int size, String period, String mapname, int outcomes,
            MLModel model) {
        List<List<Object>> listlist = new ArrayList<>();
        for (double[] key : map.keySet()) {
            List<Object> list = new ArrayList<>();
            list.addAll(Arrays.asList(key));
            list.add(map.get(key));
            listlist.add(list);
        }
        int i = 0;
        Object[][] objobj = new Object[map.size()][];
        Object[] cat = new Object[map.size()];
        for (double[] key : map.keySet()) {
            Object obj[] = new Object[key.length/* + 1*/];
            for (int j = 0; j < key.length; j ++) {
            //System.arraycopy( key, 0, obj, 0, key.length );
                obj[j] = key[j];
            }
            //obj[key.length] = map.get(key);
            cat[i] = map.get(key);
            objobj[i++] = obj;
        }
        LearnTest param = new LearnTest();
        param.array = objobj;
        param.cat = cat;
        param.listlist = listlist;
        param.modelInt = model.getId();
        param.size = size;
        param.period = period;
        param.mapname = mapname;
        param.outcomes = outcomes;
        LearnTest test = EurekaUtil.sendMe(LearnTest.class, param, "http://localhost:8000/learntest");
        System.out.println("test " + test.outcomes);
    }

    @Override
    public Double eval(int modelInt, String period, String mapname) {
        LearnTest param = new LearnTest();
        param.modelInt = modelInt;
        param.period = period;
        param.mapname = mapname;
        LearnTest test = EurekaUtil.sendMe(LearnTest.class, param, "http://localhost:8000/eval");
        return test.prob;
    }

    @Override
    public Map<String, Double[]> classify(Indicator indicator, Map<String, double[]> map, MLModel model, int size,
            String period, String mapname, int outcomes, Map<Double, String> shortMap) {
        Map<Integer, Map<String, Double[]>> retMap = new HashMap<>();
        //List<Integer> models = getModels();
        //for (Integer modelInt : models) {
            if (map.isEmpty()) {
                return new HashMap<>();
                //retMap.put(modelInt, new HashMap<>());
                //continue;
            }
        return classifyInner(map, model, size, period, mapname, outcomes);
        //}
        //return retMap;
    }

    private Map<String, Double[]> classifyInner(Map<String, double[]> map, MLModel model, int size, String period,
            String mapname, int outcomes) {
        List<String> retList = new ArrayList<>();
        LearnTest param = new LearnTest();
        int i = 0;
        Object[][] objobj = new Object[map.size()][];
        for (String key : map.keySet()) {
            double[] value = map.get(key);
            Object obj[] = new Object[value.length/* + 1*/];
            for (int j = 0; j < value.length; j ++) {
            //System.arraycopy( key, 0, obj, 0, key.length );
                obj[j] = value[j];
            }
            //obj[value.length] = map.get(key);
            objobj[i++] = obj;
            retList.add(key);
        }
        param.array = objobj;
        param.modelInt = model.getId();
        param.size = size;
        param.period = period;
        param.mapname = mapname;
        param.outcomes = outcomes;
        for(Object[] obj : objobj) {
        log.info("inner " + Arrays.asList(obj));
        }
        LearnTest ret = EurekaUtil.sendMe(LearnTest.class, param, "http://localhost:8000/classify");
        Object[] cat = ret.cat;
        Map<String, Double[]> retMap = new HashMap<>();
        for (int j = 0; j < retList.size(); j ++) {
            Double acat = new Double((Integer) cat[j]);
            retMap.put(retList.get(j), new Double[]{acat});
        }
        //System.out.println("acat " + retMap);
        return retMap;
    }

 }

