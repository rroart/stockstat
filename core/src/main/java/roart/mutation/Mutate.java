package roart.mutation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Mutate {
    
    public static void mutate(Map<String, Double> map) {
        Random random = new Random();
        int len = map.size();
        List<String> keys = new ArrayList(map.keySet());
        int key0 = random.nextInt(len);
        String str0 = keys.get(key0);
        keys.remove(key0);
        int key1 = random.nextInt(len - 1);
        String str1 = keys.get(key1);
        map.put(str0, map.get(str0) + 1);
        map.put(str1, map.get(str1) - 1);
    }
    
    @Deprecated
    public static void mutate(Map<String, Object> configValueMap, List<String> keys) {
        List<String> keyCopies = new ArrayList<>(keys);
        Random random = new Random();
        int len = keyCopies.size();
        // only different line, missing keys =
        int key0 = random.nextInt(len);
        String str0 = keyCopies.get(key0);
        keyCopies.remove(key0);
        int key1 = random.nextInt(len - 1);
        String str1 = keyCopies.get(key1);
        if (((Integer) configValueMap.get(str0)).intValue() >= 99) {
            return;
        }
        if (((Integer) configValueMap.get(str1)).intValue() <= 1) {
            return;
        }
        configValueMap.put(str0, ((Integer) configValueMap.get(str0)) + 1);
        configValueMap.put(str1, ((Integer) configValueMap.get(str1)) - 1);
    }
}
