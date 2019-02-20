package roart.ml.common;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.SparkOVRConfig;
import roart.pipeline.common.aggregate.Aggregator;

public abstract class MLClassifyModel {
    
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private MyMyConfig conf;

    public MyMyConfig getConf() {
        return conf;
    }

    public void setConf(MyMyConfig conf) {
        this.conf = conf;
    }

    public MLClassifyModel(MyMyConfig conf) {
        this.conf = conf;
    }

    public abstract int getId();

    public abstract String getName();

    public abstract String getKey();
    
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

    public static void mapAdder(Map<MLClassifyModel, Long> map, MLClassifyModel key, Long add) {
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
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    protected <T> T getDefault(Class<T> clazz) {
        try {
            return new ObjectMapper().readValue((String) getConf().getDeflt().get(getKey()), clazz);
        } catch (IOException e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

}
