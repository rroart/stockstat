package roart.ml.model;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.util.JsonUtil;
import roart.iclij.config.IclijConfig;
import roart.pipeline.common.predictor.AbstractPredictor;

public abstract class MLPredictModel {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public abstract int getId();

    public abstract String getName();

    public abstract String getKey();
    
    private IclijConfig conf;

    public IclijConfig getConf() {
        return conf;
    }

    public void setConf(IclijConfig conf) {
        this.conf = conf;
    }

    public MLPredictModel(IclijConfig conf) {
        this.conf = conf;
    }

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

    protected <T> T convert(Class<T> clazz) {
        try {
            return JsonUtil.convertnostrip((String) getConf().getConfigData().getConfigValueMap().get(getKey()), clazz);
        } catch (Exception e) {
            log.info(Constants.ERROR);
            return null;
        }
    }

    protected <T> T getDefault(Class<T> clazz) {
        try {
            return JsonUtil.convertnostrip((String) getConf().getConfigData().getConfigMaps().deflt.get(getKey()), clazz);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public abstract NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestPredict param);

}
