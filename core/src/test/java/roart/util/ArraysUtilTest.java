package roart.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

public class ArraysUtilTest {
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
        Map<Integer, Integer> newPos = ArraysUtil.getAcceptedRanges(pos, 5, 5, array.length);
        Map<Integer, Integer> newNeg = ArraysUtil.getAcceptedRanges(neg, 5, 5, array.length);
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
        int num = ArraysUtil.getArrayNonNullReverse(list, values);
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
        ArraysUtil.getPercentizedPriceIndex(null, null);
        ArraysUtil.getPercentizedPriceIndex(null, "Price");
        ArraysUtil.getPercentizedPriceIndex(nullist, "Price");
        Double[] list = new Double[]{null, 200.0, 50.0, 125.0};
        ArraysUtil.getPercentizedPriceIndex(list, null);
        ArraysUtil.getPercentizedPriceIndex(list, "");
        assertEquals(list[1], 200.0, 0.1);
        ArraysUtil.getPercentizedPriceIndex(list, "Price");
        assertEquals(list[2], 25.0, 0.1);
      
    }
}
