package roart.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        if (("Index").equals(key) || ("Price").equals(key)) {
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
}

