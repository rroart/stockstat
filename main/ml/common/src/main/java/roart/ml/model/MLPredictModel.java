package roart.ml.model;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import roart.common.constants.Constants;
import roart.pipeline.common.predictor.AbstractPredictor;

public abstract class MLPredictModel {
    public abstract int getId();

    public abstract String getName();

    //@Override
    public int addTitles(Object[] objs, int retindex, AbstractPredictor indicator, String title, String key, String subType, List<Integer> typeList0, Map<Integer, String> mapTypes0, String dao) {
        List<Integer> typeList = indicator.getTypeList();
        Map<Integer, String> mapTypes = indicator.getMapTypes();
        for (int mapTypeInt : typeList) {
            String mapType = mapTypes.get(mapTypeInt);
            objs[retindex++] = title + Constants.WEBBR +  subType + getName() + mapType;
        }
        return retindex;
    }

    public int addResults(Object[] fields, int retindex, String id, MLPredictModel model, AbstractPredictor indicator, Map<String, Double[]> mapResult, Map<Double, String> labelMapShort) {
        Double[] predictions = mapResult.get(id);
        Double val = null;
        if (predictions != null) {
            int size = predictions.length;
            val = predictions[size - 1];
        }
        fields[retindex++] = val;
        return retindex;
    }

    public static String roundme(Double eval) {
        if (eval == null) {
            return null;
        }
        DecimalFormat df = new DecimalFormat("#.00");
        return df.format(eval);
    }

    public int getSizes(AbstractPredictor indicator) {
        List<Integer> typeList = indicator.getTypeList();
        if (typeList == null) {
            return 0;
        }
        return typeList.size();
    }

    public abstract String getEngineName();

    public static void mapAdder(Map<MLPredictModel, Long> map, MLPredictModel key, Long add) {
        Long val = map.get(key);
        if (val == null) {
            val = Long.valueOf(0);
        }
        val += add;
        map.put(key, val);
    }

}
