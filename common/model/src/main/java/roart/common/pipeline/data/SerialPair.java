package roart.common.pipeline.data;

public class SerialPair extends SerialObject {
    private SerialObject left;
    
    private SerialObject right;

    public SerialPair() {
        super();
    }

    public SerialPair(SerialObject left, SerialObject right) {
        super();
        this.left = left;
        this.right = right;
    }

    public SerialObject getLeft() {
        return left;
    }

    public void setLeft(SerialObject left) {
        this.left = left;
    }

    public SerialObject getRight() {
        return right;
    }

    public void setRight(SerialObject right) {
        this.right = right;
    }
    
    
}
