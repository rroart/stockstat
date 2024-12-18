package roart.common.pipeline.data;

public     class SerialString extends SerialObject {
    public String string;

    public SerialString() {
        super();
    }

    public SerialString(String string) {
        super();
        this.string = string;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }
}
