package roart.ml;

import java.util.List;
import java.util.Map;

import org.apache.spark.ml.Model;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import roart.aggregate.Aggregator;
import roart.indicator.Indicator;
import roart.indicator.IndicatorMACD;
import roart.util.Constants;

public class MLClassifySparkLRModel  extends MLClassifySparkModel {
    @Override
    public int getId() {
        return 2;
    }
    @Override
    public String getName() {
        return "LR";
    }

    @Deprecated
    @Override
    public int addTitles(Object[] objs, int retindex, Aggregator indicator, String title, String key, String subType, List<Integer> typeList0, Map<Integer, String> mapTypes0, MLClassifyDao dao) {
        if (true) return retindex;
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
            objs[retindex++] = title + Constants.WEBBR +  subType + getName() + mapType + " prob ";
        }
        return retindex;
    }

    @Deprecated
    @Override
    public int addResults(Object[] fields, int retindex, String id, MLClassifyModel model, Aggregator indicator, Map<String, Map<String, Double[]>> mapResult, Map<Double, String> labelMapShort) {
    //        public int addResults(Object[] fields, int retindex, String id, Map<Integer, Map<String, Double[]>> commonIdTypeModelHistMap, String subType, List<Integer> typeList, Map<Integer, String> mapTypes) {
        if (true) return retindex;
        retindex = super.addResults(fields, retindex, id, model, indicator, mapResult, labelMapShort);
        //fields[retindex++] = type != null ? labelMapShort.get(type[1]) : null;                   
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
            
            fields[retindex++] = type != null ? type[1] : null;
        }
                
                return retindex;
     }
    
    @Override
    public int getSizes(Aggregator indicator) { 
        return 2 * super.getSizes(indicator);
    }
    
    @Override
    public int getReturnSize() {
        return 2;
    }
    
    @Override
    public Model getModel(Dataset<Row> train, int size, int outcomes) {
        LogisticRegression reg = new LogisticRegression();
        //reg.setLabelCol("label");
        reg.setMaxIter(5);
        reg.setRegParam(0.01);
        Model model = reg.fit(train);
        return model;
    }

}

