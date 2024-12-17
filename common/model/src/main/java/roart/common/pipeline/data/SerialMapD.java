package roart.common.pipeline.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class SerialMapD extends SerialObject {

    private Map<String, Double[]> map = new HashMap<>();

    public SerialMapD() {
        super();
    }

    public SerialMapD(Map<String, Double[]> map) {
        super();
        this.map = map;
    }

    public Map<String, Double[]> getMap() {
        return map;
    }

    public void setMap(Map<String, Double[]> map) {
        this.map = map;
    }

    public SerialMapD put(String key, Double[] value) {
        map.put(key, value);
        return this;
    }

    public Object get(String key) {
        return map.get(key);
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public Set<Entry<String, Double[]>> entrySet() {
        return map.entrySet();
    }
}
