package roart.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public static void mapAdd(Map<String, Object[][]> aMap, String id, int index, Object[] value, int length) {
        Object[][] array = aMap.get(id);
        if (array == null) {
            array = new Object[length][2];
            aMap.put(id, array);
        }
        array[index] = value;
    }

    public static <K, V> void mapAddMe(Map<K, List<V>> aMap, K id, V value) {
        List<V> aList = aMap.computeIfAbsent(id, k -> new ArrayList<>());
        aList.add(value);
    }

}
