package roart.ml;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.spark.ml.Model;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import roart.aggregate.Aggregator;
import roart.config.MLConstants;
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
        return MLConstants.LR;
    }

    @Deprecated
    @Override
    public int addTitles(Object[] objs, int retindex, Aggregator indicator, String title, String key, String subType, List<Integer> typeList0, Map<Integer, String> mapTypes0, MLClassifyDao dao) {
        if (true) return retindex;
        retindex = super.addTitles(objs, retindex, indicator,title, key, subType, typeList0, mapTypes0, dao);
        List<Integer> typeList = indicator.getTypeList();
        Map<Integer, String> mapTypes = indicator.getMapTypes();
        for (int mapTypeInt : typeList) {
            String mapType = mapTypes.get(mapTypeInt);
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
        List<Integer> typeList = indicator.getTypeList();
        Map<Integer, String> mapTypes = indicator.getMapTypes();
        for (int mapTypeInt : typeList) {
            String mapType = mapTypes.get(mapTypeInt);
            Map<String, Double[]> resultMap1 = mapResult.get(mapType);
            Double[] type = null;
            if (resultMap1 != null) {
                type = resultMap1.get(id);
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
    public Model getModel(NNConfigs conf, Dataset<Row> train, int size, int outcomes) {
        SparkLRConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getSparkLRConfig();
        }    
        if (modelConf == null) {
            modelConf = new SparkLRConfig(5, 0.01, null);
        }
        LogisticRegression reg = new LogisticRegression();
        reg.setMaxIter(modelConf.getMaxiter());
        reg.setRegParam(modelConf.getReg());
        if (modelConf.getElasticnet() != null) {
            reg.setElasticNetParam(modelConf.getElasticnet());
        }
        log.info("Used ML config {}", modelConf);
        return reg.fit(train);
    }

}

