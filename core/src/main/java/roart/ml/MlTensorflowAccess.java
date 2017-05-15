package roart.ml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.indicator.IndicatorMACD;
import roart.model.LearnTest;
import roart.util.Constants;
import roart.util.EurekaUtil;

public class MlTensorflowAccess extends MlAccess {

	private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void learntest(Map<double[], Double> map, int modelIntDummy, int size, String period, String mapname,
            int outcomes) {
        List<Integer> models = getModels();
        for (Integer modelInt : models) {
        learntestInner(map, size, period, mapname, outcomes, modelInt);
    }
    }

    @Override
    public List<Integer> getModels() {
        List<Integer> models = new ArrayList<>();
        if (IndicatorMACD.wantDNN()) {
            int model = IndicatorMACD.MULTILAYERPERCEPTRONCLASSIFIER;
            models.add(model);
        }
        if (IndicatorMACD.wantL()) {
            int model = IndicatorMACD.LOGISTICREGRESSION;
            models.add(model);
        }
        return models;
    }

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

    private void learntestInner(Map<double[], Double> map, int size, String period, String mapname, int outcomes,
            Integer modelInt) {
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
        param.modelInt = modelInt;
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
    public Map<Integer, Map<String, Double[]>> classify(Map<String, double[]> map, int modelIntDummy, int size, String period,
            String mapname, int outcomes, Map<Double, String> shortMap) {
        Map<Integer, Map<String, Double[]>> retMap = new HashMap<>();
        List<Integer> models = getModels();
        for (Integer modelInt : models) {
            if (map.isEmpty()) {
                retMap.put(modelInt, new HashMap<>());
                continue;
            }
        retMap.put(modelInt, classifyInner(map, modelInt, size, period, mapname, outcomes));
        }
        return retMap;
    }

    private Map<String, Double[]> classifyInner(Map<String, double[]> map, int modelInt, int size, String period,
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
        param.modelInt = modelInt;
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

    @Override
    public int getSizes() {
        int size = 0;
        if (IndicatorMACD.wantMLHist()) {
            if (IndicatorMACD.wantDNN()) {
                size += 3;
            }
            if (IndicatorMACD.wantL()) {
                size += 3;
            }
        }
        if (IndicatorMACD.wantMLMacd()) {
            if (IndicatorMACD.wantDNN()) {
                size += 3;
            }
            if (IndicatorMACD.wantL()) {
                size += 3;
            }
        }
        // TODO Auto-generated method stub
        return size;
    }

    @Override
    public int addTitles(Object[] objs, int retindex, String title, String key) {
        if (IndicatorMACD.wantMCP() && IndicatorMACD.wantMLHist()) {
            String mpc = "";
            String val = "";
            //String mpc = "" + DbSpark.eval(MULTILAYERPERCEPTRONCLASSIFIER, title, "common");
            val = "" + IndicatorMACD.roundme(eval(IndicatorMACD.MULTILAYERPERCEPTRONCLASSIFIER, key, "common"));
            objs[retindex++] = title + Constants.WEBBR + "DNNcomH "+val;
            val = "" + IndicatorMACD.roundme(eval(IndicatorMACD.MULTILAYERPERCEPTRONCLASSIFIER, key, "neg"));
            objs[retindex++] = title + Constants.WEBBR + "DNNposH "+val;
            val = "" + IndicatorMACD.roundme(eval(IndicatorMACD.MULTILAYERPERCEPTRONCLASSIFIER, key, "neg"));
            objs[retindex++] = title + Constants.WEBBR + "DNNnegH "+val;
        }
        if (IndicatorMACD.wantLR() && IndicatorMACD.wantMLHist()) {
            String val = "";
            //String lr = "" + DbSpark.eval("LogisticRegression ", title, "common");
            String lr = "";
            val = "" + IndicatorMACD.roundme(eval(IndicatorMACD.LOGISTICREGRESSION, key, "common"));
            objs[retindex++] = title + Constants.WEBBR + "LcomH "+val;
            val = "" + IndicatorMACD.roundme(eval(IndicatorMACD.LOGISTICREGRESSION, key, "pos"));
            objs[retindex++] = title + Constants.WEBBR + "LposH "+val;
            val = "" + IndicatorMACD.roundme(eval(IndicatorMACD.LOGISTICREGRESSION, key, "neg"));
            objs[retindex++] = title + Constants.WEBBR + "LnegH "+val;
            //objs[retindex++] = title + Constants.WEBBR + "LR prob H";
        }
        if (IndicatorMACD.wantMCP() && IndicatorMACD.wantMLMacd()) {
            String mpc = "";
            String val = "";
            //String mpc = "" + DbSpark.eval(MULTILAYERPERCEPTRONCLASSIFIER, title, "common");
            val = "" + IndicatorMACD.roundme(eval(IndicatorMACD.MULTILAYERPERCEPTRONCLASSIFIER, key, "commonM"));
            objs[retindex++] = title + Constants.WEBBR + "DNNcomM "+val;
            val = "" + IndicatorMACD.roundme(eval(IndicatorMACD.MULTILAYERPERCEPTRONCLASSIFIER, key, "negM"));
            objs[retindex++] = title + Constants.WEBBR + "DNNposM "+val;
            val = "" + IndicatorMACD.roundme(eval(IndicatorMACD.MULTILAYERPERCEPTRONCLASSIFIER, key, "negM"));
            objs[retindex++] = title + Constants.WEBBR + "DNNnegM "+val;
        }
        if (IndicatorMACD.wantLR() && IndicatorMACD.wantMLMacd()) {
            String val = "";
            //String lr = "" + DbSpark.eval("LogisticRegression ", title, "common");
            String lr = "";
            val = "" + IndicatorMACD.roundme(eval(IndicatorMACD.LOGISTICREGRESSION, key, "commonM"));
            objs[retindex++] = title + Constants.WEBBR + "LcomM "+val;
            val = "" + IndicatorMACD.roundme(eval(IndicatorMACD.LOGISTICREGRESSION, key, "posM"));
            objs[retindex++] = title + Constants.WEBBR + "LposM "+val;
            val = "" + IndicatorMACD.roundme(eval(IndicatorMACD.LOGISTICREGRESSION, key, "negM"));
            objs[retindex++] = title + Constants.WEBBR + "LnegM "+val;
            //objs[retindex++] = title + Constants.WEBBR + "LR prob M ";
        }
        return retindex;
    }
    @Override
    public int addResults(Object[] fields, int retindex, String id, Map<Integer, Map<String, Double[]>> commonIdTypeModelHistMap,
            Map<Integer, Map<String, Double[]>> posIdTypeModelHistMap,
            Map<Integer, Map<String, Double[]>> negIdTypeModelHistMap,
            Map<Integer, Map<String, Double[]>> commonIdTypeModelMacdMap,
            Map<Integer, Map<String, Double[]>> posIdTypeModelMacdMap,
            Map<Integer, Map<String, Double[]>> negIdTypeModelMacdMap) {
        List<Integer> models = getModels();
        Map<Double, String> labelMapShort = IndicatorMACD.createLabelMapShort();
        Double[] type = null;
        for (Integer model : models) {
            if (IndicatorMACD.wantMLHist()) {
                Map<String, Double[]> commonIdTypeHistMap = commonIdTypeModelHistMap.get(model);
                Map<String, Double[]> posIdTypeHistMap = posIdTypeModelHistMap.get(model);
                Map<String, Double[]> negIdTypeHistMap = negIdTypeModelHistMap.get(model);

                type = commonIdTypeHistMap.get(id);
                fields[retindex++] = type != null ? labelMapShort.get(type[0]) : null;
                IndicatorMACD.printout(type, id, labelMapShort);
                type = posIdTypeHistMap.get(id);
                fields[retindex++] = type != null ? labelMapShort.get(type[0]) : null;
                IndicatorMACD.printout(type, id, labelMapShort);
                type = negIdTypeHistMap.get(id);
                fields[retindex++] = type != null ? labelMapShort.get(type[0]) : null;
                IndicatorMACD.printout(type, id, labelMapShort);
            }
            if (IndicatorMACD.wantMLMacd()) {
                Map<String, Double[]> commonIdTypeMacdMap = commonIdTypeModelMacdMap.get(model);
                Map<String, Double[]> posIdTypeMacdMap = posIdTypeModelMacdMap.get(model);
                Map<String, Double[]> negIdTypeMacdMap = negIdTypeModelMacdMap.get(model);
                type = commonIdTypeMacdMap.get(id);
                fields[retindex++] = type != null ? labelMapShort.get(type[0]) : null;
                IndicatorMACD.printout(type, id, labelMapShort);
                type = posIdTypeMacdMap.get(id);
                fields[retindex++] = type != null ? labelMapShort.get(type[0]) : null;
                IndicatorMACD.printout(type, id, labelMapShort);
                type = negIdTypeMacdMap.get(id);
                fields[retindex++] = type != null ? labelMapShort.get(type[0]) : null;
                IndicatorMACD.printout(type, id, labelMapShort);
            }
        }
        return retindex;
    }
}

