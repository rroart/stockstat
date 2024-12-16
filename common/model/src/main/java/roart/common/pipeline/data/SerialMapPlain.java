package roart.common.pipeline.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class SerialMapPlain extends SerialObject {

    private Map<String, Object> map = new HashMap<>();

    public SerialMapPlain() {
        super();
    }

    public SerialMapPlain(Map map) {
        super();
        this.map = map;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

    public SerialMapPlain put(String key, Object value) {
        map.put(key, value);
        return this;
    }

    public Object get(String key) {
        return map.get(key);
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }
}
