package roart.common.pipeline.data;

public class PipelineDataBatch extends SerialObject {
    private boolean loaded = true;

    private String message;

    private SerialObject value;

    public PipelineDataBatch(PipelineData data) {
        this.loaded = data.isLoaded();
        this.message = data.getMessage();
        this.value = data.getValue();
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public SerialObject getValue() {
        return value;
    }

    public void setValue(SerialObject value) {
        this.value = value;
    }
}
