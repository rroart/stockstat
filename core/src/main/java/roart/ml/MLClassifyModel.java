package roart.ml;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.jfree.util.Log;

import roart.indicator.Indicator;
import roart.indicator.IndicatorMACD;
import roart.util.Constants;

public abstract class MLClassifyModel {
    public abstract int getId();
    
    public abstract String getName();
    
    //public abstract int addTitles(Object[] objs, int retindex, String title, String key, String subType, List<Integer> typeList, Map<Integer, String> mapTypes, MLDao dao);
    //@Override
    public int addTitles(Object[] objs, int retindex, Indicator indicator, String title, String key, String subType, List<Integer> typeList0, Map<Integer, String> mapTypes0, MLClassifyDao dao) {
        List<Integer> typeList = indicator.getTypeList();
        Map<Integer, String> mapTypes = indicator.getMapTypes();
       for (int mapTypeInt : typeList) {
             String mapType = mapTypes.get(mapTypeInt);
            String val = "";
            //String lr = "" + DbSpark.eval("LogisticRegression ", title, "common");
            String lr = "";
            // TODO workaround
            try {
            val = "" + roundme(dao.eval(getId(), key, subType + mapType));
            } catch (Exception e) {
                Log.error("Exception fix later, refactor", e);
            }
            objs[retindex++] = title + Constants.WEBBR +  subType + getName() + mapType +val;
        }
        return retindex;
    }
    
    //public int addResults(Object[] fields, int retindex, String id, Indicator indicator, Map<Integer, Map<String, Double[]>> commonIdTypeModelHistMap,String subType, List<Integer> typeList0, Map<Integer, String> mapTypes0 ) {
        public int addResults(Object[] fields, int retindex, String id, MLClassifyModel model, Indicator indicator, Map<String, Map<String, Double[]>> mapResult, Map<Double, String> labelMapShort) {
        List<Integer> typeList = indicator.getTypeList();
        Map<Integer, String> mapTypes = indicator.getMapTypes();
        for (int mapTypeInt : typeList) {
            String mapType = mapTypes.get(mapTypeInt);
            Map<String, Double[]> resultMap1 = mapResult.get(mapType);
            Double[] type = null;
            if (resultMap1 != null) {
                resultMap1.get(id);
            } else {
                System.out.println("map null " + mapType);
            }
            fields[retindex++] = type != null ? labelMapShort.get(type[0]) : null;
        }
        return retindex;
 }

    public static String roundme(Double eval) {
        if (eval == null) {
            return null;
        }
        DecimalFormat df = new DecimalFormat("#.00");
        return df.format(eval);
    }

    public int getSizes(Indicator indicator) {
        List<Integer> typeList = indicator.getTypeList();
        if (typeList == null) {
            return 0;
        }
        return typeList.size();
    }

    public abstract String getEngineName();

}
