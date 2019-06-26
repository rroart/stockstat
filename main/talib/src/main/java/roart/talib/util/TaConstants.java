package roart.talib.util;

public class TaConstants {
    public static final int MACDIDXHIST = 0;
    public static final int MACDIDXMACD = 1;
    public static final int MACDIDXSIGN = 2;
    public static final int MACDIDXBEG = 3;
    public static final int MACDIDXEND = 4;
    public static final int MACDIDXMACDFIXED = 5;
    public static final int MACDIDXSIGFIXED = 6;
    public static final int MACDIDXHISTFIXED = 7;

    public static final int RSIIDXRSI = 0;
    public static final int RSIIDXBEG = 1;
    public static final int RSIIDXEND = 2;
    public static final int RSIIDXRSIFIXED = 3;

    public static final int ONEIDXARRONE = 0;
    public static final int ONEIDXBEG = 1;
    public static final int ONEIDXEND = 2;
    public static final int ONEIDXARRONEFIXED = 3;

    public static final int[] ONEARRAY = { ONEIDXARRONE };
    public static final int[] ONERANGE = { ONEIDXBEG, ONEIDXEND };
    public static final int[] ONEARRAYFIXED = { ONEIDXARRONEFIXED };
    public static final int[][] ONE = { ONEARRAY, ONERANGE, ONEARRAYFIXED };
    
    public static final int TWOIDXARRONE = 0;
    public static final int TWOIDXARRTWO = 1;
    public static final int TWOIDXBEG = 2;
    public static final int TWOIDXEND = 3;
    public static final int TWOIDXARRONEFIXED = 4;
    public static final int TWOIDXARRTWOFIXED = 5;

    public static final int[] TWOARRAY = { TWOIDXARRONE, TWOIDXARRTWO };
    public static final int[] TWORANGE = { TWOIDXBEG, TWOIDXEND };
    public static final int[] TWOARRAYFIXED = { TWOIDXARRONEFIXED, TWOIDXARRTWOFIXED };
    public static final int[][] TWO = { TWOARRAY, TWORANGE, TWOARRAYFIXED };
    
    public static final int THREEIDXARRONE = 0;
    public static final int THREEIDXARRTWO = 1;
    public static final int THREEIDXARRTHREE = 2;
    public static final int THREEIDXBEG = 3;
    public static final int THREEIDXEND = 4;
    public static final int THREEIDXARRONEFIXED = 5;
    public static final int THREEIDXARRTWOFIXED = 6;
    public static final int THREEIDXARRTHREEFIXED = 7;

    public static final int[] THREEARRAY = { THREEIDXARRONE, THREEIDXARRTWO, THREEIDXARRTHREE };
    public static final int[] THREERANGE = { THREEIDXBEG, THREEIDXEND };
    public static final int[] THREEARRAYFIXED = { THREEIDXARRONEFIXED, THREEIDXARRTWOFIXED, THREEIDXARRTHREEFIXED };
    public static final int[][] THREE = { THREEARRAY, THREERANGE, THREEARRAYFIXED };
    
    public static final int ARRAY = 0;
    public static final int RANGE = 1;
    public static final int ARRAYFIXED = 2;
    
}
