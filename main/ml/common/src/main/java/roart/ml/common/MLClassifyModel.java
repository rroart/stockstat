package roart.ml.common;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.pipeline.common.aggregate.Aggregator;

public abstract class MLClassifyModel {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    public abstract int getId();

    public abstract String getName();

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

}
