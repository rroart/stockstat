package roart.ml.model;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.pipeline.common.predictor.AbstractPredictor;

public abstract class MLPredictModel {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public abstract int getId();

    public abstract String getName();

    public abstract String getKey();
    
    private MyMyConfig conf;

    public MyMyConfig getConf() {
        return conf;
    }

    public void setConf(MyMyConfig conf) {
        this.conf = conf;
    }

    public MLPredictModel(MyMyConfig conf) {
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
            return new ObjectMapper().readValue((String) getConf().getConfigValueMap().get(getKey()), clazz);
        } catch (Exception e) {
            log.info(Constants.ERROR);
            return null;
        }
    }

    protected <T> T getDefault(Class<T> clazz) {
        try {
            return new ObjectMapper().readValue((String) getConf().getConfigMaps().deflt.get(getKey()), clazz);
        } catch (IOException e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public abstract NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestPredict param);

}
