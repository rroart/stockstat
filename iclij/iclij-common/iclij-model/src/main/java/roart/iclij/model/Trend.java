package roart.iclij.model;

public class Trend {
    public int up;
    public int neutral;
    public int down;
    
    public double incProp;
    
    public double incAverage;
    
    @Override public String toString() {
        return "D " + down + " N " + neutral + " U " + up + " prop " + incProp + " aver " + incAverage;
    }
}