package roart.common.pipeline.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class SerialMapL extends SerialObject {

    private Map<String, Long[]> map = new HashMap<>();

    public SerialMapL() {
        super();
    }

    public SerialMapL(Map<String, Long[]> map) {
        super();
        this.map = map;
    }

    public Map<String, Long[]> getMap() {
        return map;
    }

    public void setMap(Map<String, Long[]> map) {
        this.map = map;
    }

    public SerialMapL put(String key, Long[] value) {
        map.put(key, value);
        return this;
    }

    public Object get(String key) {
        return map.get(key);
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public Set<Entry<String, Long[]>> entrySet() {
        return map.entrySet();
    }
}
