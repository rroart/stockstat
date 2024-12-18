package roart.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.SerialVolume;

public class MapUtil {

    /**
     * The id is mapped to a list, and the value is added to that list
     * 
     * @param aMap the map
     * @param id Map id
     * @param value value to map
     */
    
    public static void mapAdd(Map<String, List<Double>> aMap, String id, Double value) {
        List<Double> aList = aMap.computeIfAbsent(id, k -> new ArrayList<>());
        aList.add(value);
    }

    public static void mapAdd(Map<String, List<Double>[]> aMap, String id, Double[] value) {
        List<Double>[] aList = aMap.get(id);
        for (int i = 0; i < value.length; i++) {
            if (aList == null) {
                aList = new ArrayList[3];
                aMap.put(id, aList);
            }
            aList[i].add(value[i]);
        }
    }

    public static void mapAdd(Map<String, Double[]> aMap, String id, int index, Double value, int length) {
        Double[] array = aMap.computeIfAbsent(id, k -> new Double[length]);
        array[index] = value;
    }

    public static void mapAdd(Map<String, Double[][]> aMap, String id, int index, Double[] value, int length) {
        Double[][] array = aMap.get(id);
        for (int i = 0; i < value.length; i++) {
            if (array == null) {
                array = new Double[value.length][length];
                aMap.put(id, array);
            }
            array[i][index] = value[i];
        }
    }

    public static void mapAdd(Map<String, SerialVolume[]> aMap, String id, int index, SerialVolume value, int length) {
        SerialVolume[] array = aMap.get(id);
        if (array == null) {
            array = new SerialVolume[length];
            aMap.put(id, array);
        }
        array[index] = value;
    }

    public static <K, V> void mapAddMe(Map<K, List<V>> aMap, K id, V value) {
        List<V> aList = aMap.computeIfAbsent(id, k -> new ArrayList<>());
        aList.add(value);
    }

    public static Map<String, Double[][]> convert(Map<String, List<List<Double>>> map) {
        Map newMap = new HashMap<>();
        for (Entry<String, List<List<Double>>> entry : map.entrySet()) {
            newMap.put(entry.getKey(), ArraysUtil.convert(entry.getValue()));
        }
        return newMap;
    }

    public static Map<String, List<List<Double>>> convertA2L(Map<String, Double[][]> map) {
        Map<String, List<List<Double>>> newMap = new HashMap<>();
        if (map == null) {
            return newMap;
        }
        for (Entry<String, Double[][]> entry : map.entrySet()) {
            newMap.put(entry.getKey(), ArraysUtil.convertA2L(entry.getValue()));
        }
        return newMap;
    }

}
