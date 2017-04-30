package roart.util;

import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

public class ArraysUtilTest {
    private static double[] array = { 1,1,1,1,1,1,1,1,-1,-1,-1,-1,-1,1,1,1,1};
    
    @Test
    public void testSearchForward() throws Exception {
        Map<Integer, Integer>[] map = ArraysUtil.searchForward(array);
        Map<Integer, Integer> pos = map[0];
        Map<Integer, Integer> neg = map[1];
        System.out.println("pos " + pos);
        System.out.println("neg " + neg);
        assertEquals(pos.get(0), new Integer(7));
        assertEquals(pos.get(13), new Integer(16));
        assertEquals(neg.get(8), new Integer(12));
        
    }
}
