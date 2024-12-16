package roart.common.pipeline.data;

public class TwoDimd extends SerialObject {

    private double[][] array;
    
    public TwoDimd() {
        super();
    }

    public TwoDimd(double[][] array) {
        super();
        this.array = array;
    }

    public double[][] getArray() {
        return array;
    }

    public void setArray(double[][] array) {
        this.array = array;
    }
}
