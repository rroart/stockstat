package roart.common.pipeline.data;

public class SerialInteger extends SerialObject {
    public Integer integer;

    public SerialInteger() {
        super();
    }

    public SerialInteger(Integer integer) {
        super();
        this.integer = integer;
    }

    public Integer getInteger() {
        return integer;
    }

    public void setInteger(Integer integer) {
        this.integer = integer;
    }
}
