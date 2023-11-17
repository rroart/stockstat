package roart.common.util;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.CategoryConstants;
import roart.common.constants.Constants;

import com.google.common.collect.Range;

public class ArraysUtil {

    private static Logger log = LoggerFactory.getLogger(ArraysUtil.class);

    private int searchBackwardNegative(double[] array, int i) {
        return searchBackwardBelowLimit(array, i, 0);
    }
    
    private int searchBackwardBelowLimit(double[] array, int i, double limit) {
        while (i >= 0 && array[i] >= limit) {
            i--;
        }
        return i;
    }

    private static int searchForwardNegative(double[] array, int i) {
        return searchForwardBelowLimit(array, i, 0);
    }
    
    private static int searchForwardBelowLimit(double[] array, int i, double limit) {
        while (i < array.length && array[i] >= limit) {
            i++;
        }
        return i;
    }

    private static int searchForwardNegative(double[] array, int i, int length) {
        return searchForwardBelowLimit(array, i, length, 0);
    }
    
    private static int searchForwardBelowLimit(double[] array, int i, int length, double limit) {
        while (i < length && array[i] >= limit) {
            i++;
        }
        return i;
    }

    private static int searchForwardInRange(double[] array, int i, int length, Range<Double> range) {
        while (i < length && range.contains(array[i])) {
            i++;
        }
        return i;
    }

    private int searchBackwardPositive(double[] array, int i) {
        return searchBackwardAboveEqualLimit(array, i, 0);
    }
    
    private int searchBackwardAboveEqualLimit(double[] array, int i, double limit) {
        while (i >= 0 && array[i] < limit) {
            i--;
        }
        return i;
    }

    private static int searchForwardPositive(double[] array, int i) {
        return searchForwardAboveEqualLimit(array, i, 0);
    }
    
    private static int searchForwardAboveEqualLimit(double[] array, int i, double limit) {
        while (i < array.length && array[i] < limit)  {
            i++;
        }
        return i;
    }

    private static int searchForwardPositive(double[] array, int i, int length) {
        return searchForwardAboveEqualLimit(array, i, length, 0);
    }
    
    private static int searchForwardAboveEqualLimit(double[] array, int i, int length, double limit) {
        while (i < length && array[i] < limit)  {
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
        return searchForwardLimit(array, maxlen, 0);
    }
    
    public static Map<Integer, Integer>[] searchForwardLimit(double[] array, int maxlen, double limit) {
        int length = array.length;
        if (maxlen > 0) {
            length = maxlen;
        }
        Map<Integer, Integer>[] retmap = new HashMap[2];
        retmap[0] = new HashMap<>();
        retmap[1] = new HashMap<>();
        double prevval = array[0];
        int start = 0;
        for (int i = 0; i < length; i++) {
            if (!Double.isNaN(array[i])) {
                prevval = array[i];
                start = i;
                break;
            }
        }
        int prev = start;
        for (int i = start; i < length; i++) {
            if (prevval >= limit) {
                i = searchForwardBelowLimit(array, i, length, limit);
                retmap[0].put(prev, i - 1);
                if (i < length) {
                    prevval = array[i];
                }
                prev = i;
            } else {
                i = searchForwardAboveEqualLimit(array, i, length, limit);                       
                retmap[1].put(prev, i - 1);
                if (i < length) {
                    prevval = array[i];
                }
                prev = i;
            }
        }
        return retmap;
    }

    public static Map<Integer, Integer>[] searchForwardLimit(double[] array, int maxlen, double limit, Double otherlimit) {
        Range<Double>[] ranges;
        if (true || otherlimit == null || limit == otherlimit) {
            ranges = new Range[2];
            ranges[0] = Range.atLeast(limit);
            ranges[1] = Range.lessThan(limit);
        } else {
            ranges = new Range[3];
            if (otherlimit < limit) {
                ranges[0] = Range.atLeast(limit);
                ranges[1] = Range.closedOpen(otherlimit, limit);
                ranges[2] = Range.lessThan(otherlimit);
            } else {
                ranges[0] = Range.closedOpen(limit, otherlimit);
                ranges[1] = Range.lessThan(limit);
                ranges[2] = Range.atLeast(otherlimit);
            }
        }
        Map<Integer, Integer>[] retmap = getRangeMap(array, maxlen, ranges);
        for (int i = 0; i < retmap.length; i++) {
            if (retmap[i].containsValue(maxlen - 1)) {
                Integer key = null;
                for (Entry<Integer, Integer> entry : retmap[i].entrySet()) {
                    if (entry.getValue() == maxlen - 1) {
                        key = entry.getKey();
                    }
                }
                retmap[i].remove(key);
                break;
            }
        }
        if (ranges.length == 3) {
            Collection<Integer> keys = retmap[2].keySet();
            for (int i = 0; i < 2; i++) {
                Map<Integer, Integer> aRetmap = retmap[i];
                Map<Integer, Integer> newRetmap = new HashMap<>();
                for (Entry<Integer, Integer> entry : aRetmap.entrySet()) {
                    if (!keys.contains(entry.getValue() + 1)) {
                        newRetmap.put(entry.getKey(), entry.getValue());
                    }
                }
                retmap[i] = newRetmap;
            }
        }
        return retmap;
    }

    private static Map<Integer, Integer>[] getRangeMap(double[] array, int maxlen, Range<Double>[] ranges) {
        int length = array.length;
        if (maxlen > 0) {
            length = maxlen;
        }
        Map<Integer, Integer>[] retmap = new HashMap[ranges.length];
        for (int i = 0; i < ranges.length; i++) {
            retmap[i] = new HashMap<>();
        }
        int start = getFirstNumberIndex(array, length);
        int index = start;
        while (index < length) {
            int newIndex = -1;
            int rangeIndex = 0;
            for (rangeIndex = 0; rangeIndex < ranges.length; rangeIndex++) {
                Range<Double> aRange = ranges[rangeIndex];
                newIndex = searchForwardInRange(array, index, length, aRange);
                if (newIndex > index) {
                    break;
                }
            }
            if (newIndex > index) {
                retmap[rangeIndex].put(index, newIndex - 1);
                index = newIndex;
            } else {
                log.error("No range for {}", array[index]);
                index++;
            }
        }
        return retmap;
    }

    private static int getFirstNumberIndex(double[] array, int length) {
        int start = 0;
        for (int i = 0; i < length; i++) {
            if (!Double.isNaN(array[i])) {
                start = i;
                break;
            }
        }
        return start;
    }

    private Map<Integer, Integer>[] searchBackward(double[] array) {
        return searchBackwardLimit(array, 0);        
    }
    
    private Map<Integer, Integer>[] searchBackwardLimit(double[] array, double limit) {
        Map<Integer, Integer>[] retmap = new HashMap[2];
        retmap[0] = new HashMap<>();
        retmap[1] = new HashMap<>();
        int prev = array.length - 1;
        for (int i = array.length - 1; i >=0; i--) {
            if (prev >= 0) {
                i = searchBackwardBelowLimit(array, i, limit);
                retmap[0].put(prev, i);
            } else {
                i = searchBackwardAboveEqualLimit(array, i, limit);                       
            }
        }        
        return retmap;
    }

    public static Double[] getPercentizedPriceIndex(Double[] list) {
        if (list.length == 0) {
            return list;
        }
        return getPercentizedPriceIndex(list, list);
    }

    /**
     * Pick the first non-null element in the array, and find the
     * increases/decreases since that. ( * 100 / first)
     * @param list
     * @param key whether price/index, if not, then skip
     * @return new array
     */

    public static Double[] getPercentizedPriceIndex(Double[] list, Double[] firstlist) {
        if (list == null) {
            return list;
        }
        Double[] retlist = new Double[list.length]; 
        int i = 0;
        double first = 0;
        for (i = 0; i < firstlist.length; i ++) {
            if (firstlist[i] != null) {
                first = firstlist[i];
                break;
            }
        }
        for (; i < list.length; i ++) {
            if (list[i] != null) {
                retlist[i] = list[i]* 100 / first;
            }
        }
        return retlist;
    }

    public static double[] getPercentizedPriceIndex(double[] list) {
        if (list.length == 0) {
            return list;
        }
        return getPercentizedPriceIndex(list, list[0]);
    }

    public static double[] getPercentizedPriceIndex(double[] list, double first) {
        if (list == null || list.length == 0 || first == 0) {
            return list;
        }
        double[] retlist = new double[list.length];
        int i = 0;
        for (; i < list.length; i ++) {
            retlist[i] = list[i] * 100 / first;
        }
        return retlist;
    }

    public static boolean verifyPercentized(Double[] list, String key) {
        if (list != null) {
            if (list[0] == 100.0) {
                return true;
            } else {
                log.error("Not rescaled");
                return false;
            }
        }
        return true;
    }

    private static int getArrayNonNullReversenot(List<Double> list, double[] values) {
        int count = values.length;
        for (Double val : list) {
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

    public static int getArrayNonNullReverse(List<Double> list, double[] values) {
        int count = 0;
        boolean display = false;
        List<Double> newList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Double val = list.get(i);
            if (val != null && count < values.length) {
                newList.add(val);
                count++;
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
            log.info("mydisplay {}", list);
        }
        return count;
    }

    static int getArrayNonNullReverse(Double[] list, double[] values) {
        int count = 0;
        boolean display = false;
        List<Double> newList = new ArrayList<>();
        for (int i = 0; i < list.length; i++) {
            Double val = list[i];
            if (val != null && count < values.length) {
                newList.add(val);
                count++;
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
            log.info("mydisplay {}", list);
        }
        return count;
    }

    public static Double[] getArrayNonNullReverse(Double[] array) {
        if (array == null) {
            return array;
        }
        ArrayList<Double> list = new ArrayList<>(Arrays.asList(array));
        list.removeAll(Collections.singleton(null)); 
        Collections.reverse(list);
        Double[] newArray = new Double[list.size()];
        return list.toArray(newArray);
    }

    private static int getArrayNonNull(List<Double> list, double[] values) {
        int size = 0;
        for (Double val : list) {
            if (val != null && size < values.length) {
                values[size++] = val;
            }
        }
        return size;
    }

    public List<Double> getNonNullList(List<Double> list) {
        List<Double> nonNullList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Double val = list.get(i);
            if (val != null) {
                nonNullList.add(val);
            }
        }
        return nonNullList;
    }
    
    public List<Double> getNonNullListNew(List<Double> list) {
        return list
                .stream()
                .filter(Objects::nonNull)
                .toList();
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
     * @param endOnly 
     * @return resized accepted ranges
     */

    @Deprecated
    public static Map<Integer, Integer> getAcceptedRanges(Map<Integer, Integer> map, int before, int after, int size, boolean endOnly) {
        Map<Integer, Integer> retMap = new HashMap<>();
        for (int start : map.keySet()) {
            int end = map.get(start);
            if (end - start + 1 >= before) {
                start = end - before + 1;
                if (end + after < size) {
                    retMap.put(start, end);
                }
            }
        }
        return retMap;
    }

    @Deprecated
    public static Map<Integer, Integer> getAcceptedRanges(Map<Integer, Integer> map, int after, int size) {
        Map<Integer, Integer> retMap = new HashMap<>();
        for (int start : map.keySet()) {
            int end = map.get(start);
            // + after
            if (end < size && start == 0 && end + 1 != size) {
                retMap.put(start, end);
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
        for (Entry<Integer, Integer> entry : map.entrySet()) {
            int start = entry.getKey();
            int end = entry.getValue();
            if (end - start + 1 >= before) {
                start = end - before + 1;
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

    public static double[] getSubInclusive(double[] arr, int start, int end) {        
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
     * @param end end, exclusive
     * @return sub part
     */

    public static Double[] getSubExclusive(Double[] arr, int start, int end) {
        return getSubInclusive(arr, start, end - 1);
    }

    public static double[] getSubExclusive(double[] arr, int start, int end) {
        return getSubInclusive(arr, start, end - 1);
    }

    public static List<Double[]> splitEpocsWindowsize(Double[] arr, int minepocs, int windowsize) {
        log.info("mysplits {} {} {}", arr.length, minepocs, windowsize);
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
        log.info("ws {}", windowslide);
        int startidx = 0;
        for (int i = 0; i < epocs; i++) {
            Double[] smallarr = new Double[windowsize];
            for (int j = 0; j < smallarr.length; j++) {
                smallarr[j] = arr[startidx + j];
            }
            retlist.add(smallarr);
            startidx += windowslide;
            log.info("wn {} {}", startidx, i);
        }
        return retlist;
    }

    public static double[] convert(Double[] doubles) {
        double[] ret = new double[doubles.length];
        for (int i = 0; i < doubles.length; i ++) {
            ret[i] = doubles[i];
        }
        return ret;
    }

    public static double[][] convert(Double[][] doubles) {
        double[][] ret = new double[doubles.length][];
        for (int i = 0; i < doubles.length; i ++) {
            ret[i] = new double[doubles[i].length];
            for (int j = 0; j < doubles[i].length; j ++) {
                ret[i][j] = doubles[i][j];
            }
        }
        return ret;
    }

    public static Double[] convert(double[] doubles) {
        Double[] ret = new Double[doubles.length];
        for (int i = 0; i < doubles.length; i ++) {
            ret[i] = doubles[i];
        }
        return ret;
    }

    public static Double[][] convertNot(List<List> listOfList) {
        return listOfList.stream()
        .map(l -> l.stream()
                //.mapToDouble(Double::doubleValue)
                .toArray(Double[]::new)
                )
        .toArray(Double[][]::new);
    }

    public static Double[][] convert(List<List<Double>> listOfList) {
        //listOfList.stream().forEach(l -> l.stream().forEach(e -> System.out.print(e != null ? e.getClass().getName() : "")));
        /*
        for (Object obj : listOfList) {
            if (obj instanceof List list) {
                list.stream().forEach(e -> System.out.print(e != null ? e.getClass().getName() : ""));
            } else {
                System.out.println("other" + obj);
            }
        }
        */
        return listOfList.stream()
                .map(l -> l.toArray(Double[]::new)
                        )
                .toArray(Double[][]::new);
    }
    
    public static Double[] convert1(List<Double> list) {
        for (int i = 0; i < list.size(); i++) {
            Object object = list.get(i);
            if (object instanceof String string && string.equals("NaN")) {
                list.set(i, Double.NaN);
            }
            if (object instanceof String string && string.equals("-Infinity")) {
                list.set(i, Double.NEGATIVE_INFINITY);
            }
            if (object instanceof String string && string.equals("Infinity")) {
                list.set(i, Double.POSITIVE_INFINITY);
            }
        }
        return list.toArray(Double[]::new);
    }
    
    public static Object[] convert2(List list) {
        for (int i = 0; i < list.size(); i++) {
            Object object = list.get(i);
            if (object instanceof List anotherlist) {
                Double[] array = convert1(anotherlist);
                list.set(i, convert(array));
            }
        }
        return list.toArray(Object[]::new);
    }
    
    public static List<List<Double>> convertA2L(Double[][] doubles) {
        List<List<Double>> ret = new ArrayList<>();
        for (int i = 0; i < doubles.length; i ++) {
            ret.add(Arrays.asList(doubles[i]));
        }
        return ret;
    }

    
/**
     * Goes through the array, smoothes out missing values (by using the previous),
     * or nulls out, depending or whether a max number was passed
     * @param maxHoleNumber
     * @param interpolationmethod TODO
     * @param acceptLastNull TODO
     * @param doubles
     * @return Fixed array
     */

    public static Double[] fixMapHoles(Double[] srcArray, Double[] dstArray, int maxHoleNumber, String interpolationmethod, boolean acceptLastNull) {
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
        if (!acceptLastNull && start != length - 1) {
            for (int k = 0; k < length; k++) {
                dstArray[k] = null;
            }                   
            return retDouble;            
        }
        boolean ffill = Constants.FFILL.equals(interpolationmethod);
        int mymaxholenumber = maxHoleNumber == 0 ? 1 : maxHoleNumber;
        if (ffill && acceptLastNull && start != length - 1 && start >= 0 /*&& length - 1 - start <= mymaxholenumber*/) {
            for (int k = start + 1; k < length; k++) {
                dstArray[k] = dstArray[start];
            }                   
        }
        for (int i = start; i >= 0; i--) {
            i = searchBackwardNull(dstArray, i);
            int j = searchBackwardNonNull(dstArray, i);
            if (maxHoleNumber > 0 && i - j > maxHoleNumber) {
                for (int k = 0; k <= i; k++) {
                    dstArray[k] = null;
                }
                return dstArray;
            } else {
                if (false && i < 0) {
                    for (int k = 0; k < length; k++) {
                        dstArray[k] = null;
                    }                   
                    return retDouble;
                }
                if (j < 0) {
                    return dstArray;
                }
                double diff = (dstArray[i + 1] - dstArray[j]) / (i + 1 - j);
                if (Constants.FFILL.equals(interpolationmethod)) {
                    diff = 0;
                }
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
        for (Entry<String, Double[]> entry : listMap.entrySet()) {
            retMap.put(entry.getKey(), getNonNullNew(entry.getValue()));
        }
        return retMap;
    }

    public static Map<String, double[][]> getTruncListArr(Map<String, Double[][]> listMap) {
        Map<String, double[][]> retMap = new HashMap<>();
        for (Entry<String, Double[][]> entry : listMap.entrySet()) {
            Double[][] array = entry.getValue();
            double[][] newArray = new double[array.length][];
            for (int i = 0; i < array.length; i++) {
                newArray[i] = getNonNullNew(array[i]);
            }
            retMap.put(entry.getKey(), newArray);
        }
        return Collections.unmodifiableMap(retMap);
    }

    public static Map<Pair<String, String>, double[]> getTruncList2(Map<Pair<String, String>, Double[]> listMap) {
        Map<Pair<String, String>, double[]> retMap = new HashMap<>();
        for (Entry<Pair<String, String>, Double[]> entry : listMap.entrySet()) {
            retMap.put(entry.getKey(), getNonNullNew(entry.getValue()));
        }
        return retMap;
    }

    public static Map<Pair<String, String>, double[][]> getTruncList22(Map<Pair<String, String>, Double[][]> listMap) {
        Map<Pair<String, String>, double[][]> retMap = new HashMap<>();
        for (Entry<Pair<String, String>, Double[][]> entry : listMap.entrySet()) {
            Double[][] array = entry.getValue();
            double[][] newArray = new double[array.length][];
            for (int i = 0; i < array.length; i++) {
                newArray[i] = getNonNullNew(array[i]);
            }
            retMap.put(entry.getKey(), newArray);
        }
        return retMap;
    }

    public static double[] getNonNull(Double[] doubles) {
        int offset = searchForwardNonNull(doubles, 0, doubles.length);
        if (offset > 0) {
            int jj = 0;
        }
        double[] retArray = new double[doubles.length - offset];
        for (int i = 0; i < doubles.length - offset; i++) {
            if (doubles[i + offset] == null) {
                int jj = 0;
                continue;
            }
            retArray[i] = doubles[i + offset];
        }
        return retArray;
    }

    public static double[] getNonNullNew(Double[] doubles) {
        int end = doubles.length - 1;
        int lastoffset = searchBackwardNonNull(doubles, end);
        if (lastoffset < 0 || lastoffset != end) {
            return new double[0];
        }
        int nextlastoffset = searchBackwardNull(doubles, lastoffset);
        if (nextlastoffset < 0) {
            nextlastoffset = 0;
        }
        if (nextlastoffset < 2) {
            int jj = 0;
        }
        //System.out.println("" + nextlastoffset);
        nextlastoffset = searchForwardNonNull(doubles, nextlastoffset, doubles.length);
        double[] retArray = new double[doubles.length - nextlastoffset];
        for (int i = nextlastoffset; i < doubles.length; i++) {
            if (doubles[i] == null) {
                int jj = 0;
            }
            retArray[i - nextlastoffset] = doubles[i];
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
        for (Entry<String, Object[]> entry : objectMap.entrySet()) {
            Object[] array = entry.getValue();
            Object[] nullArray = new Object[length - array.length];
            retMap.put(entry.getKey(), ArrayUtils.addAll(nullArray, array));
        }
        return retMap;
    }

    public static double[] makeFixed(double[] array, int begin, int end, int length) {
        if (end == 0) {
            return new double[length];
        }
        return ArrayUtils.addAll(new double[begin], ArrayUtils.subarray(array, 0, end));
    }

    public static Pair<Integer, Integer> intersect(List<Pair<Integer, Integer>> begendList) {
        Pair<Integer, Integer> pair = begendList.get(0);
        Range<Integer> range = Range.closed(pair.getLeft(), pair.getRight());
        for (int i = 1; i < begendList.size(); i++) {
            Pair<Integer, Integer> aPair = begendList.get(i);
            Range<Integer> aRange = Range.closed(aPair.getLeft(), aPair.getRight());
            range = range.intersection(aRange);
        }
        return new ImmutablePair<Integer, Integer>(range.lowerEndpoint(), range.upperEndpoint());
    }

    public static <E> E getLast(List<E> list) {
        return !list.isEmpty() ? list.get(list.size() -1 ) : null;
    }
}

