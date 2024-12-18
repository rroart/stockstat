package roart.common.pipeline.data;

public class SerialPlain extends SerialObject {
    public Object object;

    public SerialPlain() {
        super();
    }

    public SerialPlain(Object object) {
        super();
        this.object = object;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

}
