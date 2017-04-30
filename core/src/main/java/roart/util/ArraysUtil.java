package roart.util;

import java.util.HashMap;
import java.util.Map;

public class ArraysUtil {

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
    
}

