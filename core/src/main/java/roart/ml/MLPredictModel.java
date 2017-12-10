package roart.ml;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.jfree.util.Log;

import roart.model.LearnTestPredict;
import roart.predictor.Predictor;
import roart.util.Constants;

public abstract class MLPredictModel {
    public abstract int getId();
    
    public abstract String getName();
    
    //public abstract int addTitles(Object[] objs, int retindex, String title, String key, String subType, List<Integer> typeList, Map<Integer, String> mapTypes, MLDao dao);
    //@Override
    public int addTitles(Object[] objs, int retindex, Predictor indicator, String title, String key, String subType, List<Integer> typeList0, Map<Integer, String> mapTypes0, MLPredictDao dao) {
        List<Integer> typeList = indicator.getTypeList();
        Map<Integer, String> mapTypes = indicator.getMapTypes();
       for (int mapTypeInt : typeList) {
             String mapType = mapTypes.get(mapTypeInt);
            String val = "";
            //String lr = "" + DbSpark.eval("LogisticRegression ", title, "common");
            String lr = "";
            val = ""; // + roundme(dao.eval(getId(), key, subType + mapType));
            objs[retindex++] = title + Constants.WEBBR +  subType + getName() + mapType +val;
        }
        return retindex;
    }
    
    //public int addResults(Object[] fields, int retindex, String id, Predictor indicator, Map<Integer, Map<String, Double[]>> commonIdTypeModelHistMap,String subType, List<Integer> typeList0, Map<Integer, String> mapTypes0 ) {
        public int addResults(Object[] fields, int retindex, String id, MLPredictModel model, Predictor indicator, Map<String, LearnTestPredict> mapResult, Map<Double, String> labelMapShort) {
        List<Integer> typeList = indicator.getTypeList();
        //Map<Integer, String> mapTypes = indicator.getMapTypes();
        //for (int mapTypeInt : typeList) {
           // String mapType = mapTypes.get(mapTypeInt);
            LearnTestPredict predict = mapResult.get(id);
            Double val = null;
            if (predict != null) {
                Double[] predictions = predict.predicted;
                if (predictions != null) {
                    int size = predictions.length;
                    val = predictions[size - 1];
                    System.out.println("setting val " + val);
                }
            }
            System.out.println("here set retindex val id " + retindex + " " + val + " " + id);
            fields[retindex++] = val;
        //}
        return retindex;
 }

    public static String roundme(Double eval) {
        if (eval == null) {
            return null;
        }
        DecimalFormat df = new DecimalFormat("#.00");
        return df.format(eval);
    }

    public int getSizes(Predictor indicator) {
        List<Integer> typeList = indicator.getTypeList();
        if (typeList == null) {
            return 0;
        }
        return typeList.size();
    }

    public abstract String getEngineName();

}
