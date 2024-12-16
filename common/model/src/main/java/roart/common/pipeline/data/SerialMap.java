package roart.common.pipeline.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SerialMap extends SerialObject {

    private Map<String, SerialObject> map = new HashMap<>();

    public SerialMap() {
        super();
    }

    public Map<String, SerialObject> getMap() {
        return map;
    }

    public void setMap(Map<String, SerialObject> map) {
        this.map = map;
    }

    public SerialMap put(String key, SerialObject value) {
        map.put(key, value);
        return this;
    }

    public SerialObject get(String key) {
        return map.get(key);
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public Set<Entry<String, SerialObject>> entrySet() {
        return map.entrySet();
    }
    
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }
}
    
