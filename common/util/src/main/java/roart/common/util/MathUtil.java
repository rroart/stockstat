package roart.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;

public class MathUtil {
    public static double round(Double d, int n) {
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

}
