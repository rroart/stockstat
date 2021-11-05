package roart.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import roart.common.constants.Constants;
import roart.common.util.ArraysUtil;

import static org.junit.jupiter.api.Assertions.*;

public class ArraysUtilTest {
    private String interpolationmethod = Constants.FFILL;
    private static double[] array = { 1,1,1,1,1,1,1,1,-1,-1,-1,-1,-1,1,1,1,1};
    @Test
    public void testSearchForward() throws Exception {
        Map<Integer, Integer>[] map = ArraysUtil.searchForward(array, 0);
        Map<Integer, Integer> pos = map[0];
        Map<Integer, Integer> neg = map[1];
        System.out.println("pos " + pos);
        System.out.println("neg " + neg);
        assertEquals(pos.get(0), new Integer(7));
        assertEquals(pos.get(13), new Integer(16));
        assertEquals(neg.get(8), new Integer(12));  
        Map<Integer, Integer> newPos = ArraysUtil.getAcceptedRanges(pos, 5, 5, array.length, true);
        Map<Integer, Integer> newNeg = ArraysUtil.getAcceptedRanges(neg, 5, 5, array.length, true);
        System.out.println("newpos " + newPos);
        System.out.println("newneg " + newNeg);
        assertEquals(newPos.size(), 1);
        assertEquals(newPos.get(3), new Integer(7));
        assert(newNeg.isEmpty());
        Map<Integer, Integer> fresh = ArraysUtil.getFreshRanges(pos, 4, 5, array.length);
        System.out.println("fresh " + fresh);
        assertEquals(fresh.get(13), new Integer(16));
   }
    
    @Test
    public void testGetArrayNonNullReverse() {
        List<Double> list = new ArrayList<>();
        list.add(2.0);
        list.add(null);
        list.add(1.0);
        double[] values = new double[list.size()];
        int num = ArraysUtil.getArrayNonNullReverse(list, values);
        assertEquals(num, 2);
        assertEquals(values[0], 1, 0.1);
    }  
    
    @Test
    public void testGetArrayNonNullReverse2() {
        Double[] list = new Double[]{2.0, null, 1.0};
        double[] values = new double[list.length];
        int num = ArraysUtil.getArrayNonNullReverse(Arrays.asList(list), values);
        assertEquals(num, 2);
        assertEquals(values[0], 1, 0.1);
    }  
    
    @Test
    public void testGetArrayNonNullReverse3() {
        Double[] list = new Double[]{2.0, null, 1.0};
        Double[] values = ArraysUtil.getArrayNonNullReverse(list);
        assertEquals(values.length, 2);
        assertEquals(values[0], 1, 0.1);
    }  
    
    @Test
    public void testGetSub() {
        Double[] list = new Double[]{1.0, 2.0, 3.0, 4.0, 5.0};
        Double[] values = ArraysUtil.getSubExclusive(list, 1, 3);
        System.out.println("Arr " + Arrays.asList(values));
        assertEquals(values.length, 2);
        assertEquals(values[0], 2, 0.1);
    }  
    
    @Test
    public void getPercentizedPriceIndex() {
        Double[] nullist = new Double[]{null, null, null, null};
        //ArraysUtil.getPercentizedPriceIndex(null, null);
        //ArraysUtil.getPercentizedPriceIndex(null, "Price");
        //ArraysUtil.getPercentizedPriceIndex(nullist, "Price");
        Double[] list = new Double[]{null, 200.0, 50.0, 125.0};
        ArraysUtil.getPercentizedPriceIndex(null, list);
        //ArraysUtil.getPercentizedPriceIndex(list, "");
        assertEquals(list[1], 200.0, 0.1);
        list = ArraysUtil.getPercentizedPriceIndex(list, list);
        assertEquals(list[2], 25.0, 0.1);
      
    }
    
    @Test
    public void splitEpocsWindowsize() {
        Double[] list = {2.0, 3.0, 4.0, 5.0, 6.0, 7.0 , 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0,15.0};
        List<Double[]> list2 = ArraysUtil.splitEpocsWindowsize(list, 3, 4);
        for (Double[] arr : list2) {
            System.out.println(Arrays.asList(arr));
        }
        System.out.println("more");
        list2 = ArraysUtil.splitEpocsWindowsize(list, 5, 5);
        for (Double[] arr : list2) {
            System.out.println(Arrays.asList(arr));
        }
        System.out.println("more3");
        list2 = ArraysUtil.splitEpocsWindowsize(list, 2, 3);
        for (Double[] arr : list2) {
            System.out.println(Arrays.asList(arr));
        }
        System.out.println("more4");
        list = new Double[145];
        list2 = ArraysUtil.splitEpocsWindowsize(list, 20, 10);
        list = new Double[46];
        list2 = ArraysUtil.splitEpocsWindowsize(list, 20, 10);
    }
    
    @Test
    public void arrayUtilTest() {
        Double[] array = { 1.0, 2.0, 3.0, null, null, null, 4.0, 5.0, null, null, 10.0, 11.0 };
        ArraysUtil.fixMapHoles(array, null, 2, interpolationmethod, false);
        System.out.println(Arrays.asList(array));
        assertArrayEquals(new Double[] {null, null, null, null, null, null, 4.0, 5.0, 5.0, 5.0, 10.0, 11.0 }, array);
        Double[] array2 = { null, 1.0, 2.0, 3.0 };
        ArraysUtil.fixMapHoles(array2, null, 2, interpolationmethod, false);
        System.out.println(Arrays.asList(array2));
        Double[] array3 = { 1.0, 2.0, 3.0, null };
        ArraysUtil.fixMapHoles(array3, null, 2, interpolationmethod, false);
        System.out.println(Arrays.asList(array3));
        Double[] array4 = { null, null, null, 1.0, 2.0, 3.0, null };
        System.out.println("n4");
        ArraysUtil.fixMapHoles(array4, null, 2, interpolationmethod, false);
        System.out.println("nr4 " + Arrays.asList(array4));
        Double[] array5 = { 1.0, 2.0, 3.0, null, null, null, 4.0, 5.0, null, null, 10.0, 11.0 };
        ArraysUtil.fixMapHoles(array5, null, 5, interpolationmethod, false);
        System.out.println(Arrays.asList(array5));
    }
    
    @Test
    public void makeFixed() {
        System.out.println("makefixed");
        double[] array1 = { 1.0, 2.0, 3.0, 4.0, 0.0, 0.0, 0.0 };
        double[] res1 = ArraysUtil.makeFixed(array1, 3, 4, 7);
        System.out.println(Arrays.asList(res1));
        double[] res2 = ArraysUtil.makeFixed(array1, 0, 0, 0);
        System.out.println(Arrays.asList(res2));
        double[] res3 = ArraysUtil.makeFixed(array1, 0, 0, 7);
        System.out.println(Arrays.asList(res3));
    }
}
