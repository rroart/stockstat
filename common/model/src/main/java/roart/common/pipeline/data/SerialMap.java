package roart.common.pipeline.data;

import java.util.HashMap;
import java.util.Map;

public class SerialMap extends SerialObject {

    public Map<Object, SerialObject> map = new HashMap<>();

    public SerialMap() {
        super();
    }

    public Map<Object, SerialObject> getMap() {
        return map;
    }

    public void setMap(Map<Object, SerialObject> map) {
        this.map = map;
    }
}
    
