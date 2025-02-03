package roart.mutation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class MutateTest {
    private static double[] array = { 1,1,1,1,1,1,1,1,-1,-1,-1,-1,-1,1,1,1,1};
    
    @Test
    public void testMutate() throws Exception {
        Map<String, Double> map = new HashMap<>();
        map.put("a", 2.0);
        map.put("b", 5.0);
        //Mutate.mutate(map);
        System.out.println(map);
        //assertNotEquals(map.get("a"), 2.0);
        //assertNotEquals(map.get("b"), 5.0);
    }
}
