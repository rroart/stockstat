package roart.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArraysUtil {

    private static Logger log = LoggerFactory.getLogger(TaUtil.class);

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

    /**
     * This will search an array, and return the gruoupings of positive and negative numbers
     * This returns an array of two maps, the first is for positive, the second for negative ranges.
     * The key is low value, while the value is the high value.
     * 
     * @param array
     * @return a resulting array
     */
    
    public static Map<Integer, Integer>[] searchForward(double[] array) {
        Map<Integer, Integer>[] retmap = new HashMap[2];
        retmap[0] = new HashMap<>();
        retmap[1] = new HashMap<>();
        double prevval = array[0];
        int prev = 0;
        for (int i = 0; i < array.length; i++) {
            if (prevval >= 0) {
                i = searchForwardNegative(array, i);
                retmap[0].put(prev, i - 1);
                if (i < array.length) {
                    prevval = array[i];
                }
                prev = i;
           } else {
                i = searchForwardPositive(array, i);                       
                retmap[1].put(prev, i - 1);
                if (i < array.length) {
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
    
}

