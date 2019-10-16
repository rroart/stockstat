package roart.common.util;

import java.util.Random;

public class RandomUtil {
    
    public static int random(Random random, int min, int max) {
        return random(random, min, 1, max - min);
    }
    
    public static int random(Random random, int base, int multiple, int range) {
        return base + multiple * random.nextInt(range);
    }
    
    public static double random(Random random, double base, double multiple, int range) {
        return base + multiple * random.nextInt(range);
    }
    
    public static double random(Random random, double base, double range) {
        return base + range * random.nextDouble();
    }
    
    public static double generatePow(Random random, double base, int min, int max) {
        return Math.pow(base, random(random, min, max + 1));
    }
 
}
