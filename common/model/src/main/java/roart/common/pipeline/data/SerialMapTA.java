package roart.common.pipeline.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class SerialMapTA extends SerialObject {

    private Map<String, SerialTA> map = new HashMap<>();

    public SerialMapTA() {
        super();
    }

    public SerialMapTA(Map<String, SerialTA> map) {
        super();
        this.map = map;
    }

    public Map<String, SerialTA> getMap() {
        return map;
    }

    public void setMap(Map<String, SerialTA> map) {
        this.map = map;
    }

    public SerialMapTA put(String key, SerialTA value) {
        map.put(key, value);
        return this;
    }

    public SerialTA get(String key) {
        return map.get(key);
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public Set<Entry<String, SerialTA>> entrySet() {
        return map.entrySet();
    }
}
