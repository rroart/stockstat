package roart.common.pipeline.data;

public class TwoDimD extends SerialObject {

    private Double[][] array;
    
    public TwoDimD() {
        super();
    }

    public TwoDimD(Double[][] array) {
        super();
        this.array = array;
    }

    public Double[][] getArray() {
        return array;
    }

    public void setArray(Double[][] array) {
        this.array = array;
    }
    
    public Double[] get(int index) {
        return array[index];
    }
}
