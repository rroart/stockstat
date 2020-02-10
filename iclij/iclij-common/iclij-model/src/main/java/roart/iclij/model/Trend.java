package roart.iclij.model;

import java.text.DecimalFormat;

public class Trend {
    public int up;
    public int neutral;
    public int down;
    
    public double incProp;
    
    public double incAverage;
    
    public double min;
    
    public double max;
 
    public String stats;
    
    public static String roundme(Double eval) {
        if (eval == null) {
            return null;
        }
        DecimalFormat df = new DecimalFormat("#.00");
        return df.format(eval);
    }


    @Override public String toString() {
        return "D " + down + " N " + neutral + " U " + up + " prop " + roundme(incProp) + " aver " + roundme(incAverage) + " min " + roundme(min) + " max " + roundme(max) + " stats " + stats;
    }
}