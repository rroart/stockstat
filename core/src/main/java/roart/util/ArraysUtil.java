package roart.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.category.CategoryConstants;

public class ArraysUtil {

    private static Logger log = LoggerFactory.getLogger(ArraysUtil.class);

    private int searchBackwardNegative(double[] array, int i) {
        while (i >= 0 && array[i] >= 0) {
            i--;
        }
        return i;
    }

    private static int searchForwardNegative(double[] array, int i) {
        while (i < array.length && array[i] >= 0) {
            i++;
        }
        return i;
    }

    private static int searchForwardNegative(double[] array, int i, int length) {
        while (i < length && array[i] >= 0) {
            i++;
        }
        return i;
    }

    private int searchBackwardPositive(double[] array, int i) {
        while (i >= 0 && array[i] < 0) {
            i--;
        }
        return i;
    }

    private static int searchForwardPositive(double[] array, int i) {
        while (i < array.length && array[i] < 0)  {
            i++;
        }
        return i;
    }

    private static int searchForwardPositive(double[] array, int i, int length) {
        while (i < length && array[i] < 0)  {
            i++;
        }
        return i;
    }

   /**
     * This will search an array, and return the gruoupings of positive and negative numbers
     * This returns an array of two maps, the first is for positive, the second for negative ranges.
     * The key is low value, while the value is the high value.
     * 
     * @param array
     * @return a resulting array
     */
    
    public static Map<Integer, Integer>[] searchForward(double[] array, int maxlen) {
        int length = array.length;
        if (maxlen > 0) {
            length = maxlen;
        }
        Map<Integer, Integer>[] retmap = new HashMap[2];
        retmap[0] = new HashMap<>();
        retmap[1] = new HashMap<>();
        double prevval = array[0];
        int prev = 0;
        for (int i = 0; i < length; i++) {
            if (prevval >= 0) {
                i = searchForwardNegative(array, i, length);
                retmap[0].put(prev, i - 1);
                if (i < length) {
                    prevval = array[i];
                }
                prev = i;
           } else {
                i = searchForwardPositive(array, i, length);                       
                retmap[1].put(prev, i - 1);
                if (i < length) {
                    prevval = array[i];
                }
                prev = i;
            }
        }
        return retmap;
    }
    
    private Map<Integer, Integer>[] searchBackward(double[] array) {
        Map<Integer, Integer>[] retmap = new HashMap[2];
        retmap[0] = new HashMap<>();
        retmap[1] = new HashMap<>();
        double prevval = array[array.length - 1];
        int prev = array.length - 1;
        for (int i = array.length - 1; i >=0; i--) {
            if (prev >= 0) {
                i = searchBackwardNegative(array, i);
                retmap[0].put(prev, i);
            } else {
                i = searchBackwardPositive(array, i);                       
            }
        }        
        return retmap;
    }

    /**
     * Pick the first non-null element in the array, and find the
     * increases/decreases since that. ( * 100 / first)
     * @param list
     * @param key whether price/index, if not, then skip
     * @return new array
     */
    
    public static Double[] getPercentizedPriceIndex(Double[] list, String key) {
        if (list == null) {
            return list;
        }
        if ((CategoryConstants.INDEX).equals(key) || (CategoryConstants.PRICE).equals(key)) {
            int i = 0;
            double first = 0;
            for (i = 0; i < list.length; i ++) {
                if (list[i] != null) {
                    first = list[i];
                    break;
                }
            }
            for (; i < list.length; i ++) {
                if (list[i] != null) {
                    list[i] *= 100 / first;
                }
            }
        }
        return list;
    }

    public static double[] getPercentizedPriceIndex(double[] list, String key, int category) {
        if (list == null || list.length == 0) {
            return list;
        }
        if (category < 0 || (CategoryConstants.INDEX).equals(key) || (CategoryConstants.PRICE).equals(key) || ("cy").equals(key)) {
            int i = 0;
            double first = 0;
            first = list[i];
            for (; i < list.length; i ++) {
                list[i] *= 100 / first;
            }
        }
        return list;
    }

    public static boolean verifyPercentized(Double[] list, String key) {
        if ((CategoryConstants.INDEX).equals(key) || (CategoryConstants.PRICE).equals(key)) {
            if (list != null) {
                if (list[0] == 100.0) {
                    return true;
                } else {
                    System.out.println("Not rescaled");
                    log.error("Not rescaled");
                    return false;
                }
            }
        }
        return true;
    }
    
    private static int getArrayNonNullReversenot(List<Double> list, double[] values) {
    	int count = values.length;
    	for (Double val : list) {
    		// TODO bounds check
        	if (val != null && count > 0) {
        		values[--count] = val;
        	}
        }
    	return values.length - count;
    }

    /**
     * Return a reversed array and count of the non null values
     * 
     * @param list input, with possible null values
     * @param values output, without null values
     * @return the non null number
     */
    
    static int getArrayNonNullReverse(List<Double> list, double[] values) {
        int count = 0;
        boolean display = false;
        List<Double> newList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            // TODO bounds check
            Double val = list.get(i);
            if (val != null && count < values.length) {
                newList.add(val);
                count++;
                //values[count++] = val;
            } 
            if (val == null) {
                display = true;
            }
        }
        Collections.reverse(newList);
        for (int i = 0; i < count; i++) {
            values[i] = newList.get(i);
        }
        if (display) {
            log.info("mydisplay " + list);
        }
        return count;
    }

    static int getArrayNonNullReverse(Double[] list, double[] values) {
        int count = 0;
        boolean display = false;
        List<Double> newList = new ArrayList<>();
        for (int i = 0; i < list.length; i++) {
            // TODO bounds check
            Double val = list[i];
            if (val != null && count < values.length) {
                newList.add(val);
                count++;
                //values[count++] = val;
            } 
            if (val == null) {
                display = true;
            }
        }
        Collections.reverse(newList);
        for (int i = 0; i < count; i++) {
            values[i] = newList.get(i);
        }
        if (display) {
            log.info("mydisplay " + list);
        }
        return count;
    }

    public static Double[] getArrayNonNullReverse(Double[] array) {
        if (array == null) {
            return array;
        }
        ArrayList<Double> list = new ArrayList<>(Arrays.asList(array));
        list.removeAll(Collections.singleton(null)); 
        if (list.size() != array.length) {
            //System.out.println("shrink from " + array.length + " to " + list.size());
        }
        Collections.reverse(list);
        Double[] newArray = new Double[list.size()];
        return list.toArray(newArray);
    }

   private static int getArrayNonNull(List<Double> list, double[] values) {
    	int size = 0;
    	for (Double val : list) {
    		// TODO bounds check
        	if (val != null && size < values.length) {
        		values[size++] = val;
        	}
        }
    	return size;
    }
    
    /**
     * Get accepted ranges
     * where the range end should be have least a number of elements after it
     * and it is resized to be no more the a certain elements before
     * 
     * @param map of ranges
     * @param before minimum and required number to have before the range end
     * @param after required number for range end before the array end
     * @param size of the array with the ranges
     * @return resized accepted ranges
     */
    
    public static Map<Integer, Integer> getAcceptedRanges(Map<Integer, Integer> map, int before, int after, int size) {
        Map<Integer, Integer> retMap = new HashMap<>();
        for (int start : map.keySet()) {
            int end = map.get(start);
            if (end - start + 1 >= before) {
                // TODO check exact limit
                start = end - before + 1;
                // TODO check exact limit
                if (end + after < size) {
                    retMap.put(start, end);
                }
            }
        }
        return retMap;
    }

    /**
     * Get fresh ranges, with end less than after
     * 
     * @param map of ranges
     * @param before minimum and required number to have before the range end
     * @param after required number for range end before the array end
     * @param size of the array with the ranges
     * @return resized fresh range
     */
    
    public static Map<Integer, Integer> getFreshRanges(Map<Integer, Integer> map, int before, int after, int size) {
        Map<Integer, Integer> retMap = new HashMap<>();
        for (int start : map.keySet()) {
            int end = map.get(start);
            if (end - start + 1 >= before) {
                // TODO check exact limit
                start = end - before + 1;
                // TODO check exact limit
                if (end + after >= size) {
                    retMap.put(start, end);
                }
            }
        }
        return retMap;
    }

    /**
     * Get a sub part of Array
     * 
     * @param arr Array
     * @param start start
     * @param end end, inclusive
     * @return sub part
     */
    
    public static double[] getSub(double[] arr, int start, int end) {
        double[] retArr = new double[end - start + 1];
        for (int i = start; i <= end; i++) {
            retArr[i - start] = arr[i];
        }
        return retArr;
    }
    /**
     * Get a sub part of Array
     * 
     * @param arr Array
     * @param start start
     * @param end end, inclusive
     * @return sub part
     */
    
    public static Double[] getSubInclusive(Double[] arr, int start, int end) {        
        Double[] retArr = new Double[end - start + 1];
        for (int i = start; i <= end; i++) {
            retArr[i - start] = arr[i];
        }
        return retArr;
    }
    /**
     * Get a sub part of Array
     * 
     * @param arr Array
     * @param start start
     * @param end end, exclusive
     * @return sub part
     */
    
    public static Double[] getSubExclusive(Double[] arr, int start, int end) {
        return getSubInclusive(arr, start, end - 1);
    }
    
    public static List<Double[]> splitEpocsWindowsize(Double[] arr, int minepocs, int windowsize) {
        log.info("mysplits " + arr.length + " " + minepocs + " " + windowsize);
        if (arr.length < (windowsize + minepocs)) {
            return null;
        }
        List<Double[]> retlist = new ArrayList<>();
        int windowslide = windowsize;
        int epocs = arr.length / windowsize;
        windowsize++;
        if (epocs < minepocs) {
            epocs = minepocs;
            windowslide = arr.length / (epocs + 1);
            windowslide--;
        }
        System.out.println("ws"+windowslide);
        int startidx = 0;
        for (int i = 0; i < epocs; i++) {
            Double[] smallarr = new Double[windowsize];
            for (int j = 0; j < smallarr.length; j++) {
                smallarr[j] = arr[startidx + j];
            }
            retlist.add(smallarr);
            startidx += windowslide;
            System.out.println("wn"+startidx+ " " +i);
        }
        return retlist;
    }

    public static double[] convert(Double[] doubles) {
        double ret[] = new double[doubles.length];
        for (int i = 0; i < doubles.length; i ++) {
            ret[i] = doubles[i];
        }
        return ret;
    }

    /**
     * Goes through the array, smoothes out missing values (by using the previous),
     * or nulls out, depending or whether a max number was passed
     * 
     * @param doubles
     * @param maxHoleNumber
     * @return Fixed array
     */
    
    public static Double[] fixMapHoles(Double[] srcArray, Double[] dstArray, int maxHoleNumber) {
        int length = srcArray.length;
        if (dstArray == null) {
            dstArray = srcArray;
        } else {
            for (int i = 0; i < length; i ++) {
                dstArray[i] = srcArray[i];
            }
        }
        Double[] retDouble = new Double[dstArray.length];
        int start = searchBackwardNonNull(dstArray, length - 1);
        if (start != length - 1) {
            for (int k = 0; k < length; k++) {
                dstArray[k] = null;
            }                   
            return retDouble;            
        }
        for (int i = start; i >= 0; i--) {
            i = searchBackwardNull(dstArray, i);
            int j = searchBackwardNonNull(dstArray, i);
            if (i - j > maxHoleNumber) {
                for (int k = 0; k <= i; k++) {
                    dstArray[k] = null;
                }
                return dstArray;
            } else {
                //System.out.println(length + " " + i + " " + j);
                if (i < 0 || j < 0) {
                    //System.out.println(Arrays.asList(dstArray));
                }
                if (false && i < 0) {
                    for (int k = 0; k < length; k++) {
                        dstArray[k] = null;
                    }                   
                    return retDouble;
                }
                if (j < 0) {
                    return dstArray;
                }
                double diff = (dstArray[i + 1] - dstArray[j]) / 3;
                for (int k = j + 1; k <= i; k++) {
                    dstArray[k] = dstArray[j] + diff * (k - j);
                }
            }
            i = j;
        }
        return dstArray;
    }

    public static Map<String, double[]> getTruncList(Map<String, Double[]> listMap) {
        Map<String, double[]> retMap = new HashMap<>();
        for (String id : listMap.keySet()) {
            retMap.put(id, getNonNull(listMap.get(id)));
        }
        return retMap;
    }
    
    public static Map<Pair, double[]> getTruncList2(Map<Pair, Double[]> listMap) {
        Map<Pair, double[]> retMap = new HashMap<>();
        for (Pair id : listMap.keySet()) {
            retMap.put(id, getNonNull(listMap.get(id)));
        }
        return retMap;
    }
    
    public static double[] getNonNull(Double[] doubles) {
        int offset = searchForwardNonNull(doubles, 0, doubles.length);
        double[] retArray = new double[doubles.length - offset];
        for (int i = 0; i < doubles.length - offset; i++) {
            if (doubles[i + offset] == null) {
                int j = 0;
            }
            retArray[i] = doubles[i + offset];
        }
        return retArray;
    }

    private static int searchForwardNonNull(Double[] array, int i, int length) {
        while (i < length && array[i] == null)  {
            i++;
        }
        return i;
    }

    private static int searchBackwardNull(Double[] array, int i) {
        while (i >= 0 && array[i] != null) {
            i--;
        }
        return i;
    }

    private static int searchBackwardNonNull(Double[] array, int i) {
        while (i >= 0 && array[i] == null) {
            i--;
        }
        return i;
    }

    public static Map<String, Object[]> makeFixedMap(Map<String, Object[]> objectMap, int length) {
        Map<String, Object[]> retMap = new HashMap<>();
        for (String id : objectMap.keySet()) {
            Object[] array = objectMap.get(id);
            Object[] nullArray = new Object[length - array.length];
            retMap.put(id, ArrayUtils.addAll(nullArray, array));
        }
        return retMap;
    }

    public static double[] makeFixed(double[] array, int begin, int end, int length) {
        if (end == 0) {
            return new double[length];
        }
        return ArrayUtils.addAll(new double[begin], ArrayUtils.subarray(array, 0, end));
    }

}

