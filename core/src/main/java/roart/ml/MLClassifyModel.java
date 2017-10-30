package roart.ml;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.aggregate.Aggregator;
import roart.indicator.Indicator;
import roart.indicator.IndicatorMACD;
import roart.util.Constants;

public abstract class MLClassifyModel {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    public abstract int getId();
    
    public abstract String getName();
    
    //public abstract int addTitles(Object[] objs, int retindex, String title, String key, String subType, List<Integer> typeList, Map<Integer, String> mapTypes, MLDao dao);
    //@Override
    @Deprecated
    public int addTitles(Object[] objs, int retindex, Aggregator indicator, String title, String key, String subType, List<Integer> typeList0, Map<Integer, String> mapTypes0, MLClassifyDao dao) {
        if (true) return retindex;
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
                log.error("Exception fix later, refactor", e);
            }
            objs[retindex++] = title + Constants.WEBBR +  subType + getName() + mapType +val;
        }
        return retindex;
    }
    
    @Deprecated
    //public int addResults(Object[] fields, int retindex, String id, Aggregator indicator, Map<Integer, Map<String, Double[]>> commonIdTypeModelHistMap,String subType, List<Integer> typeList0, Map<Integer, String> mapTypes0 ) {
        public int addResults(Object[] fields, int retindex, String id, MLClassifyModel model, Aggregator indicator, Map<String, Map<String, Double[]>> mapResult, Map<Double, String> labelMapShort) {
        if (true) return retindex;
        List<Integer> typeList = indicator.getTypeList();
        Map<Integer, String> mapTypes = indicator.getMapTypes();
        for (int mapTypeInt : typeList) {
            String mapType = mapTypes.get(mapTypeInt);
            Map<String, Double[]> resultMap1 = mapResult.get(mapType);
            Double[] type = null;
            if (resultMap1 != null) {
                type = resultMap1.get(id);
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

    public int getSizes(Aggregator indicator) {
        List<Integer> typeList = indicator.getTypeList();
        if (typeList == null) {
            return 0;
        }
        return typeList.size();
    }

     public int getReturnSize() {
        return 1;
    }
    
    public abstract String getEngineName();

}
