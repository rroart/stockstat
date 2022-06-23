package roart.common.util;

import java.text.NumberFormat;
import java.util.Locale;

public class MemUtil {

    public static long[] mem() {
        return new long[] { Runtime.getRuntime().totalMemory(), Runtime.getRuntime().freeMemory(), Runtime.getRuntime().maxMemory() };
    }
    
    public static long[] diff(long[] now, long[] old) {
        return new long[] { now[0] - old[0], now[1] - old[1], now[2] - old[2] };
    }
    
    public static String print(long[] mem) {
        NumberFormat fmt = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
        return fmt.format(mem[0]) + " " + fmt.format(mem[1]) + " " + fmt.format(mem[2]);
    }

}
