package roart.common.pipeline.data;

public class SerialPairPlain extends SerialObject {
    private Object left;
    
    private Object right;

    public SerialPairPlain() {
        super();
    }

    public SerialPairPlain(Object left, Object right) {
        super();
        this.left = left;
        this.right = right;
    }

    public Object getLeft() {
        return left;
    }

    public void setLeft(Object left) {
        this.left = left;
    }

    public Object getRight() {
        return right;
    }

    public void setRight(Object right) {
        this.right = right;
    }
    

}
