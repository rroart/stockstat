package roart.common.pipeline.data;

public class SerialKeyValue extends SerialObject {
    private String key;
    
    private SerialObject value;

    public SerialKeyValue() {
        super();
    }

    public SerialKeyValue(String key, SerialObject value) {
        super();
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public SerialObject getValue() {
        return value;
    }

    public void setValue(SerialObject value) {
        this.value = value;
    }
    
}
