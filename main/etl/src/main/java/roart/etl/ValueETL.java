package roart.etl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.util.ArraysUtil;

public class ValueETL {

    protected static Logger log = LoggerFactory.getLogger(ValueETL.class);

    public static void zeroPrice(Map<String, Double[][]> aListMap, int category) {
        if (category == Constants.PRICECOLUMN || category == Constants.INDEXVALUECOLUMN) {
            for (Entry<String, Double[][]> entry : aListMap.entrySet()) {
                Double[][] value = entry.getValue();
                for(int i = 0; i < value[0].length; i++) {
                    if (value[0][i] != null && value[0][i] == 0) {
                        log.info("Value 0 for {}", entry.getKey());
                        value[0][i] = null;
                        value[1][i] = null;
                        value[2][i] = null;
                    }
                }
            }
        }
    }

    public static Map<String, Double[][]> getBase100D(Map<String, Double[][]> aListMap) {
        Map<String, Double[][]> aMap = new HashMap<>();
        for (Entry<String, Double[][]> entry : aListMap.entrySet()) {
            Double[][] value = entry.getValue();
            if (value != null) {
                Double[][] newValue = new Double[value.length][];
                for (int i = value.length - 1; i >= 0; i--) {
                    newValue[i] = ArraysUtil.getPercentizedPriceIndex(value[i], value[0]);
                }
                aMap.put(entry.getKey(), newValue);
            }
        }
        return aMap;
    }

    public static Map<String, double[][]> getBase100(Map<String, double[][]> aListMap) {
        Map<String, double[][]> aMap = new HashMap<>();
        for (Entry<String, double[][]> entry : aListMap.entrySet()) {
            double[][] value = entry.getValue();
            if (value != null) {
                double[][] newValue = new double[value.length][];
                double first = value.length > 0 && value[0].length > 0 ? value[0][0] : 0;
                for (int i = 0; i < value.length; i++) {
                    newValue[i] = ArraysUtil.getPercentizedPriceIndex(value[i], first);
                }
                aMap.put(entry.getKey(), newValue);
            }
        }
        return Collections.unmodifiableMap(aMap);
    }

    public static Map<String, Double[][]> getReverseArrSparseFillHolesArr(MyMyConfig conf, Map<String, Double[][]> listMap) {
        Map<String, Double[][]> retMap = new HashMap<>();
        for (Entry<String, Double[][]> entry : listMap.entrySet()) {
            Double[][] array = entry.getValue();
            Double[][] newArray = new Double[array.length][];
            if ("ETLX".equals(entry.getKey())) {
                int jj = 0;
            }
            for (int i = 0; i < array.length; i ++) {
                newArray[i] = new Double[array[i].length];
                String interpolationmethod = conf.getInterpolationmethod();
                boolean acceptLastNull = conf.getInterpolateLastNull();
                newArray[i] = ArraysUtil.fixMapHoles(array[i], newArray[i], maxHoleNumber(conf), interpolationmethod, acceptLastNull);
            }
            retMap.put(entry.getKey(), newArray);
        }
        return Collections.unmodifiableMap(retMap);
    }

    public static Map<String, Double[]> getReverseArrSparseFillHoles(MyMyConfig conf, Map<String, Double[]> listMap) {
        Map<String, Double[]> retMap = new HashMap<>();
        for (Entry<String, Double[]> entry : listMap.entrySet()) {
            Double[] array = entry.getValue();
            Double[] newArray = new Double[array.length];
            String interpolationmethod = conf.getInterpolationmethod();
            boolean acceptLastNull = conf.getInterpolateLastNull();
            newArray = ArraysUtil.fixMapHoles(array, newArray, maxHoleNumber(conf), interpolationmethod, acceptLastNull);
            retMap.put(entry.getKey(), newArray);
        }      
        return retMap;
    }

    public static int maxHoleNumber(MyMyConfig conf) {
        return conf.getMaxHoles();
    }

}
