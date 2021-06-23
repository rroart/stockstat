package roart.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;

public class MathUtil {
    public static double round(Double d, int n) {
        if (d.isInfinite()) {
            int jj = 0;
        }
        String hashes = StringUtils.repeat("#", n);
        DecimalFormat df = new DecimalFormat("#." + hashes);
        //df.setRoundingMode(RoundingMode.HALF_UP);
        return Double.valueOf(df.format(d));
    }

    public static double round2(Double d, int n) {
        return Precision.round(d, n);
    }

    public static double round3(Double d, int n) {
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(n, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static Object[] round(Object[] o, int n) {
        String hashes = StringUtils.repeat("#", n);
        DecimalFormat df = new DecimalFormat("#." + hashes);
        Object[] newo = new Object[o.length];
        for (int i = 0; i < o.length; i++) {
            if (o[i] instanceof Double) {
                //df.setRoundingMode(RoundingMode.HALF_UP);
                newo[i] = Double.valueOf(df.format(o[i]));
            } else {
                newo[i] = o[i];
            }
        }
        return newo;
    }

    public static Object[] round2(Object[] o, int n) {
        Object[] newo = new Object[o.length];
        for (int i = 0; i < o.length; i++) {
            if (o[i] instanceof Double) {
                newo[i] = Precision.round((double) o[i], n);
            } else {
                newo[i] = o[i];
            }
        }
        return newo;
    }

    public static Object[] round3(Object[] o, int n) {
        Object[] newo = new Object[o.length];
        for (int i = 0; i < o.length; i++) {
            if (o[i] instanceof Double) {
                BigDecimal bd = new BigDecimal(Double.toString((double) o[i]));
                bd = bd.setScale(n, RoundingMode.HALF_UP);
                newo[i] = bd.doubleValue();
            } else {
                newo[i] = o[i];
            }
        }
        return newo;
    }    
    
    private Object[] round4(Object[] objs, int places) {
        if (objs != null) {
            for (int i = 0; i < objs.length; i++) {
                Object obj = objs[i];
                if (obj.getClass() == Double.class) {
                    obj = MathUtil.round2((Double) obj, 3);
                }
            }
        }
        return objs;
    }

    public static double[] getGeoSeq(double[] array) {
        if (array == null || array.length == 1) {
            return array;
        }
        double[] geom = new double[array.length];
        Double first = array[0];
        Double last = array[array.length - 1];        
        for (int i = 0; i < array.length; i++) {
            geom[i] = first * Math.pow((double)last/first, (double) i/(array.length - 1));
        }
        return geom;
    }

}
