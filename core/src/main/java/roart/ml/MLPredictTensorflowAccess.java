package roart.ml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.config.MyConfig;
import roart.model.LearnTestPredict;
import roart.predictor.Predictor;
import roart.ml.MLClassifyModel;
import roart.ml.MLClassifyTensorflowDNNModel;
import roart.ml.MLClassifyTensorflowLModel;
import roart.util.Constants;
import roart.util.EurekaUtil;

public class MLPredictTensorflowAccess extends MLPredictAccess {

	private Logger log = LoggerFactory.getLogger(this.getClass());

    private MyConfig conf;
    
	public MLPredictTensorflowAccess(MyConfig conf) {
        this.conf = conf;
	    findModels();
	}
	
	private void findModels() {
        models = new ArrayList<>();
        if (conf.wantDNN()) {
            MLPredictModel model = new MLPredictTensorflowLSTMModel();
            models.add(model);
        }
        if (conf.wantL()) {
            //MLModel2 model = null; //new MLTensorflowLModel();
            //models.add(model);
        }	    
	}
	
    @Override
    public LearnTestPredict learntestpredict(Predictor predictor, Double[] list, List<Double> next, Map<double[], Double> map, MLPredictModel model, int size, String period, String mapname,
            int outcomes, int windowsize, int horizon, int epochs) {
        //List<MLModel> models = getModels();
        //for (MLModel modelInt : models) {
        return learntestInner(list, next, map, size, period, mapname, outcomes, model, horizon, epochs, epochs);
    //}
    }

    @Override
    public List<MLPredictModel> getModels() {
        return models;
    }

    /*
    private Map<Integer, String> getModelsMap() {
        Map<Integer, String> models = new HashMap<>();
        if (PredictorMACD.wantDNN()) {
            int model = PredictorMACD.MULTILAYERPERCEPTRONCLASSIFIER;
            models.put(model, "DNN");
        }
        if (PredictorMACD.wantL()) {
            int model = PredictorMACD.LOGISTICREGRESSION;
            models.put(model, "L");
        }
        return models;
    }
*/
    
    private LearnTestPredict learntestInner(Double[] list, List<Double> next, Map<double[], Double> map, int size, String period, String mapname, int outcomes,
            MLPredictModel model, int windowsize,int horizon, int epochs) {
        /*
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
            Object obj[] = new Object[key.length];
            for (int j = 0; j < key.length; j ++) {
            //System.arraycopy( key, 0, obj, 0, key.length );
                obj[j] = key[j];
            }
            //obj[key.length] = map.get(key);
            cat[i] = map.get(key);
            objobj[i++] = obj;
        }
    */
        LearnTestPredict param = new LearnTestPredict();
        //param.array = objobj;
        //System.out.println("cats " + cat.length + " " + Arrays.asList(cat));
        //param.cat = cat;
        //param.listlist = listlist;
        param.modelInt = model.getId();
        param.size = size;
        param.period = period;
        param.mapname = mapname;
        param.outcomes = outcomes;
        param.array = list;
        param.windowsize = windowsize;
        param.horizon = horizon;
        param.epochs = epochs;
        //param.next = next;
        log.info("evalin " + param.modelInt + period + mapname);
        LearnTestPredict test = EurekaUtil.sendMe(LearnTestPredict.class, param, "http://localhost:8001/learntestpredict");
        //System.out.println("test " + test.outcomes);
        return test;
    }

    @Override
    public Double eval(int modelInt, String period, String mapname) {
        LearnTestPredict param = new LearnTestPredict();
        param.modelInt = modelInt;
        param.period = period;
        param.mapname = mapname;
        log.info("evalout " + modelInt + period + mapname);
        LearnTestPredict test = EurekaUtil.sendMe(LearnTestPredict.class, param, "http://localhost:8000/eval");
        return test.prob;
    }

    @Override
    public Map<String, Double[]> predict(Predictor indicator, Map<String, double[]> map, MLPredictModel model, int size,
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

    private Map<String, Double[]> classifyInner(Map<String, double[]> map, MLPredictModel model, int size, String period,
            String mapname, int outcomes) {
        List<String> retList = new ArrayList<>();
        LearnTestPredict param = new LearnTestPredict();
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
        LearnTestPredict ret = EurekaUtil.sendMe(LearnTestPredict.class, param, "http://localhost:8000/classify");
        Object[] cat = ret.cat;
        Map<String, Double[]> retMap = new HashMap<>();
        for (int j = 0; j < retList.size(); j ++) {
            Double acat = new Double((Integer) cat[j]);
            retMap.put(retList.get(j), new Double[]{acat});
        }
        //System.out.println("acat " + retMap.size());
        return retMap;
    }

 }

