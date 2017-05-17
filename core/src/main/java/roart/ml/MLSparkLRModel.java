package roart.ml;

import java.util.List;
import java.util.Map;

import roart.indicator.Indicator;
import roart.indicator.IndicatorMACD;
import roart.util.Constants;

public class MLSparkLRModel  extends MLSparkModel {
    public int id = 2;
    public String name = "LR";

    @Override
    public int addTitles(Object[] objs, int retindex, Indicator indicator, String title, String key, String subType, List<Integer> typeList0, Map<Integer, String> mapTypes0, MLDao dao) {
        retindex = super.addTitles(objs, retindex, indicator,title, key, subType, typeList0, mapTypes0, dao);
        //objs[retindex++] = title + Constants.WEBBR + subType + name + " prob";
        List<Integer> typeList = indicator.getTypeList();
        Map<Integer, String> mapTypes = indicator.getMapTypes();
       for (int mapTypeInt : typeList) {
             String mapType = mapTypes.get(mapTypeInt);
            String val = "";
            //String lr = "" + DbSpark.eval("LogisticRegression ", title, "common");
            String lr = "";
            //val = "" + roundme(dao.eval(id, key, mapType));
            objs[retindex++] = title + Constants.WEBBR +  subType + name + mapType + " prob ";
        }
        return retindex;
    }

    @Override
    public int addResults(Object[] fields, int retindex, String id, MLModel model, Indicator indicator, Map<String, Map<String, Double[]>> mapResult, Map<Double, String> labelMapShort) {
    //        public int addResults(Object[] fields, int retindex, String id, Map<Integer, Map<String, Double[]>> commonIdTypeModelHistMap, String subType, List<Integer> typeList, Map<Integer, String> mapTypes) {
        retindex = super.addResults(fields, retindex, id, model, indicator, mapResult, labelMapShort);
        //fields[retindex++] = type != null ? labelMapShort.get(type[1]) : null;                   
        List<Integer> typeList = indicator.getTypeList();
        Map<Integer, String> mapTypes = indicator.getMapTypes();
        for (int mapTypeInt : typeList) {
            String mapType = mapTypes.get(mapTypeInt);
            Map<String, Double[]> resultMap1 = mapResult.get(mapType);
            Double[] type = resultMap1.get(id);
            fields[retindex++] = type != null ? labelMapShort.get(type[1]) : null;
        }
                
                return retindex;
     }
    
    @Override
    public int getSizes(Indicator indicator) { 
        return 2 * super.getSizes(indicator);
    }
}
