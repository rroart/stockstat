package roart.common.pipeline.data;

public class OneDim {

    private Object[] array;

    public OneDim() {
        super();
    }

    public OneDim(Object[] array) {
        super();
        this.array = array;
    }

    public Object[] getArray() {
        return array;
    }

    public void setArray(Object[] array) {
        this.array = array;
    }

    public Object get(int index) {
        return array[index];
    }
        
}
