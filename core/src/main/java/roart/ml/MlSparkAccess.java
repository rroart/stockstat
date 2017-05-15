package roart.ml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.db.DbSpark;
import roart.indicator.IndicatorMACD;
import roart.util.Constants;

public class MlSparkAccess extends MlAccess {

	private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void learntest(Map<double[], Double> map, int modelIntDum, int size, String period, String mapname,
            int outcomes) {
        List<Integer> models = getModels();
        for (Integer modelInt : models) {
       DbSpark.learntest(map, modelInt, size, period, mapname, outcomes);       
    }
    }

    @Override
    public Double eval(int modelInt, String period, String mapname) {
        return DbSpark.eval(modelInt, period, mapname);
    }

    @Override
    public Map<Integer, Map<String, Double[]>> classify(Map<String, double[]> map, int modelIntDum, int size, String period,
            String mapname, int outcomes, Map<Double, String> shortMap) {
        Map<Integer, Map<String, Double[]>> retMap = new HashMap<>();
        List<Integer> models = getModels();
        for (Integer modelInt : models) {
         retMap.put(modelInt, DbSpark.classify(map, modelInt, size, period, mapname, outcomes, shortMap));
        }
        return retMap;
    }

    @Override
    public List<Integer> getModels() {
        List<Integer> models = new ArrayList<>();
        if (IndicatorMACD.wantMCP()) {
            int model = IndicatorMACD.MULTILAYERPERCEPTRONCLASSIFIER;
            models.add(model);
        }
        if (IndicatorMACD.wantLR()) {
            int model = IndicatorMACD.LOGISTICREGRESSION;
            models.add(model);
        }
        return models;
    }

    private Map<Integer, String> getModelsMap() {
        Map<Integer, String> models = new HashMap<>();
        if (IndicatorMACD.wantMCP()) {
            int model = IndicatorMACD.MULTILAYERPERCEPTRONCLASSIFIER;
            models.put(model, "MPC");
        }
        if (IndicatorMACD.wantLR()) {
            int model = IndicatorMACD.LOGISTICREGRESSION;
            models.put(model, "LR");
        }
        return models;
    }

    @Override
    public int getSizes() {
        int size = 0;
        if (IndicatorMACD.wantMLHist()) {
            if (IndicatorMACD.wantMCP()) {
                size += 3;
            }
            if (IndicatorMACD.wantLR()) {
                size += 4;
            }
        }
        if (IndicatorMACD.wantMLMacd()) {
            if (IndicatorMACD.wantMCP()) {
                size += 3;
            }
            if (IndicatorMACD.wantLR()) {
                size += 4;
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
            objs[retindex++] = title + Constants.WEBBR + "MPCcomH "+val;
            val = "" + IndicatorMACD.roundme(eval(IndicatorMACD.MULTILAYERPERCEPTRONCLASSIFIER, key, "neg"));
            objs[retindex++] = title + Constants.WEBBR + "MPCposH "+val;
            val = "" + IndicatorMACD.roundme(eval(IndicatorMACD.MULTILAYERPERCEPTRONCLASSIFIER, key, "neg"));
            objs[retindex++] = title + Constants.WEBBR + "MPCnegH "+val;
        }
        if (IndicatorMACD.wantLR() && IndicatorMACD.wantMLHist()) {
            String val = "";
            //String lr = "" + DbSpark.eval("LogisticRegression ", title, "common");
            String lr = "";
            val = "" + IndicatorMACD.roundme(eval(IndicatorMACD.LOGISTICREGRESSION, key, "common"));
            objs[retindex++] = title + Constants.WEBBR + "LRcomH "+val;
            val = "" + IndicatorMACD.roundme(eval(IndicatorMACD.LOGISTICREGRESSION, key, "pos"));
            objs[retindex++] = title + Constants.WEBBR + "LRposH "+val;
            val = "" + IndicatorMACD.roundme(eval(IndicatorMACD.LOGISTICREGRESSION, key, "neg"));
            objs[retindex++] = title + Constants.WEBBR + "LRnegH "+val;
            objs[retindex++] = title + Constants.WEBBR + "LR prob H";
        }
        if (IndicatorMACD.wantMCP() && IndicatorMACD.wantMLMacd()) {
            String mpc = "";
            String val = "";
            //String mpc = "" + DbSpark.eval(MULTILAYERPERCEPTRONCLASSIFIER, title, "common");
            val = "" + IndicatorMACD.roundme(eval(IndicatorMACD.MULTILAYERPERCEPTRONCLASSIFIER, key, "commonM"));
            objs[retindex++] = title + Constants.WEBBR + "MPCcomM "+val;
            val = "" + IndicatorMACD.roundme(eval(IndicatorMACD.MULTILAYERPERCEPTRONCLASSIFIER, key, "negM"));
            objs[retindex++] = title + Constants.WEBBR + "MPCposM "+val;
            val = "" + IndicatorMACD.roundme(eval(IndicatorMACD.MULTILAYERPERCEPTRONCLASSIFIER, key, "negM"));
            objs[retindex++] = title + Constants.WEBBR + "MPCnegM "+val;
        }
        if (IndicatorMACD.wantLR() && IndicatorMACD.wantMLMacd()) {
            String val = "";
            //String lr = "" + DbSpark.eval("LogisticRegression ", title, "common");
            String lr = "";
            val = "" + IndicatorMACD.roundme(eval(IndicatorMACD.LOGISTICREGRESSION, key, "commonM"));
            objs[retindex++] = title + Constants.WEBBR + "LRcomM "+val;
            val = "" + IndicatorMACD.roundme(eval(IndicatorMACD.LOGISTICREGRESSION, key, "posM"));
            objs[retindex++] = title + Constants.WEBBR + "LRposM "+val;
            val = "" + IndicatorMACD.roundme(eval(IndicatorMACD.LOGISTICREGRESSION, key, "negM"));
            objs[retindex++] = title + Constants.WEBBR + "LRnegM "+val;
            objs[retindex++] = title + Constants.WEBBR + "LR prob M ";
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
                if (model == 2) {
                    fields[retindex++] = type != null ? labelMapShort.get(type[1]) : null;                   
                }
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
                if (model == 2) {
                    fields[retindex++] = type != null ? labelMapShort.get(type[1]) : null;                   
                }
            }
        }
        return retindex;
    }
}

